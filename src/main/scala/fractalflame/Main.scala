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
import fractalflame.application.FutureImageProcessor.FutureImageProcessor

import scala.util.Random
import scala.concurrent.ExecutionContext.Implicits.global

import cats.effect.{IO, IOApp, ExitCode}

import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File
import fractalflame.application.Image.DefaultImage.DefaultImage
import fractalflame.shared.eums.SymmetryType.SymmetryType

object Main extends IOApp:

  override def run(args: List[String]): IO[ExitCode] =
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

    // val imageProcessor = ImageProcessor(
    //   emptyImage,
    //   sampleCount,
    //   eqCount,
    //   iterationCount,
    //   random,
    //   affineTransformations,
    //   nonLinearTransformations,
    //   SymmetryType.NoneSymmetry
    // )

    val imageProcessor = FutureImageProcessor(
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

    // readyImageProcessor.renderImage("png", "examples/fractal_flame33.png", true) *>
    //   IO(ExitCode.Success)

    IO.fromFuture(IO(readyImageProcessor)).flatMap { imageProcessor =>
      IO.fromFuture(IO(imageProcessor.renderImage("png", "examples/fractal_flame33.png", true)))
    }.as(ExitCode.Success)