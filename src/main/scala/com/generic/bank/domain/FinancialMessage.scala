package com.generic.bank.domain

import cats.Show
import enumeratum.{Enum, EnumEntry}

case class FinancialMessage(
  sender: FinancialMessage.SenderBic,
  receiver: FinancialMessage.ReceiverBic,
  amount: FinancialMessage.Amount,
  isFraud: Option[Boolean] = None
)
object FinancialMessage {

  case class SenderBic(value: Bic)
  case class ReceiverBic(value: Bic)

  case class Amount(
    value: Amount.Value,
    currency: Amount.Currency
  )
  object Amount {

    case class Value(value: Double) extends AnyVal

    sealed trait Currency extends EnumEntry
    object Currency extends Enum[Currency] {
      val values: IndexedSeq[Currency] = findValues

      case object AUD extends Currency
      case object CAD extends Currency
      case object CHF extends Currency
      case object EUR extends Currency
      case object GBP extends Currency
      case object NZD extends Currency
      case object JPY extends Currency
      case object SGD extends Currency
      case object USD extends Currency
    }

  }

  implicit  val showFinancialMessage: Show[FinancialMessage] = Show.fromToString
}
