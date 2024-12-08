package fractalflame.utils.Transformation.NonLinear.PopcornTransformation

import scala.util.Random

import fractalflame.utils.Transformation.Transformation
import fractalflame.domain.Point.Point

class PopcornTransformation(val random: Random) extends Transformation:
  override def transformPoint(point: Point): Point =
    val c = random.nextDouble()
    val f = random.nextDouble()
    
    val xAddition = c * math.sin(math.tan(3 * point.y))
    val yAddition = f * math.sin(math.tan(3 * point.x))

    Point(point.x + xAddition, point.y + yAddition)