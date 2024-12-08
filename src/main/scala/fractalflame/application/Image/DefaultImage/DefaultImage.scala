package fractalflame.application.Image.DefaultImage

import fractalflame.application.Image.Image
import fractalflame.domain.Pixel.Pixel
import fractalflame.infrastructure.Pixel.PixelRepository

case class DefaultImage(
    xRes: Int,
    yRes: Int,
    pixelRepo: PixelRepository
) extends Image:
    override def addPixel(pixel: Pixel): DefaultImage = 
        copy(
            pixelRepo = pixelRepo.addPixel(pixel)
        )
    
    override def getPixel(xPixel: Int, yPixel: Int): Option[Pixel] = 
        pixelRepo.getPixel(xPixel, yPixel)

    override def getPixels: Seq[Pixel] = 
        pixelRepo.getPixels
    
    override def isValidPixel(xPixel: Int, yPixel: Int): Boolean = 
        xPixel >= 0 && xPixel < xRes && yPixel >= 0 && yPixel < yRes
    
    override def getResolution: (Int, Int) = 
        (xRes, yRes)
    
    override def aspectRatio = xRes.toDouble / yRes
    override def xMin = -aspectRatio
    override def xMax = aspectRatio
    override def yMin = -1.0
    override def yMax = 1.0

object DefaultImage:
    def invariant(xRes: Int, yRes: Int ): Boolean = 
        xRes > 0 && yRes > 0

    def apply(xRes: Int, yRes: Int, pixelRepo: PixelRepository): DefaultImage = 
        if invariant(xRes, yRes) then
            new DefaultImage(xRes, yRes, pixelRepo)
        else
            new DefaultImage(1920, 1080, pixelRepo)