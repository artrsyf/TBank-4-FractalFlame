package fractalflame.utils.Transformation.NonLinear.SphericalTransformation

import fractalflame.utils.Transformation.Transformation
import fractalflame.domain.Point.Point

class SphericalTransformation extends Transformation:
  override def transformPoint(point: Point): Point =
    val r = point.x * point.x + point.y * point.y
    
    Point(point.x / r, point.y / r)