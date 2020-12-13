package com.generic.bank.runner

import akka.actor.ActorSystem
import com.generic.bank.stream.incoming.IncomingStream
import com.generic.bank.stream.outgoing.OutgoingStream
import com.google.inject.Inject
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure}

class AppRunner @Inject() (
    incomingStream: IncomingStream,
    outgoingStream: OutgoingStream
  )(
    implicit system: ActorSystem,
    ec: ExecutionContext
  ) extends LazyLogging {

  def run(): Unit = {
    incomingStream
      .source()
      .map(outgoingStream.process)
      .fold(e => Future.failed(new Exception(e.toString)), _.run())
      .transformWith(streamResult =>
        // be sure to wait for the actor system to shutdown before sending the exit code
        system.terminate().transform(_ => streamResult)
      )
      .onComplete {
        case Success(_) => System.exit(0)
        case Failure(e) =>
          logger.error("Error while processing the stream", e)
          System.exit(1)
      }
  }
}
