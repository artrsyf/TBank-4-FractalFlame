package fractalflame.utils.Transformation

import fractalflame.domain.Point.Point

trait Transformation:
  def transformPoint(point: Point): Point

trait ColorTransformation extends Transformation:
  def transformPoint(point: Point): Point
  def colorCoeff: (Int, Int, Int)
