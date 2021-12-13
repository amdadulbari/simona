/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
 */

package edu.ie3.simona.test.common.model.grid

import breeze.math.Complex
import edu.ie3.datamodel.models.input.connector.ConnectorPort
import org.scalatest.prop.TableDrivenPropertyChecks.Table
import org.scalatest.prop.{TableFor2, TableFor5}

trait TapTestData extends TransformerTestGrid {
  val tapDependentEquivalentCircuitParameters
      : TableFor5[ConnectorPort, Int, Complex, Complex, Complex] = Table(
    ("tapSide", "tapPos", "yij", "yii", "yjj"),
    (
      ConnectorPort.A,
      -10,
      Complex(15.208258743, -50.248897222),
      Complex(5.069419581, -16.752965741),
      Complex(-3.802064686, 12.560349306)
    ),
    (
      ConnectorPort.A,
      -9,
      Complex(14.717669751, -48.627965054),
      Complex(4.272871863, -14.120918054),
      Complex(-3.311475694, 10.939417137)
    ),
    (
      ConnectorPort.A,
      -8,
      Complex(14.257742572, -47.108341146),
      Complex(3.564435643, -11.780014974),
      Complex(-2.851548514, 9.419793229)
    ),
    (
      ConnectorPort.A,
      -7,
      Complex(13.825689766, -45.680815656),
      Complex(2.932722072, -9.692624809),
      Complex(-2.419495709, 7.992267740)
    ),
    (
      ConnectorPort.A,
      -6,
      Complex(13.419051832, -44.337262255),
      Complex(2.368067970, -7.826817907),
      Complex(-2.012857775, 6.648714338)
    ),
    (
      ConnectorPort.A,
      -5,
      Complex(13.035650351, -43.070483333),
      Complex(1.862235764, -6.155375170),
      Complex(-1.629456294, 5.381935417)
    ),
    (
      ConnectorPort.A,
      -4,
      Complex(12.673548952, -41.874081018),
      Complex(1.408172106, -4.654990484),
      Complex(-1.267354895, 4.185533102)
    ),
    (
      ConnectorPort.A,
      -3,
      Complex(12.331020602, -40.742349099),
      Complex(0.999812481, -3.305625091),
      Complex(-0.924826545, 3.053801182)
    ),
    (
      ConnectorPort.A,
      -2,
      Complex(12.006520060, -39.670182017),
      Complex(0.631922108, -2.089981879),
      Complex(-0.600326003, 1.981634101)
    ),
    (
      ConnectorPort.A,
      -1,
      Complex(11.698660571, -38.652997863),
      Complex(0.299965656, -0.993074896),
      Complex(-0.292466514, 0.964449947)
    ),
    (
      ConnectorPort.A,
      0,
      Complex(11.406194057, -37.686672917),
      Complex(0.000000000, -0.001875000),
      Complex(0.000000000, -0.001875000)
    ),
    (
      ConnectorPort.A,
      1,
      Complex(11.127994202, -36.767485772),
      Complex(-0.271414493, 0.894983294),
      Complex(0.278199855, -0.921062144)
    ),
    (
      ConnectorPort.A,
      2,
      Complex(10.863041959, -35.892069444),
      Complex(-0.517287712, 1.707445484),
      Complex(0.543152098, -1.796478472)
    ),
    (
      ConnectorPort.A,
      3,
      Complex(10.610413076, -35.057370155),
      Complex(-0.740261377, 2.444240535),
      Complex(0.795780981, -2.631177762)
    ),
    (
      ConnectorPort.A,
      4,
      Complex(10.369267325, -34.260611742),
      Complex(-0.942660666, 3.113051481),
      Complex(1.036926732, -3.427936174)
    ),
    (
      ConnectorPort.A,
      5,
      Complex(10.138839162, -33.499264815),
      Complex(-1.126537685, 3.720659053),
      Complex(1.267354895, -4.189283102)
    ),
    (
      ConnectorPort.A,
      6,
      Complex(9.918429615, -32.771019927),
      Complex(-1.293708211, 4.273063091),
      Complex(1.487764442, -4.917527989)
    ),
    (
      ConnectorPort.A,
      7,
      Complex(9.707399198, -32.073764184),
      Complex(-1.445782859, 4.775585521),
      Complex(1.698794860, -5.614783732)
    ),
    (
      ConnectorPort.A,
      8,
      Complex(9.505161714, -31.405560764),
      Complex(-1.584193619, 5.232958044),
      Complex(1.901032343, -6.282987153)
    ),
    (
      ConnectorPort.A,
      9,
      Complex(9.311178822, -30.764630952),
      Complex(-1.710216518, 5.649397022),
      Complex(2.095015235, -6.923916964)
    ),
    (
      ConnectorPort.A,
      10,
      Complex(9.124955246, -30.149338333),
      Complex(-1.824991049, 6.028667667),
      Complex(2.281238811, -7.539209583)
    ),
    (
      ConnectorPort.B,
      -10,
      Complex(15.208258743, -50.248897222),
      Complex(-3.802064686, 12.560349306),
      Complex(5.069419581, -16.752965741)
    ),
    (
      ConnectorPort.B,
      -9,
      Complex(14.717669751, -48.627965054),
      Complex(-3.311475694, 10.939417137),
      Complex(4.272871863, -14.120918054)
    ),
    (
      ConnectorPort.B,
      -8,
      Complex(14.257742572, -47.108341146),
      Complex(-2.851548514, 9.419793229),
      Complex(3.564435643, -11.780014974)
    ),
    (
      ConnectorPort.B,
      -7,
      Complex(13.825689766, -45.680815656),
      Complex(-2.419495709, 7.992267740),
      Complex(2.932722072, -9.692624809)
    ),
    (
      ConnectorPort.B,
      -6,
      Complex(13.419051832, -44.337262255),
      Complex(-2.012857775, 6.648714338),
      Complex(2.368067970, -7.826817907)
    ),
    (
      ConnectorPort.B,
      -5,
      Complex(13.035650351, -43.070483333),
      Complex(-1.629456294, 5.381935417),
      Complex(1.862235764, -6.155375170)
    ),
    (
      ConnectorPort.B,
      -4,
      Complex(12.673548952, -41.874081018),
      Complex(-1.267354895, 4.185533102),
      Complex(1.408172106, -4.654990484)
    ),
    (
      ConnectorPort.B,
      -3,
      Complex(12.331020602, -40.742349099),
      Complex(-0.924826545, 3.053801182),
      Complex(0.999812481, -3.305625091)
    ),
    (
      ConnectorPort.B,
      -2,
      Complex(12.006520060, -39.670182017),
      Complex(-0.600326003, 1.981634101),
      Complex(0.631922108, -2.089981879)
    ),
    (
      ConnectorPort.B,
      -1,
      Complex(11.698660571, -38.652997863),
      Complex(-0.292466514, 0.964449947),
      Complex(0.299965656, -0.993074896)
    ),
    (
      ConnectorPort.B,
      0,
      Complex(11.406194057, -37.686672917),
      Complex(0.000000000, -0.001875000),
      Complex(0.000000000, -0.001875000)
    ),
    (
      ConnectorPort.B,
      1,
      Complex(11.127994202, -36.767485772),
      Complex(0.278199855, -0.921062144),
      Complex(-0.271414493, 0.894983294)
    ),
    (
      ConnectorPort.B,
      2,
      Complex(10.863041959, -35.892069444),
      Complex(0.543152098, -1.796478472),
      Complex(-0.517287712, 1.707445484)
    ),
    (
      ConnectorPort.B,
      3,
      Complex(10.610413076, -35.057370155),
      Complex(0.795780981, -2.631177762),
      Complex(-0.740261377, 2.444240535)
    ),
    (
      ConnectorPort.B,
      4,
      Complex(10.369267325, -34.260611742),
      Complex(1.036926732, -3.427936174),
      Complex(-0.942660666, 3.113051481)
    ),
    (
      ConnectorPort.B,
      5,
      Complex(10.138839162, -33.499264815),
      Complex(1.267354895, -4.189283102),
      Complex(-1.126537685, 3.720659053)
    ),
    (
      ConnectorPort.B,
      6,
      Complex(9.918429615, -32.771019927),
      Complex(1.487764442, -4.917527989),
      Complex(-1.293708211, 4.273063091)
    ),
    (
      ConnectorPort.B,
      7,
      Complex(9.707399198, -32.073764184),
      Complex(1.698794860, -5.614783732),
      Complex(-1.445782859, 4.775585521)
    ),
    (
      ConnectorPort.B,
      8,
      Complex(9.505161714, -31.405560764),
      Complex(1.901032343, -6.282987153),
      Complex(-1.584193619, 5.232958044)
    ),
    (
      ConnectorPort.B,
      9,
      Complex(9.311178822, -30.764630952),
      Complex(2.095015235, -6.923916964),
      Complex(-1.710216518, 5.649397022)
    ),
    (
      ConnectorPort.B,
      10,
      Complex(9.124955246, -30.149338333),
      Complex(2.281238811, -7.539209583),
      Complex(-1.824991049, 6.028667667)
    )
  )

  val tapDependentVoltRatio: TableFor2[Int, String] = Table(
    ("tapPos", "voltRatio"),
    (-13, "8.85500"),
    (-12, "9.02000"),
    (-11, "9.18500"),
    (-10, "9.35000"),
    (-9, "9.51500"),
    (-8, "9.68000"),
    (-7, "9.84500"),
    (-6, "10.01000"),
    (-5, "10.17500"),
    (-4, "10.34000"),
    (-3, "10.50500"),
    (-2, "10.67000"),
    (-1, "10.83500"),
    (0, "11.00000"),
    (1, "11.16500"),
    (2, "11.33000"),
    (3, "11.49500"),
    (4, "11.66000"),
    (5, "11.82500"),
    (6, "11.99000"),
    (7, "12.15500"),
    (8, "12.32000"),
    (9, "12.48500"),
    (10, "12.65000"),
    (11, "12.81500"),
    (12, "12.98000"),
    (13, "13.14500")
  )
}