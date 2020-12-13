package com.generic.bank.stream.outgoing

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Sink, Source}
import cats.implicits._
import com.generic.bank.config.ApplicationConfig
import com.generic.bank.domain.FinancialMessage
import com.generic.bank.fraud.client.domain.FraudResult
import com.generic.bank.fraud.client.{FraudClient, Error => FraudApiError}
import com.generic.bank.notification.NotificationService
import com.generic.bank.parsing.{MessageParser, Error => ParsingError}
import com.softwaremill.quicklens.ModifyPimp
import org.mockito.scalatest.MockitoSugar
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.File
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class OutgoingStreamSpec
    extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures with BeforeAndAfterAll {
  import com.generic.bank.arbitraries.ArbitraryFinancialMessage._
  import com.generic.bank.parsing.Error.{showError => showParsingError}
  import com.generic.bank.fraud.client.Error.{showError => showFraudClientError}

  implicit val actorSystem: ActorSystem = ActorSystem()

  override def afterAll(): Unit = actorSystem.terminate().void.futureValue

  "OutgoingStream" should {
    "print to console and return message without fraud" in new Fixture {
      val message: FinancialMessage = arbFinancialMessage.arbitrary.sample.get
      when(parser.parse(any[File])).thenReturn(message.rightNec)
      when(fraudClient.call(any[FinancialMessage]))
        .thenReturn(FraudResult.NoFraud.asRight.pure[Future])

      val processed: Seq[FinancialMessage] = stream.process(source).runWith(Sink.seq).futureValue

      verifyZeroInteractions(notificationService)
      verify(parser).parse(file)
      verify(fraudClient).call(message)
      processed shouldBe List(message.modify(_.isFraud).setTo(Some(false)))
    }

    "print to console and return message with fraud" in new Fixture {
      val message: FinancialMessage = arbFinancialMessage.arbitrary.sample.get
      when(parser.parse(any[File])).thenReturn(message.rightNec)
      when(fraudClient.call(any[FinancialMessage]))
        .thenReturn(FraudResult.Fraud.asRight.pure[Future])

      val processed: Seq[FinancialMessage] = stream.process(source).runWith(Sink.seq).futureValue

      verifyZeroInteractions(notificationService)
      verify(parser).parse(file)
      verify(fraudClient).call(message)
      processed shouldBe List(message.modify(_.isFraud).setTo(Some(true)))
    }

    "call notification service if the parsing fails" in new Fixture {
      val error: ParsingError = ParsingError.FileTooLarge(file)
      when(parser.parse(any[File])).thenReturn(error.leftNec)
      when(notificationService.notifyError(any[String])).thenReturn(().pure[Future])

      val processed: Seq[FinancialMessage] = stream.process(source).runWith(Sink.seq).futureValue

      verify(notificationService).notifyError(error.show)
      verify(parser).parse(file)
      verifyZeroInteractions(fraudClient)
      processed shouldBe Symbol("empty")
    }

    "call notification service if fraud service call fails" in new Fixture {
      val message: FinancialMessage = arbFinancialMessage.arbitrary.sample.get
      val error: FraudApiError =
        FraudApiError.ApiCallFailure(new RuntimeException("Http stream broken"))
      when(parser.parse(any[File])).thenReturn(message.rightNec)
      when(fraudClient.call(any[FinancialMessage])).thenReturn(error.asLeft.pure[Future])
      when(notificationService.notifyError(any[String])).thenReturn(().pure[Future])

      val processed: Seq[FinancialMessage] = stream.process(source).runWith(Sink.seq).futureValue

      verify(notificationService).notifyError(error.show)
      verify(parser).parse(file)
      verify(fraudClient).call(message)
      processed shouldBe Symbol("empty")
    }
  }

  trait Fixture {
    val parser: MessageParser = mock[MessageParser]
    val fraudClient: FraudClient = mock[FraudClient]
    val notificationService: NotificationService = mock[NotificationService]
    val config = new ApplicationConfig(
      ApplicationConfig.MessageFolder("/messages", 1023),
      ApplicationConfig.FraudApi(1),
      ApplicationConfig.NotificationApi(1)
    )

    val stream = new OutgoingStream(parser, fraudClient, notificationService, config)

    val file: File = new File("dummyPath")
    val source: Source[File, NotUsed] = Source(List(file))
  }

}
