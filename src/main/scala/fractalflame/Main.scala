package fractalflame

import fractalflame.domain.Point.Point
import fractalflame.domain.Pixel.Pixel
import fractalflame.utils.Transformation.Linear.AffineTransformation.AffineTransformation

import fractalflame.utils.Transformation.NonLinear.SphericalTransformation.SphericalTransformation
import fractalflame.utils.Transformation.NonLinear.SinusoidalTransformation.SinusoidalTransformation
import fractalflame.utils.Transformation.NonLinear.HyperbolicTransformation.HyperbolicTransformation
import fractalflame.utils.Transformation.NonLinear.PopcornTransformation.PopcornTransformation
import fractalflame.utils.Transformation.NonLinear.HyperbolicTransformation.HyperbolicTransformation

import fractalflame.utils.Transformation.Transformation
import fractalflame.infrastructure.Pixel.MemoryRepository.PixelMemoryRepository
import fractalflame.application.ImageProcessor.ImageProcessor

import scala.util.Random
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File
import fractalflame.application.Image.DefaultImage.DefaultImage
import fractalflame.shared.eums.SymmetryType.SymmetryType

object Main:

  def invariant(xPixel: Int, yPixel: Int, xRes: Int, yRes: Int): Boolean = 
    xPixel >= 0 && xPixel < xRes && yPixel >= 0 && yPixel < yRes

  def main(args: Array[String]): Unit =
    // Гиперпараметры
    val xRes = 1920 // Ширина изображения
    val yRes = 1080 // Высота изображения
    val sampleCount = 20 // Количество точек
    val eqCount = 5 // Количество аффинных преобразований
    val iterationCount = 1_000_000 // Число итераций для каждой точки

    val pixelRepo = PixelMemoryRepository()
    val emptyImage = DefaultImage(
      xRes,
      yRes,
      pixelRepo
    )

    val random = new Random()

    val affineTransformations = (0 until eqCount).map(_ =>
      AffineTransformation()
    )

    val nonLinearTransformations = IndexedSeq(
      PopcornTransformation(random)
    )

    val nonLinearTransformation = SphericalTransformation()

    val imageProcessor = ImageProcessor(
      emptyImage,
      sampleCount,
      eqCount,
      iterationCount,
      random,
      affineTransformations,
      nonLinearTransformations,
      SymmetryType.NoneSymmetry
    )
  
    val readyImageProcessor = imageProcessor.prepareImage

    readyImageProcessor.renderImage("png", "examples/fractal_flame33.png", true)