package com.generic.bank.parsing

import cats.implicits._
import cats.data.EitherNec
import com.generic.bank.config.ApplicationConfig
import com.generic.bank.domain.{Bic, FinancialMessage}
import com.generic.bank.domain.FinancialMessage.{Amount, ReceiverBic, SenderBic}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import com.generic.bank.parsing.Error._

import java.io.File
import java.nio.file.Paths

class MessageParserSpec extends AnyWordSpecLike with Matchers {
  "MessageParserSpec" should {
    "parse file correctly" in new Fixture {
      parser.parse(getResourceFile("mt103_1.json")) shouldBe Right(
        FinancialMessage(
          SenderBic(Bic("TNISROB2")),
          ReceiverBic(Bic("BTRLRO22")),
          Amount(
            Amount.Value(123),
            Amount.Currency.EUR
          )
        )
      )
      parser.parse(getResourceFile("mt103_2.json")) shouldBe Right(
        FinancialMessage(
          SenderBic(Bic("RNCBROBU")),
          ReceiverBic(Bic("TNISROB2")),
          Amount(
            Amount.Value(123),
            Amount.Currency.AUD
          )
        )
      )
      parser.parse(getResourceFile("mt103_4.json")) shouldBe Right(
        FinancialMessage(
          SenderBic(Bic("RNCBROBU")),
          ReceiverBic(Bic("BBRUBEBB")),
          Amount(
            Amount.Value(123),
            Amount.Currency.CAD
          )
        )
      )
      parser.parse(getResourceFile("mt202_2.json")) shouldBe Right(
        FinancialMessage(
          SenderBic(Bic("BBRUBEBB")),
          ReceiverBic(Bic("RNCBROBU")),
          Amount(
            Amount.Value(123),
            Amount.Currency.CHF
          )
        )
      )
    }

    "return error when currency is incorrect" in new Fixture {
      val parsingResult: EitherNec[Error, FinancialMessage] =
        parser.parse(getResourceFile("mt103_3.json"))
      parsingResult shouldBe Symbol("left")
      parsingResult.leftMap(_.toList).left.getOrElse(List.empty).head shouldBe InvalidCurrency
    }

    "return error when json is incorrect" in new Fixture {
      val parsingResult: EitherNec[Error, FinancialMessage] =
        parser.parse(getResourceFile("mt202_1.json"))
      parsingResult shouldBe Symbol("left")
      parsingResult.leftMap(_.toList).left.getOrElse(List.empty).head shouldBe a[InvalidJson]
    }

    "return error when file doesn't exists" in new Fixture {
      parser.parse(new File("incorrect file")) shouldBe Symbol("left")
    }
  }

  trait Fixture {
    val config: ApplicationConfig = ApplicationConfig(
      messageFolder = ApplicationConfig.MessageFolder("/messages", 1024)
    )
    val parser = new MessageParser(config)

    def getResourceFile(file: String): File =
      Paths.get(getClass.getResource(s"${config.messageFolder.path}/$file").toURI).toFile
  }
}
