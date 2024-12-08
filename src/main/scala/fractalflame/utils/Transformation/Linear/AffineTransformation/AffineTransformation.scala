package fractalflame.utils.Transformation.Linear.AffineTransformation

import scala.util.Random

import fractalflame.domain.Point.Point
import fractalflame.utils.Transformation.Transformation
import fractalflame.utils.Transformation.ColorTransformation

case class AffineTransformation(
    a: Double, 
    b: Double, 
    c: Double, 
    d: Double, 
    e: Double, 
    f: Double,
    rColorCoeff: Int,
    gColorCoeff: Int,
    bColorCoeff: Int
) extends ColorTransformation:

  override def transformPoint(point: Point): Point =
    val newX = a * point.x + b * point.y + e
    val newY = c * point.x + d * point.y + f

    Point(newX, newY)
  
  override def colorCoeff: (Int, Int, Int) = 
    (rColorCoeff, gColorCoeff, bColorCoeff)

object AffineTransformation:
  def apply(): AffineTransformation = 
    val random = new Random()
    val absoluteBound = Random.nextInt(2) match
        case 0 => 1.0
        case 1 => 1.5

    AffineTransformation(
      random.between(-absoluteBound, absoluteBound), // a
      random.between(-absoluteBound, absoluteBound), // b
      random.between(-absoluteBound, absoluteBound), // c
      random.between(-absoluteBound, absoluteBound), // d
      random.nextDouble() * 2 - 1, // e
      random.nextDouble() * 2 - 1, // f
      random.between(0, 255),
      random.between(0, 255),
      random.between(0, 255),
    )