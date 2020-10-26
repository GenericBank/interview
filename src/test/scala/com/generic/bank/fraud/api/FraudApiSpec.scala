package com.generic.bank.fraud.api

import com.generic.bank.domain.FinancialMessage
import com.generic.bank.fraud.api.domain.FraudResult
import com.generic.bank.modules.ActorSystemModule
import com.softwaremill.quicklens._
import org.scalatest.RecoverMethods
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import scala.concurrent.ExecutionContext

class FraudApiSpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with RecoverMethods
    with ScalaCheckPropertyChecks {
  import com.generic.bank.arbitraries.ArbitraryFinancialMessage._

  "FraudApi#handle" should {

    val actorSystemModule = ActorSystemModule()
    val fraudApi = new DefaultFraudApi(actorSystemModule)

    implicit val executionContext: ExecutionContext = actorSystemModule.executionContext

    "return Fraud" in {
      forAll { (financialMessage: FinancialMessage) =>
        val fraudMessage = financialMessage
          .modify(_.amount.currency)
          .setTo(FinancialMessage.Amount.Currency.CAD)

        val result = fraudApi.handle(fraudMessage)

        result.futureValue shouldBe Right(FraudResult.Fraud)
      }
    }

    "return NoFraud" in {
      forAll { (financialMessage: FinancialMessage) =>
        val noFraudMessage = financialMessage
          .modify(_.amount.currency)
          .setTo(FinancialMessage.Amount.Currency.EUR)

        val result = fraudApi.handle(noFraudMessage)

        result.futureValue shouldBe Right(FraudResult.NoFraud)
      }
    }

    "return an Error.Illegal" in {
      forAll { (financialMessage: FinancialMessage) =>
        val CHFMessage = financialMessage
          .modify(_.amount.currency)
          .setTo(FinancialMessage.Amount.Currency.CHF)

        val result = fraudApi.handle(CHFMessage)

        result.futureValue.swap.getOrElse(null) shouldBe a[Error.Illegal]
      }
    }

    "return an Error.System" in {
      forAll { (financialMessage: FinancialMessage) =>
        val JPYMessage = financialMessage
          .modify(_.amount.currency)
          .setTo(FinancialMessage.Amount.Currency.JPY)

        val result = fraudApi.handle(JPYMessage)

        result.futureValue.swap.getOrElse(null) shouldBe a[Error.System]
      }
    }

    "return Future.Failure" in {
      forAll { (financialMessage: FinancialMessage) =>
        val CADMessage = financialMessage
          .modify(_.amount.currency)
          .setTo(FinancialMessage.Amount.Currency.CAD)

        val result = fraudApi.handle(CADMessage)

        recoverToExceptionIf[RuntimeException](result)
      }
    }
  }

}
