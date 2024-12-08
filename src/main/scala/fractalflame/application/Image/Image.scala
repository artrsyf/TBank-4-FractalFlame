package fractalflame.application.Image

import fractalflame.domain.Pixel.Pixel
import fractalflame.infrastructure.Pixel.PixelRepository

trait Image:
  def addPixel(pixel: Pixel): Image

  def getPixel(xPixel: Int, yPixel: Int): Option[Pixel]

  def getPixels: Seq[Pixel]

  def isValidPixel(xPixel: Int, yPixel: Int): Boolean

  def getResolution: (Int, Int)
  def aspectRatio: Double
  def xMin: Double
  def xMax: Double
  def yMin: Double
  def yMax: Double
