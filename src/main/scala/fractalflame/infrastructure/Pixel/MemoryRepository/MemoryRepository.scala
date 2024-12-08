package fractalflame.infrastructure.Pixel.MemoryRepository

import fractalflame.infrastructure.Pixel.PixelRepository
import fractalflame.domain.Pixel.Pixel

case class PixelMemoryRepository(pixels: Option[Map[(Int, Int), Pixel]] = None)
    extends PixelRepository:
  override def addPixel(pixel: Pixel): PixelMemoryRepository =
    pixels match
      case Some(map) =>
        copy(
          pixels = Some(map + ((pixel.x, pixel.y) -> pixel))
        )
      case _ =>
        copy(
          pixels = Some(Map((pixel.x, pixel.y) -> pixel))
        )
  override def getPixel(xPixel: Int, yPixel: Int): Option[Pixel] =
    pixels match
      case Some(map) =>
        map.get((xPixel, yPixel))
      case _ =>
        None

  override def getPixels: Seq[Pixel] =
    pixels match
      case Some(map) => map.values.toSeq
      case None      => Seq.empty
