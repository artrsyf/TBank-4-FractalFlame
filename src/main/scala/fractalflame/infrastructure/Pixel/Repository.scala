package fractalflame.infrastructure.Pixel

import fractalflame.domain.Pixel.Pixel

trait PixelRepository:
    def addPixel(pixel: Pixel): PixelRepository
    def getPixel(xPixel: Int, yPixel: Int): Option[Pixel]
    def getPixels: Seq[Pixel]