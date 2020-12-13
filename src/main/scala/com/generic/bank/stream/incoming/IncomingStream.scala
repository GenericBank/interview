package com.generic.bank.stream.incoming

import java.io.File
import java.nio.file.Paths

import akka.NotUsed
import akka.stream.scaladsl._
import cats.syntax.either._
import com.generic.bank.config.ApplicationConfig
import com.generic.bank.stream.incoming
import com.google.inject.Inject

import scala.util.Try

class IncomingStream @Inject() (applicationConfig: ApplicationConfig) {

  def source(): Either[Error, Source[File, NotUsed]] =
    Try(getClass.getResource(applicationConfig.messageFolder.value)).toEither
      .leftMap(incoming.Error.System)
      .flatMap(Option(_).toRight(incoming.Error.DirectoryNotFound))
      .map(_.toURI)
      .map(Paths.get)
      .map(_.toFile)
      .flatMap { directory =>
        Either.cond(
          directory.exists() && directory.isDirectory,
          directory.listFiles().filter(_.isFile).toList,
          incoming.Error.DirectoryNotFound
        )
      }
      .map(Source(_))

}
