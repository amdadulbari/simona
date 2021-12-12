/*
 * © 2021. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
 */

package edu.ie3.simona.api

import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestProbe}
import com.typesafe.config.ConfigFactory
import edu.ie3.simona.api.ExtSimAdapter.InitExtSimAdapter
import edu.ie3.simona.api.data.ontology.ScheduleDataServiceMessage
import edu.ie3.simona.api.simulation.ExtSimAdapterData
import edu.ie3.simona.api.simulation.ontology.{
  ActivityStartTrigger => ExtActivityStartTrigger,
  CompletionMessage => ExtCompletionMessage
}
import edu.ie3.simona.ontology.messages.SchedulerMessage.{
  CompletionMessage,
  ScheduleTriggerMessage,
  TriggerWithIdMessage
}
import edu.ie3.simona.ontology.trigger.Trigger.{
  ActivityStartTrigger,
  InitializeExtSimAdapterTrigger
}
import edu.ie3.simona.test.common.TestKitWithShutdown
import edu.ie3.simona.util.SimonaConstants.INIT_SIM_TICK
import org.scalatest.wordspec.AnyWordSpecLike

import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters.SeqHasAsJava

class ExtSimAdapterSpec
    extends TestKitWithShutdown(
      ActorSystem(
        "ExtSimAdapterSpec",
        ConfigFactory
          .parseString("""
                     |akka.loggers = ["akka.testkit.TestEventListener"]
                     |akka.loglevel = "INFO"
                     |""".stripMargin)
      )
    )
    with AnyWordSpecLike {

  private val scheduler = TestProbe("scheduler")
  private val mainArgs = Array.empty[String]

  "An uninitialized ExtSimScheduler" must {
    "send correct completion message after initialisation" in {
      val extSimAdapter = TestActorRef(
        new ExtSimAdapter(scheduler.ref)
      )

      val extData = new ExtSimAdapterData(extSimAdapter, mainArgs)

      val triggerId = 1L

      scheduler.send(
        extSimAdapter,
        TriggerWithIdMessage(
          InitializeExtSimAdapterTrigger(
            InitExtSimAdapter(
              extData
            )
          ),
          triggerId,
          extSimAdapter
        )
      )

      scheduler.expectMsg(
        CompletionMessage(
          triggerId,
          Some(
            Seq(
              ScheduleTriggerMessage(
                ActivityStartTrigger(INIT_SIM_TICK),
                extSimAdapter
              )
            )
          )
        )
      )
    }
  }

  "An initialized ExtSimScheduler" must {
    "forward an activation trigger and a corresponding completion message properly" in {
      val extSimAdapter = TestActorRef(
        new ExtSimAdapter(scheduler.ref)
      )

      val extData = new ExtSimAdapterData(extSimAdapter, mainArgs)

      scheduler.send(
        extSimAdapter,
        TriggerWithIdMessage(
          InitializeExtSimAdapterTrigger(
            InitExtSimAdapter(
              extData
            )
          ),
          1L,
          extSimAdapter
        )
      )

      scheduler.expectMsgType[CompletionMessage]

      val triggerId = 2L

      scheduler.send(
        extSimAdapter,
        TriggerWithIdMessage(
          ActivityStartTrigger(
            INIT_SIM_TICK
          ),
          triggerId,
          extSimAdapter
        )
      )

      awaitCond(
        !extData.receiveTriggerQueue.isEmpty,
        max = 3.seconds,
        message = "No message received"
      )
      extData.receiveTriggerQueue.size() shouldBe 1
      extData.receiveTriggerQueue.take() shouldBe new ExtActivityStartTrigger(
        INIT_SIM_TICK
      )
      scheduler.expectNoMessage()

      // external simulation sends completion
      val nextTick = 900L
      extData.send(
        new ExtCompletionMessage(
          List[java.lang.Long](nextTick).asJava
        )
      )

      scheduler.expectMsg(
        CompletionMessage(
          triggerId,
          Some(
            Seq(
              ScheduleTriggerMessage(
                ActivityStartTrigger(nextTick),
                extSimAdapter
              )
            )
          )
        )
      )
    }

    "schedule the data service when it is told to" in {
      val extSimAdapter = TestActorRef(
        new ExtSimAdapter(scheduler.ref)
      )

      val extData = new ExtSimAdapterData(extSimAdapter, mainArgs)
      val dataService = TestProbe("dataService")

      scheduler.send(
        extSimAdapter,
        TriggerWithIdMessage(
          InitializeExtSimAdapterTrigger(
            InitExtSimAdapter(
              extData
            )
          ),
          1L,
          extSimAdapter
        )
      )

      scheduler.expectMsgType[CompletionMessage]

      val triggerId = 2L
      val tick = 0L

      scheduler.send(
        extSimAdapter,
        TriggerWithIdMessage(
          ActivityStartTrigger(
            tick
          ),
          triggerId,
          extSimAdapter
        )
      )

      awaitCond(
        !extData.receiveTriggerQueue.isEmpty,
        max = 3.seconds,
        message = "No message received"
      )
      extData.receiveTriggerQueue.size() shouldBe 1
      extData.receiveTriggerQueue.take()
      scheduler.expectNoMessage()

      extSimAdapter ! new ScheduleDataServiceMessage(
        dataService.ref
      )

      scheduler.expectMsg(
        ScheduleTriggerMessage(
          ActivityStartTrigger(tick),
          dataService.ref
        )
      )
      dataService.expectNoMessage()
    }
  }

}
