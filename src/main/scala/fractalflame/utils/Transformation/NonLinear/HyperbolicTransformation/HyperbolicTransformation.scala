package fractalflame.utils.Transformation.NonLinear.HyperbolicTransformation

import fractalflame.utils.Transformation.Transformation
import fractalflame.domain.Point.Point

class HyperbolicTransformation extends Transformation:
  override def transformPoint(point: Point): Point =
    Point(1 / math.sinh(point.x), 1 / math.cosh(point.y))