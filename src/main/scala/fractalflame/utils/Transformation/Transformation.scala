package fractalflame.utils.Transformation

import fractalflame.domain.Point.Point

trait Transformation:
    def transformPoint(point: Point): Point