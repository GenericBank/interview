package com.generic.bank.stream.incoming

import akka.NotUsed
import akka.stream.scaladsl._
import cats.syntax.either._
import com.generic.bank.config.ApplicationConfig
import com.generic.bank.stream.incoming
import com.google.inject.Inject

import java.io.File
import java.nio.file.Paths

class IncomingStream @Inject() (applicationConfig: ApplicationConfig) {

  def source(): Either[Error, Source[File, NotUsed]] =
    Either.catchNonFatal(getClass.getResource(applicationConfig.messageFolder.path))
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
