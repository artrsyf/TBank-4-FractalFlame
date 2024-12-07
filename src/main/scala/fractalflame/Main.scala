package fractalflame

import fractalflame.domain.Point.Point
import fractalflame.domain.Pixel.Pixel
import fractalflame.utils.Transformation.Linear.AffineTransformation.AffineTransformation
import fractalflame.utils.Transformation.NonLinear.SphericalTransformation.SphericalTransformation
import fractalflame.utils.Transformation.Transformation
import fractalflame.infrastructure.Pixel.MemoryRepository.PixelMemoryRepository

import scala.util.Random
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.io.File

object Main:

  def invariant(xPixel: Int, yPixel: Int, xRes: Int, yRes: Int): Boolean = 
    xPixel >= 0 && xPixel < xRes && yPixel >= 0 && yPixel < yRes

  def main(args: Array[String]): Unit =
    // Гиперпараметры
    val xRes = 1920 // Ширина изображения
    val yRes = 1080 // Высота изображения
    val n = 30 // Количество точек
    val eqCount = 15 // Количество аффинных преобразований
    val it = 100_000 // Число итераций для каждой точки

    // Считаем на основе hparams
    val aspectRatio = xRes.toFloat / yRes
    val XMIN = -aspectRatio
    val XMAX = aspectRatio
    val YMIN = -1.0
    val YMAX = 1.0

    val pixelRepo = PixelMemoryRepository()

    // Генерируем аффинные преобразования
    val random = new Random()

    val affineTransformations = (0 until eqCount).map(_ =>
      AffineTransformation()
    )

    // Нелинейное преобразование
    val nonLinearTransformation = SphericalTransformation()

    // Создаем буфер для изображения
    val image = new BufferedImage(xRes, yRes, BufferedImage.TYPE_INT_RGB)

    // Главный цикл
    val calculatedPixelRepo = (0 until n).foldLeft(pixelRepo) { (repo, _) => 
       // Генерируем случайную точку в пределах [XMIN, XMAX], [YMIN, YMAX]
      val initialPoint = Point(random.between(XMIN, XMAX), random.between(YMIN, YMAX))

      (-20 until it).foldLeft((pixelRepo, initialPoint)) { case ((repo, currentPoint), step) => 
        // Выбираем случайное аффинное преобразование
        val transformation = affineTransformations(random.nextInt(eqCount))

        // Применяем аффинное преобразование
        val linearTransformedcurrentPoint = transformation.transformPoint(currentPoint)

        // Применяем нелинейное преобразование
        val transformedPoint = nonLinearTransformation.transformPoint(linearTransformedcurrentPoint)

        if step < 0 then
          (repo, transformedPoint)
        else
          // Преобразуем координаты в пиксельные
          val xPixel = ((transformedPoint.x - XMIN) / (XMAX - XMIN) * xRes).toInt
          val yPixel = ((transformedPoint.y - YMIN) / (YMAX - YMIN) * yRes).toInt

          // Если точка не попадает в изображение
          if !invariant(xPixel, yPixel, xRes, yRes) then
            (repo, transformedPoint)

          val existingPixel = repo.getPixel(xPixel, yPixel)
          val updatedPixel = existingPixel match
            case Some(pixel) =>
              // Обновляем цвет и увеличиваем количество попаданий
              val newR = (pixel.r + transformation.rColorCoeff) / 2
              val newG = (pixel.g + transformation.gColorCoeff) / 2
              val newB = (pixel.b + transformation.bColorCoeff) / 2
              pixel.copy(r = newR, g = newG, b = newB, hitCount = pixel.hitCount + 1)
            case None =>
              // Создаём новый пиксель
              // Pixel(xPixel, yPixel, 128, 128, 255, 1)
              Pixel(xPixel, yPixel, transformation.rColorCoeff, transformation.gColorCoeff, transformation.bColorCoeff, 1)


          (repo.addPixel(updatedPixel), transformedPoint)
      }._1
    }
    
    // Рисуем изображение
    // calculatedPixelRepo.getPixels.map { pixel => 
    //   if pixel.hitCount > 0 then 
    //     val red = Math.min(255, (pixel.r * 255).toInt)
    //     val green = Math.min(255, (pixel.g * 255).toInt)
    //     val blue = Math.min(255, (pixel.b * 255).toInt)
    //     val rgb = (red << 16) | (green << 8) | blue
    //     image.setRGB(pixel.x, pixel.y, rgb)
    // }

    for x <- 0 until xRes do
      for y <- 0 until yRes do
        val pixel = calculatedPixelRepo.getPixel(x, y).getOrElse(Pixel(x, y, 0, 0, 0, 0))
        if pixel.hitCount > 0 then
          val rgb = (pixel.r << 16) | (pixel.g << 8) | pixel.b
          image.setRGB(x, y, rgb)

    // Сохраняем изображение
    
    ImageIO.write(image, "png", new File("dist/fractal_flame12.png"))
