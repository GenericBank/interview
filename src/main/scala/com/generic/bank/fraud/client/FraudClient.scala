package com.generic.bank.fraud.client

import cats.implicits._
import com.generic.bank.domain.FinancialMessage
import com.generic.bank.fraud.api.FraudApi
import com.generic.bank.fraud.client.Error.{ApiCallFailure, ApiInternalError}
import com.generic.bank.fraud.client.domain.FraudResult
import com.generic.bank.fraud.api.domain.{FraudResult => ApiFraudResult}
import com.google.inject.Inject

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

trait FraudClient {
  def call(financialMessage: FinancialMessage): Future[Either[Error, FraudResult]]
}

class SimpleFraudClient @Inject() (fraudApi: FraudApi)(implicit ec: ExecutionContext) extends FraudClient {

  def call(financialMessage: FinancialMessage): Future[Either[Error, FraudResult]] =
    fraudApi.handle(financialMessage)
      .transform {
        case Success(Left(error)) => Success(ApiInternalError(error).asLeft)
        case Success(Right(ApiFraudResult.Fraud)) => Success(FraudResult.Fraud.asRight)
        case Success(Right(ApiFraudResult.NoFraud)) => Success(FraudResult.NoFraud.asRight)
        case Failure(NonFatal(e)) => Success(ApiCallFailure(e).asLeft)
        case Failure(exception) => Failure(exception)
      }
}
