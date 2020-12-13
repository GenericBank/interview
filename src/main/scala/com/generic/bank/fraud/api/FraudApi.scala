package com.generic.bank.fraud.api

import cats.instances.future._
import cats.syntax.applicative._
import cats.syntax.either._
import com.generic.bank.domain.FinancialMessage
import com.generic.bank.fraud.api.domain.FraudResult
import com.google.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

/** Do not change this code :)
  */
trait FraudApi {

  def handle(financialMessage: FinancialMessage): Future[Either[Error, FraudResult]]

}

class DefaultFraudApi @Inject() (implicit ec: ExecutionContext) extends FraudApi {

  override def handle(financialMessage: FinancialMessage): Future[Either[Error, FraudResult]] =
    financialMessage.amount.currency match {
      case FinancialMessage.Amount.Currency.AUD =>
        Future.failed(new RuntimeException("A Solar flare inferred with our datacenter cable"))
      case FinancialMessage.Amount.Currency.CAD =>
        FraudResult.Fraud.asRight.pure[Future]
      case FinancialMessage.Amount.Currency.CHF =>
        Error.Illegal("CHF is not supported").asLeft.pure[Future]
      case FinancialMessage.Amount.Currency.JPY =>
        Error.System(new InterruptedException()).asLeft.pure[Future]
      case _ =>
        FraudResult.NoFraud.asRight.pure[Future]
    }

}
