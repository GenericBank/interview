package com.generic.bank.fraud.client

import cats.implicits._
import com.generic.bank.domain.FinancialMessage
import com.generic.bank.fraud.api.{FraudApi, Error => FraudApiError}
import com.generic.bank.fraud.api.domain.{FraudResult => ApiFraudResult}
import com.generic.bank.fraud.client.Error.{ApiCallFailure, ApiInternalError}
import com.generic.bank.fraud.client.domain.FraudResult
import org.mockito.scalatest.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FraudClientSpec extends AnyWordSpec with Matchers with MockitoSugar with ScalaFutures with ScalaCheckPropertyChecks {
  import com.generic.bank.arbitraries.ArbitraryFinancialMessage._

  "FraudClient" should {
    "return successful no fraud result" in new Fixture {
      forAll { (financialMessage: FinancialMessage) =>
        when(fraudApi.handle(any[FinancialMessage])).thenReturn(ApiFraudResult.NoFraud.asRight.pure[Future])
        fraudClient.call(financialMessage).futureValue shouldBe FraudResult.NoFraud.asRight
      }
    }

    "return successful fraud result" in new Fixture {
      forAll { (financialMessage: FinancialMessage) =>
        when(fraudApi.handle(any[FinancialMessage])).thenReturn(ApiFraudResult.Fraud.asRight.pure[Future])
        fraudClient.call(financialMessage).futureValue shouldBe FraudResult.Fraud.asRight
      }
    }

    "return internal error" in new Fixture {
      forAll { (financialMessage: FinancialMessage) =>
        val error = FraudApiError.Illegal("CHF is not supported")
        when(fraudApi.handle(any[FinancialMessage])).thenReturn(error.asLeft.pure[Future])
        fraudClient.call(financialMessage).futureValue shouldBe ApiInternalError(error).asLeft
      }
    }

    "return exception calls" in new Fixture {
      forAll { (financialMessage: FinancialMessage) =>
        val error = new RuntimeException("Http stream broken")
        when(fraudApi.handle(any[FinancialMessage])).thenReturn(Future.failed(error))
        fraudClient.call(financialMessage).futureValue shouldBe ApiCallFailure(error).asLeft
      }
    }
  }

  trait Fixture {
    val fraudApi: FraudApi = mock[FraudApi]
    val fraudClient: FraudClient = new SimpleFraudClient(fraudApi)
  }
}
