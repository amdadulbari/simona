/*
 * © 2021. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
 */

package edu.ie3.simona.sim.setup

import edu.ie3.simona.api.ExtLinkInterface
import edu.ie3.simona.test.common.UnitSpec

import java.io.{BufferedReader, File, FileReader, InputStreamReader}
import java.nio.file.Paths
import java.util.ServiceConfigurationError
import scala.jdk.CollectionConverters._

class ExtSimLoaderSpec extends UnitSpec {

  private val resourceDir = "ext-sim-loader"

  private val noJarsDir = s"$resourceDir/no-jars"
  private val jarsDir = s"$resourceDir/jars"

  private val workingJar = s"$jarsDir/mock_ext_sim.jar"
  private val workingJar2 = s"$jarsDir/mock_ext_sim-2.jar"
  private val wrongImplementationJar =
    s"$jarsDir/mock_ext_sim-wrong_implementation.jar"
  private val emptyFileJar = s"$jarsDir/mock_ext_sim-empty_file.jar"
  private val missingServiceFileJar =
    s"$jarsDir/mock_ext_sim-missing_service_file.jar"
  private val twoImplementationJar =
    s"$jarsDir/mock_ext_sim-two_implementations.jar"

  // testing whether this test is complete
  "An ExtSimLoaderSpec " should {
    "have a test directory available" in {
      val dir = getResource(resourceDir)
      assert(dir.isDirectory, "Testing resource directory does not exist")
    }

    "have a directory with an irrelevant file" in {
      val dir = getResource(noJarsDir)
      assert(dir.isDirectory, "Directory with irrelevant file does not exist")

      val files = dir.listFiles().toVector
      files.size shouldBe 1

      val file = files.headOption.value
      assert(file.isFile)
      fileSuffix(file.getName) shouldBe "txt"
    }

    "have a directory with all relevant jars" in {
      val dir = getResource(jarsDir)
      assert(dir.isDirectory, "Directory with jars does not exist")

      val files = dir.listFiles().toVector
      files.size shouldBe 7

      files.count(file => fileSuffix(file.getName) == "jar") shouldBe 6

      files.count(file => fileSuffix(file.getName) == "txt") shouldBe 1
    }
  }

  // testing the actual class
  "An ExtSimLoader" should {
    "ignore irrelevant files" in {
      val dir = getResource(noJarsDir)
      val jars = ExtSimLoader.scanInputFolder(dir)

      jars shouldBe empty
    }

    "find all jars in directory" in {
      val dir = getResource(jarsDir)
      val jars = ExtSimLoader.scanInputFolder(dir)

      jars.size shouldBe 6

      jars.foreach { jar =>
        fileSuffix(jar.getName) shouldBe "jar"
      }
    }

    "no service loaded when the META-INF/service file is missing" in {
      val jar = getResource(missingServiceFileJar)
      val extSim = ExtSimLoader.loadExtLink(jar)

      extSim.iterator.hasNext shouldBe false
    }

    "no service loaded when service file is empty" in {
      val jar = getResource(emptyFileJar)
      val extSim = ExtSimLoader.loadExtLink(jar)

      extSim.iterator.hasNext shouldBe false
    }

    "throw exception when ExtLinkInterface is not implemented" in {
      val jar = getResource(wrongImplementationJar)
      assertThrows[ServiceConfigurationError] {
        val extSim = ExtSimLoader.loadExtLink(jar)

        extSim.iterator.next().getExtSimulation shouldBe false
      }
    }

    "load a proper jar correctly" in {
      val jar = getResource(workingJar)
      val jars = Iterable(jar)
      val extSim = jars.flatMap(ExtSimLoader.loadExtLink).iterator.next()

      extSim should not be null
      extSim shouldBe an[ExtLinkInterface]
    }

    "load multiple proper jars correctly" in {
      val jarOne = getResource(workingJar)
      val jarTwo = getResource(workingJar2)
      val jars = Iterable(jarOne, jarTwo)
      val extSims = jars.flatMap(ExtSimLoader.loadExtLink)

      extSims.size shouldBe 2

      val extSimIterator = extSims.iterator

      while (extSimIterator.hasNext) {
        val extSim = extSimIterator.next()

        extSim should not be null
        extSim shouldBe an[ExtLinkInterface]
      }
    }

    "test for log message when two implementations are loaded" in {
      val jarOne = getResource(twoImplementationJar)
      val jars = Iterable(jarOne)
      jars.flatMap(ExtSimLoader.loadExtLink)

      val path =
        Paths.get("test/logs/simona/simona_tests.log").toAbsolutePath.toUri

      val reader = new BufferedReader(new FileReader(new File(path)))
      var line = reader.readLine()
      var log = ""

      while (line != null) {
        log = line
        line = reader.readLine()
      }

      log.substring(
        52
      ) shouldBe "WARN  e.ie3.simona.sim.setup.ExtSimLoader$ - External simulation mock_ext_sim-two_implementations.jar was loaded with 2 implementations."
    }
  }

  private def getResource(name: String): File = {
    val path = getClass.getResource(name)
    path should not be null
    new File(path.getPath)
  }

  private def fileSuffix(fileName: String): String =
    if (fileName.contains("."))
      fileName.substring(fileName.lastIndexOf('.') + 1)
    else
      fileName
}
