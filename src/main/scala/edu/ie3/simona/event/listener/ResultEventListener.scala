/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
 */

package edu.ie3.simona.event.listener

import akka.actor.{ActorRef, FSM, PoisonPill, Props, Stash}
import akka.stream.Materializer
import edu.ie3.datamodel.io.processor.result.ResultEntityProcessor
import edu.ie3.datamodel.models.result.ResultEntity
import edu.ie3.simona.agent.grid.GridResultsSupport.PartialTransformer3wResult
import edu.ie3.simona.agent.state.AgentState
import edu.ie3.simona.agent.state.AgentState.{Idle, Uninitialized}
import edu.ie3.simona.event.ResultEvent
import edu.ie3.simona.event.ResultEvent.{
  ParticipantResultEvent,
  PowerFlowResultEvent
}
import edu.ie3.simona.event.listener.ResultEventListener.{
  AggregatedTransformer3wResult,
  BaseData,
  Init,
  ResultEventListenerData,
  Transformer3wKey,
  UninitializedData
}
import edu.ie3.simona.exceptions.{
  FileHierarchyException,
  InitializationException,
  ProcessResultEventException
}
import edu.ie3.simona.io.result.{
  ResultEntityCsvSink,
  ResultEntityInfluxDbSink,
  ResultEntitySink,
  ResultSinkType
}
import edu.ie3.simona.logging.SimonaFSMActorLogging
import edu.ie3.simona.sim.SimonaSim.ServiceInitComplete
import edu.ie3.simona.util.ResultFileHierarchy

import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

object ResultEventListener extends Transformer3wResultSupport {

  /** Internal [[ResultEventListenerData]]
    */
  sealed trait ResultEventListenerData

  /** Data for state [[Uninitialized]]
    */
  private final case object UninitializedData extends ResultEventListenerData

  private final case object Init

  /** [[ResultEventListener]] base data containing all information the listener
    * needs
    *
    * @param classToSink
    *   a map containing the sink for each class that should be processed by the
    *   listener
    */
  private final case class BaseData(
      classToSink: Map[Class[_], ResultEntitySink],
      threeWindingResults: Map[
        Transformer3wKey,
        AggregatedTransformer3wResult
      ] = Map.empty
  ) extends ResultEventListenerData

  def props(
      eventClassesToConsider: Set[Class[_ <: ResultEntity]],
      resultFileHierarchy: ResultFileHierarchy,
      supervisor: ActorRef
  ): Props =
    Props(
      new ResultEventListener(
        eventClassesToConsider,
        resultFileHierarchy,
        supervisor
      )
    )

  /** Initialize the sinks for this listener based on the provided collection
    * with the model names as strings. It generates one sink for each model
    * class.
    *
    * @param eventClassesToConsider
    *   Incoming event classes that should be considered
    * @return
    *   mapping of the model class to the sink for this model class
    */
  private def initializeSinks(
      eventClassesToConsider: Set[Class[_ <: ResultEntity]],
      resultFileHierarchy: ResultFileHierarchy
  )(implicit
      materializer: Materializer
  ): Iterable[Future[(Class[_], ResultEntitySink)]] = {
    resultFileHierarchy.resultSinkType match {
      case _: ResultSinkType.Csv =>
        eventClassesToConsider
          .map(resultClass => {
            val fileName =
              resultFileHierarchy.rawOutputDataFilePaths.getOrElse(
                resultClass,
                throw new FileHierarchyException(
                  s"Unable to get file path for result class '${resultClass.getSimpleName}' from output file hierarchy! " +
                    s"Available file result file paths: ${resultFileHierarchy.rawOutputDataFilePaths}"
                )
              )
            if (fileName.endsWith(".csv") || fileName.endsWith(".csv.gz")) {
              val sink =
                ResultEntityCsvSink(
                  fileName.replace(".gz", ""),
                  new ResultEntityProcessor(resultClass),
                  fileName.endsWith(".gz")
                )
              sink.map((resultClass, _))
            } else {
              throw new ProcessResultEventException(
                s"Invalid output file format for file $fileName provided. Currently only '.csv' or '.csv.gz' is supported!"
              )
            }
          })

      case ResultSinkType.InfluxDb1x(url, database, scenario) =>
        // creates one connection per result entity that should be processed
        eventClassesToConsider
          .map(resultClass =>
            ResultEntityInfluxDbSink(url, database, scenario).map(
              (resultClass, _)
            )
          )
    }
  }
}

