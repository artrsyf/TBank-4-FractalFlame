package fractalflame.utils.Transformation.NonLinear.SinusoidalTransformation

import fractalflame.utils.Transformation.Transformation
import fractalflame.domain.Point.Point

class SinusoidalTransformation extends Transformation:
  override def transformPoint(point: Point): Point =
    Point(math.sin(point.x), math.sin(point.y))
