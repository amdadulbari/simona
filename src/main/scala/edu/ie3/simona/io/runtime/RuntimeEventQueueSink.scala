/*
 * © 2022. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
 */

package edu.ie3.simona.io.runtime
import edu.ie3.simona.event.RuntimeEvent
import org.slf4j.Logger

import java.util.concurrent.BlockingQueue

final case class RuntimeEventQueueSink(queue: BlockingQueue[RuntimeEvent])
    extends RuntimeEventSink {

  override def handleRuntimeEvent(
      runtimeEvent: RuntimeEvent,
      log: Logger
  ): Unit =
    queue.put(runtimeEvent)

  override def close(): Unit = {}
}
