import Dependencies._

name := "generic-bank-interview"

version := "0.1"

scalaVersion := "2.13.4"

libraryDependencies ++= Seq(
  Libraries.akkaStream,
  Libraries.guice,
  Libraries.cats,
  Libraries.enumeratum,
  Libraries.pureConfig,
  Libraries.logback,
  Libraries.scalaLogging,
  Libraries.akkaStreamTestKit       % Test,
  Libraries.quicklens               % Test,
  Libraries.scalaCheck              % Test,
  Libraries.scalaTest               % Test,
  Libraries.scalaTestPlusScalaCheck % Test,
  Libraries.mockitoScala            % Test,
  Libraries.mockitoScalaTest        % Test
) ++ Libraries.circeCore


scalacOptions += "-Ymacro-annotations"
addCommandAlias("update", ";dependencyUpdates; reload plugins; dependencyUpdates; reload return")
addCommandAlias("fmt", ";scalafmtSbt;scalafmtAll")