class ResultEventListener(
    eventClassesToConsider: Set[Class[_ <: ResultEntity]],
    resultFileHierarchy: ResultFileHierarchy,
    supervisor: ActorRef
) extends SimonaListener
    with FSM[AgentState, ResultEventListenerData]
    with SimonaFSMActorLogging
    with Stash {

  implicit private val materializer: Materializer = Materializer(context)

  override def preStart(): Unit = {
    log.debug("Starting initialization!")
    log.debug(
      s"Events that will be processed: {}",
      eventClassesToConsider.map(_.getSimpleName).mkString(",")
    )
    self ! Init
  }

  /** Handle the given result and possibly update the state data
    *
    * @param resultEntity
    *   Result entity to handle
    * @param baseData
    *   Base data
    * @return
    *   The possibly update base data
    */
  private def handleResult(
      resultEntity: ResultEntity,
      baseData: BaseData
  ): BaseData = {
    handOverToSink(resultEntity, baseData.classToSink)
    baseData
  }

  /** Handle a partial three winding result properly by adding it to an
    * [[AggregatedTransformer3wResult]] and flushing then possibly completed
    * results. Finally, the base data are updated.
    *
    * @param result
    *   Result entity to handle
    * @param baseData
    *   Base data
    * @return
    *   The possibly update base data
    */
  private def handlePartialTransformer3wResult(
      result: PartialTransformer3wResult,
      baseData: ResultEventListener.BaseData
  ): BaseData = {
    val enhancedResults =
      registerPartialTransformer3wResult(result, baseData.threeWindingResults)
    val uncompletedResults =
      flushComprehensiveResults(enhancedResults, baseData.classToSink)
    baseData.copy(threeWindingResults = uncompletedResults)
  }

  /** Register the newly received partial 3 winding transformer result result
    * within the map of yet existing results
    *
    * @param result
    *   Result, that has been received
    * @param threeWindingResults
    *   Collection of all incomplete results
    * @return
    *   Map with added result
    */
  private def registerPartialTransformer3wResult(
      result: PartialTransformer3wResult,
      threeWindingResults: Map[Transformer3wKey, AggregatedTransformer3wResult]
  ): Map[Transformer3wKey, AggregatedTransformer3wResult] = {
    val resultKey = Transformer3wKey(result.input, result.time)
    val partialTransformer3wResult =
      threeWindingResults.getOrElse(
        resultKey,
        AggregatedTransformer3wResult.EMPTY
      )
    val updatedTransformer3wResult =
      partialTransformer3wResult.add(result) match {
        case Success(value) => value
        case Failure(exception) =>
          log.warning(
            "Cannot handle the given result:\n\t{}",
            exception.getMessage
          )
          partialTransformer3wResult
      }
    threeWindingResults + (resultKey -> updatedTransformer3wResult)
  }

  /** Go through all yet available results and check, if one or more of it are
    * comprehensive. If so, hand them over to the sinks and remove them from the
    * map
    *
    * @param results
    *   Available (possibly) ready results
    * @param classToSink
    *   Mapping from result entity class to applicable sink
    * @return
    *   results without ready ones
    */
  private def flushComprehensiveResults(
      results: Map[Transformer3wKey, AggregatedTransformer3wResult],
      classToSink: Map[Class[_], ResultEntitySink]
  ): Map[Transformer3wKey, AggregatedTransformer3wResult] = {
    val comprehensiveResults = results.filter(_._2.ready)
    comprehensiveResults.map(_._2.consolidate).foreach {
      case Success(result) => handOverToSink(result, classToSink)
      case Failure(exception) =>
        log.warning("Cannot consolidate / write result.\n\t{}", exception)
    }
    results.removedAll(comprehensiveResults.keys)
  }

  /** Handing over the given result entity to the sink, that might be apparent
    * in the map
    *
    * @param resultEntity
    *   entity to handle
    * @param classToSink
    *   mapping from entity class to sink
    */
  private def handOverToSink(
      resultEntity: ResultEntity,
      classToSink: Map[Class[_], ResultEntitySink]
  ): Unit =
    Try {
      classToSink
        .get(resultEntity.getClass)
        .foreach(_.handleResultEntity(resultEntity))
    } match {
      case Failure(exception) =>
        log.error(exception, "Error while writing result event: ")
      case Success(_) =>
    }

  startWith(Uninitialized, UninitializedData)

  when(Uninitialized) {

    case Event(_: ResultEvent, _) =>
      stash()
      stay()

    case Event(baseData: BaseData, UninitializedData) =>
      unstashAll()
      goto(Idle) using baseData

    case Event(Init, _) =>
      Future
        .sequence(
          ResultEventListener.initializeSinks(
            eventClassesToConsider,
            resultFileHierarchy
          )
        )
        .onComplete {
          case Failure(exception) =>
            throw new InitializationException(
              "Cannot initialize result sinks!"
            ).initCause(exception)
            self ! PoisonPill
          case Success(classToSink) =>
            log.debug("Initialization complete!")
            supervisor ! ServiceInitComplete
            self ! BaseData(classToSink.toMap)
        }
      stay()
  }

  when(Idle) {
    case Event(
          ParticipantResultEvent(systemParticipantResult),
          baseData: BaseData
        ) =>
      val updateBaseData = handleResult(systemParticipantResult, baseData)
      stay() using updateBaseData

    case Event(
          PowerFlowResultEvent(
            nodeResults,
            switchResults,
            lineResults,
            transformer2wResults,
            transformer3wResults
          ),
          baseData: BaseData
        ) =>
      val updatedBaseData =
        (nodeResults ++ switchResults ++ lineResults ++ transformer2wResults ++ transformer3wResults)
          .foldLeft(baseData) {
            case (currentBaseData, resultEntity: ResultEntity) =>
              handleResult(resultEntity, currentBaseData)
            case (
                  currentBaseData,
                  partialTransformerResult: PartialTransformer3wResult
                ) =>
              handlePartialTransformer3wResult(
                partialTransformerResult,
                currentBaseData
              )
          }
      stay() using updatedBaseData
  }

  onTermination { case StopEvent(_, _, baseData: BaseData) =>
    // wait until all I/O has finished
    log.debug(
      "Shutdown initiated.\n\tThe following three winding results are not comprehensive and are not " +
        "handled in sinks:{}\n\tWaiting until writing result data is completed ...",
      baseData.threeWindingResults.keys
        .map { case Transformer3wKey(model, zdt) =>
          s"model '$model' at $zdt"
        }
        .mkString("\n\t\t")
    )

    // close sinks concurrently to speed up closing (closing calls might be blocking)
    Await.ready(
      Future.sequence(
        baseData.classToSink.valuesIterator.map(sink =>
          Future {
            sink.close()
          }
        )
      ),
      Duration(100, TimeUnit.MINUTES)
    )

    log.debug("Result I/O completed.")

    log.debug("Shutdown.")
  }

  whenUnhandled { case event =>
    log.error(s"Unhandled event $event in state $stateName.")
    stay()

  }
}