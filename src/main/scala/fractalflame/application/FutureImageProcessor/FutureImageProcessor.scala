package fractalflame.application.FutureImageProcessor

import scala.concurrent._
import scala.concurrent.duration._
import scala.util.Random
import scala.concurrent.ExecutionContext.Implicits.global

import fractalflame.domain.Point.Point
import fractalflame.application.Image.DefaultImage.DefaultImage
import fractalflame.utils.Transformation.Linear.AffineTransformation.AffineTransformation
import fractalflame.utils.Transformation.NonLinear.SphericalTransformation.SphericalTransformation
import fractalflame.utils.Transformation.{Transformation, ColorTransformation}
import fractalflame.domain.Pixel.Pixel
import fractalflame.shared.eums.SymmetryType.SymmetryType

import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File

case class FutureImageProcessor(
  image: DefaultImage,
  sampleCount: Int,
  eqCount: Int,
  iterationCount: Int,
  random: Random,
  affineTransformations: Seq[ColorTransformation],
  nonLinearTransformations: Seq[Transformation],
  symmetryType: SymmetryType = SymmetryType.NoneSymmetry
):

  private def applySymmetryToPixels(image: DefaultImage): DefaultImage =
    val (xRes, yRes) = image.getResolution
    val centerX = xRes / 2 - 1
    val centerY = yRes / 2 - 1

    val newPixels = image.pixelRepo.getPixels.flatMap { pixel =>
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

  def prepareImage: Future[FutureImageProcessor] = {
    val tasks: Seq[Future[DefaultImage]] = (0 until sampleCount).map { _ =>
      Future {
        val (xRes, yRes) = image.getResolution

        val initialPoint = Point(
          random.between(image.xMin, image.xMax),
          random.between(image.yMin, image.yMax)
        )

        (-20 until iterationCount)
          .foldLeft(image -> initialPoint) { case ((img, currentPoint), step) =>
            // Выбираем случайное аффинное преобразование
            val transformation = affineTransformations(random.nextInt(eqCount))

            // Применяем аффинное преобразование
            val linearTransformedcurrentPoint =
              transformation.transformPoint(currentPoint)

            // Применяем нелинейные преобразования
            val transformedPoint =
              nonLinearTransformations.foldLeft(linearTransformedcurrentPoint) {
                (currentPoint, nonLinearTransformation) =>
                  nonLinearTransformation.transformPoint(currentPoint)
              }

            if step < 0 then (img, transformedPoint)
            else
              // Преобразуем координаты в пиксельные
              val xPixel =
                xRes - ((img.yMax - transformedPoint.x) / (img.xMax - img.xMin) * xRes).toInt
              val yPixel =
                yRes - ((img.yMax - transformedPoint.y) / (img.yMax - img.yMin) * yRes).toInt

              // Если точка не попадает в изображение
              if !img.isValidPixel(xPixel, yPixel) then (img, transformedPoint)
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
    }

    val aggregated: Future[Seq[DefaultImage]] = Future.sequence(tasks)

    aggregated.map { processedImages =>
      // Сначала берем исходное изображение
      val mergedImage = processedImages.foldLeft(image) { (img, newImage) =>
        // Итерируем по пикселям нового изображения и добавляем их в текущее
        newImage.pixelRepo.getPixels.foldLeft(img) { (currentImage, pixel) =>
          currentImage.addPixel(pixel)
        }
      }
      // Возвращаем новое изображение с объединёнными пикселями
      copy(image = mergedImage)
    }

  }

  private def correctImage(): DefaultImage =
    val (xRes, yRes) = image.getResolution
    val maxLog = image.pixelRepo.getPixels.foldLeft(0.0) { (max, pixel) =>
      if pixel.hitCount > 0 then math.max(max, math.log10(pixel.hitCount))
      else max
    }

    val gamma = 2.2

    val correctedImage = image.pixelRepo.getPixels.foldLeft(image) {
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
  ): Future[Unit] = {
    val correctedImage = if applyCorrection then correctImage() else image
    val symmetrizedImage = applySymmetryToPixels(correctedImage)

    val (xRes, yRes) = image.getResolution
    val outputImage = new BufferedImage(xRes, yRes, BufferedImage.TYPE_INT_RGB)

    // Параллельная обработка пикселей при рендеринге
    val tasks: Seq[Future[Unit]] = symmetrizedImage.pixelRepo.getPixels.map {
      pixel =>
        Future {
          if pixel.hitCount > 0 then
            val rgb = (pixel.r << 16) | (pixel.g << 8) | pixel.b
            outputImage.setRGB(pixel.x, pixel.y, rgb)
        }
    }

    val aggregated: Future[Seq[Unit]] = Future.sequence(tasks)

    aggregated.map { _ =>
      ImageIO.write(outputImage, format, new File(path))
    }
  }
