package fractalflame.application.ImageProcessor

import fractalflame.domain.Point.Point
import fractalflame.application.Image.Image
import fractalflame.utils.Transformation.Linear.AffineTransformation.AffineTransformation
import fractalflame.utils.Transformation.NonLinear.SphericalTransformation.SphericalTransformation
import fractalflame.utils.Transformation.{Transformation, ColorTransformation}
import fractalflame.domain.Pixel.Pixel
import fractalflame.shared.eums.SymmetryType.SymmetryType

import scala.util.Random

import cats.effect.IO

import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File

case class ImageProcessor(
  image: Image,
  sampleCount: Int,
  eqCount: Int,
  iterationCount: Int,
  random: Random,
  affineTransformations: Seq[ColorTransformation],
  nonLinearTransformations: Seq[Transformation],
  symmetryType: SymmetryType = SymmetryType.NoneSymmetry
):
  private def applySymmetryToPixels(image: Image): Image =
    val (xRes, yRes) = image.getResolution
    val centerX = xRes / 2 - 1
    val centerY = yRes / 2 - 1

    val newPixels = image.getPixels.flatMap { pixel =>
      symmetryType match {
        case SymmetryType.ReflectX =>
          Some(pixel.copy(y = -pixel.y))
        case SymmetryType.ReflectY =>
          Some(pixel.copy(x = -pixel.x))
        case SymmetryType.ReflectCenter =>
          Some(
            pixel.copy(
              x = math.max(0, 2 * centerX - pixel.x),
              y = math.max(0, 2 * centerY - pixel.y)
            )
          )
        case SymmetryType.Rotate90 =>
          Some(pixel.copy(x = -pixel.y, y = pixel.x))
        case SymmetryType.Rotate180 =>
          Some(pixel.copy(x = -pixel.x, y = -pixel.y))
        case SymmetryType.NoneSymmetry =>
          None // Без симметрии, ничего не делаем
      }
    }

    // Добавляем новые пиксели в изображение
    newPixels.foldLeft(image)((img, pixel) => img.addPixel(pixel))

  def prepareImage: ImageProcessor =
    val (xRes, yRes) = image.getResolution

    val preparedImage = (0 until sampleCount).foldLeft(image) {
      (currentImage, _) =>
        val initialPoint = Point(
          random.between(image.xMin, image.xMax),
          random.between(image.yMin, image.yMax)
        )

        (-20 until iterationCount)
          .foldLeft((currentImage, initialPoint)) {
            case ((img, currentPoint), step) =>
              // Выбираем случайное аффинное преобразование
              val transformation =
                affineTransformations(random.nextInt(eqCount))

              // Применяем аффинное преобразование
              val linearTransformedcurrentPoint =
                transformation.transformPoint(currentPoint)

              // Применяем нелинейные преобразования
              val transformedPoint = nonLinearTransformations.foldLeft(
                linearTransformedcurrentPoint
              ) { (currentPoint, nonLinearTransformation) =>
                nonLinearTransformation.transformPoint(currentPoint)
              }
              // val transformedPoint = nonLinearTransformation.transformPoint(linearTransformedcurrentPoint)

              if step < 0 then (img, transformedPoint)
              else
                // Преобразуем координаты в пиксельные
                val xPixel =
                  xRes - ((img.yMax - transformedPoint.x) / (img.xMax - img.xMin) * xRes).toInt
                val yPixel =
                  yRes - ((img.yMax - transformedPoint.y) / (img.yMax - img.yMin) * yRes).toInt

                // Если точка не попадает в изображение
                if !img.isValidPixel(xPixel, yPixel) then
                  (img, transformedPoint)
                else
                  val existingPixel = img.getPixel(xPixel, yPixel)
                  val updatedPixel = existingPixel match
                    case Some(pixel) =>
                      val newR = (pixel.r + transformation.colorCoeff._1) / 2
                      val newG = (pixel.g + transformation.colorCoeff._2) / 2
                      val newB = (pixel.b + transformation.colorCoeff._3) / 2
                      pixel.copy(
                        r = newR,
                        g = newG,
                        b = newB,
                        hitCount = pixel.hitCount + 1
                      )
                    case None =>
                      Pixel(
                        xPixel,
                        yPixel,
                        transformation.colorCoeff._1,
                        transformation.colorCoeff._2,
                        transformation.colorCoeff._3,
                        1
                      )

                  (img.addPixel(updatedPixel), transformedPoint)
          }
          ._1
    }

    copy(
      image = preparedImage
    )

  private def correctImage(): Image =
    val (xRes, yRes) = image.getResolution
    val maxLog = image.getPixels.foldLeft(0.0) { (max, pixel) =>
      if pixel.hitCount > 0 then math.max(max, math.log10(pixel.hitCount))
      else max
    }

    val gamma = 2.2

    val correctedImage = image.getPixels.foldLeft(image) {
      (currentImage, pixel) =>
        if pixel.hitCount > 0 then
          val normalized = math.log10(pixel.hitCount) / maxLog
          val correctionFactor = math.pow(normalized, 1.0 / gamma)
          val correctedPixel = pixel.copy(
            r = (pixel.r * correctionFactor).toInt,
            g = (pixel.g * correctionFactor).toInt,
            b = (pixel.b * correctionFactor).toInt
          )

          currentImage.addPixel(correctedPixel)
        else currentImage
    }

    correctedImage

  def renderImage(
    format: String,
    path: String,
    applyCorrection: Boolean
  ): IO[Unit] =
    val correctedImage = if applyCorrection then correctImage() else image
    val symmetrizedImage = applySymmetryToPixels(correctedImage)

    val (xRes, yRes) = image.getResolution

    val outputImage = BufferedImage(xRes, yRes, BufferedImage.TYPE_INT_RGB)

    symmetrizedImage.getPixels.map { pixel =>
      if pixel.hitCount > 0 then
        val rgb = (pixel.r << 16) | (pixel.g << 8) | pixel.b
        outputImage.setRGB(pixel.x, pixel.y, rgb)
    }

    IO.delay {
      ImageIO.write(outputImage, format, File(path))
    }
