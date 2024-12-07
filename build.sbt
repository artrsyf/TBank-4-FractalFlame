val scala3Version = "3.5.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "Scala 3 Project Template",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    scalacOptions ++= Seq(
      "-encoding",
      "utf8",
      "-feature",
      "-language:implicitConversions",
      "-language:existentials",
      "-unchecked",
      "-Werror"
    ),
    libraryDependencies ++= Seq(
      "org.scalactic" %% "scalactic" % "3.2.18",
      "org.scalatest" %% "scalatest" % "3.2.18" % Test,
    )
  )