package com.generic.bank.parsing

import cats.data.{EitherNec, NonEmptyChain}
import cats.implicits._
import com.generic.bank.config.ApplicationConfig
import com.generic.bank.domain.{Bic, FinancialMessage}
import com.generic.bank.parsing.Error._
import com.generic.bank.parsing.MessageParser.JsonMessage.{MT103, MT202}
import com.generic.bank.parsing.MessageParser.{JsonMessage, currencyAndAmountRegex}
import com.generic.bank.parsing.{Error => ParsingError}
import com.google.inject.Inject
import io.circe.generic.extras.{Configuration, _}
import io.circe.parser._

import java.io.File
import scala.io.Source
import scala.util.matching.Regex

class MessageParser @Inject() (config: ApplicationConfig) {

  /**
    * Parses a file into Financial messages
    * The file should be smaller than the configured max file size
    * The file should be readable
    * The file should be a valid MT103 or MT202 file
    * The currencies and amount should be well formatted
    * @param file
    * @return Either containing either the parsed messaged or a chain of errors
    */
  def parse(file: File): EitherNec[ParsingError, FinancialMessage] =
    Either
      .cond(file.length() <= config.messageFolder.maxFileSize, file, NonEmptyChain(FileTooLarge(file)))
      .flatMap(readFile)
      .flatMap(content => toJson(file, content))
      .flatMap {
        case m: MT103 => mt103ToFinancialMessage(file, m)
        case m: MT202 => mt202ToFinancialMessage(file, m)
      }

  private def readFile(file: File): EitherNec[ParsingError, String] = {
    Either
      .catchNonFatal(Source.fromFile(file))
      .flatMap { source =>
        val result = Either.catchNonFatal(source.mkString)
        source.close() // don't forget to close the source once it has been opened
        result
      }
      .leftMap(e => CannotReadFile(file, e))
      .leftMap(NonEmptyChain(_))
  }

  private def toJson(file: File, value: String): EitherNec[ParsingError, JsonMessage] = {
    decode[JsonMessage](value)
      .leftMap(e => InvalidJson(file, e))
      .leftMap(NonEmptyChain(_))
  }

  /**
    * Mapping from raw MT103 file to FinancialMessage
    * Assumption that Bic are completely valid but the code could evolve to add validation for each fields
    * Question ? What do we do with the IBAN information ?
    * @param file the original file
    * @param message the parsed json message
    * @return Either containing either the parsed messaged or a chain of errors
    */
  private def mt103ToFinancialMessage(file: File, message: MT103): EitherNec[ParsingError, FinancialMessage] =
    (
      FinancialMessage.SenderBic(Bic(message.sendingInstitution)).rightNec,
      FinancialMessage.ReceiverBic(Bic(message.accountWithInstitution)).rightNec,
      extractCurrencyAndAmount(file, message.currencyInstructedAmount),
      None.rightNec
    ).parMapN(FinancialMessage.apply)

  /**
    * Mapping from raw MT202 file to FinancialMessage
    * Assumption that Bic are completely valid but the code could evolve to add validation for each fields
    * @param file the original file
    * @param message the parsed json message
    * @return Either containing either the parsed messaged or a chain of errors
    */
  private def mt202ToFinancialMessage(file: File, message: MT202): EitherNec[ParsingError, FinancialMessage] =
    (
      FinancialMessage.SenderBic(Bic(message.orderingInstitution)).rightNec,
      FinancialMessage.ReceiverBic(Bic(message.beneficiaryInstitution)).rightNec,
      extractCurrencyAndAmount(file, message.valueDateCurrencyCodeAmount),
      None.rightNec
    ).parMapN(FinancialMessage.apply)

  private def extractCurrencyAndAmount(
      file: File,
      value: String
    ): EitherNec[ParsingError, FinancialMessage.Amount] =
    value match {
      case currencyAndAmountRegex(currency, amount) =>
        (
          Either
            .fromOption(amount.toDoubleOption, NonEmptyChain(InvalidAmount(file, amount)))
            .map(FinancialMessage.Amount.Value),
          FinancialMessage.Amount.Currency
            .withNameEither(currency)
            .leftMap(_ => NonEmptyChain(InvalidCurrency(file, currency)))
        ).parMapN(FinancialMessage.Amount.apply)
      case value => InvalidAmount(file, value).leftNec
    }
}

object MessageParser {
  implicit val parsingConfig: Configuration =
    Configuration.default.withDiscriminator("messageType")

  @ConfiguredJsonCodec sealed trait JsonMessage

  object JsonMessage {

    case class MT103(
        @JsonKey("33B") currencyInstructedAmount: String,
        @JsonKey("50A") orderingCustomer: String,
        @JsonKey("51A") sendingInstitution: String,
        @JsonKey("57A") accountWithInstitution: String,
        @JsonKey("59A") beneficiaryCustomer: String
      ) extends JsonMessage

    case class MT202(
        @JsonKey("32A") valueDateCurrencyCodeAmount: String,
        @JsonKey("52A") orderingInstitution: String,
        @JsonKey("58A") beneficiaryInstitution: String
      ) extends JsonMessage

  }
  //implicit val jsonDecoder: Decoder[JsonMessage] = deriveConfiguredDecoder

  val currencyAndAmountRegex: Regex = "([A-Z]+)([0-9.]+)".r
}
