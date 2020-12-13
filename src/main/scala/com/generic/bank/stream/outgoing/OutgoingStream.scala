package com.generic.bank.stream.outgoing

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Sink, Source}
import cats.Show
import cats.data.{EitherT, NonEmptyChain}
import cats.implicits._
import com.generic.bank.fraud.client.FraudClient
import com.generic.bank.fraud.client.domain.FraudResult
import com.generic.bank.notification.NotificationService
import com.generic.bank.parsing.MessageParser
import com.google.inject.Inject
import com.generic.bank.domain.FinancialMessage.showFinancialMessage
import com.generic.bank.fraud.client.Error.{showError => showFraudClientError}
import com.generic.bank.parsing.Error.{showError => showParsingError}
import OutgoingStream.showNec
import com.generic.bank.config.ApplicationConfig
import com.generic.bank.domain.FinancialMessage
import com.typesafe.scalalogging.LazyLogging

import java.io.File
import scala.concurrent.ExecutionContext

class OutgoingStream @Inject() (
    parser: MessageParser,
    fraudClient: FraudClient,
    notificationService: NotificationService,
    config: ApplicationConfig
  )(
    implicit ec: ExecutionContext
  ) extends LazyLogging {

  /**
    * Process files one by one:
    * 1) parse the message
    * 2) call the fraud client
    * 3) print the message to the console
    * If any of the steps 1 and 2 fail, an error is sent to the notification service
    * @param source the initial source of files
    * @return the final stream to run
    */
  def process(source: Source[File, NotUsed]): Source[FinancialMessage, NotUsed] =
    source
      .map(parser.parse)
      .divertTo(handleFailureSink, _.isLeft)
      .collect { case Right(value) => value }
      .mapAsync(config.fraudApi.maxParallelCalls)(m =>
        EitherT(fraudClient.call(m))
          .map(result => m.copy(isFraud = Some(result == FraudResult.Fraud)))
          .value
      )
      .divertTo(handleFailureSink, _.isLeft)
      .collect { case Right(value) => value }
      .map { f =>
        logger.info(s"Received message: ${f.show}")
        f
      }

  def handleFailureSink[T: Show]: Sink[Either[T, _], NotUsed] =
    Flow[Either[T, _]]
      .collect { case Left(value) => value.show }
      .mapAsync(config.notificationApi.maxParallelCalls)(notificationService.notifyError)
      .to(Sink.ignore)
}

object OutgoingStream {
  implicit def showNec[T: Show]: Show[NonEmptyChain[T]] =
    Show.show(_.map(_.show).toList.mkString(", "))
}
