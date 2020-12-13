import sbt._

object Dependencies {

  object Versions {
    val akka = "2.6.10"
    val guice = "4.2.3"
    val cats = "2.3.0"
    val circe = "0.13.0"
    val enumeratum = "1.6.1"
    val pureConfig = "0.14.0"

    // Runtime
    val logback = "1.2.3"
    val scalaLogging = "3.9.2"

    // Test
    val quicklens = "1.6.1"
    val scalaCheck = "1.15.1"
    val scalaTest = "3.2.3"
    val scalaTestPlusScalaCheck = "3.2.2.0"
    val mockito = "1.16.3"
  }

  object Libraries {
    def akka(artifact: String): ModuleID = "com.typesafe.akka" %% artifact % Versions.akka
    def circe(artifact: String): ModuleID = "io.circe" %% artifact % Versions.circe

    lazy val akkaStream = akka("akka-stream")
    lazy val guice = "com.google.inject"          % "guice"       % Versions.guice
    lazy val cats = "org.typelevel"               %% "cats-core"  % Versions.cats
    lazy val enumeratum = "com.beachape"          %% "enumeratum" % Versions.enumeratum
    lazy val pureConfig = "com.github.pureconfig" %% "pureconfig" % Versions.pureConfig
    lazy val circeCore = Seq(circe("circe-core"), circe("circe-generic"), circe("circe-parser"), circe("circe-generic-extras"))

    // Runtime
    lazy val logback = "ch.qos.logback" % "logback-classic" % Versions.logback
    lazy val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % Versions.scalaLogging

    // Test
    lazy val akkaStreamTestKit = akka("akka-stream-testkit")
    lazy val quicklens = "com.softwaremill.quicklens"      %% "quicklens"       % Versions.quicklens
    lazy val scalaCheck = "org.scalacheck"                 %% "scalacheck"      % Versions.scalaCheck
    lazy val scalaTest = "org.scalatest"                   %% "scalatest"       % Versions.scalaTest
    lazy val scalaTestPlusScalaCheck = "org.scalatestplus" %% "scalacheck-1-14" % Versions.scalaTestPlusScalaCheck
    lazy val mockitoScalaTest  = "org.mockito"             %% "mockito-scala-scalatest" % Versions.mockito
    lazy val mockitoScala  = "org.mockito"                 %% "mockito-scala"  % Versions.mockito
  }

}
