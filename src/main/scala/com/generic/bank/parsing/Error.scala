package com.generic.bank.parsing

import cats.Show

sealed trait Error

object Error {
  case object FileTooLarge extends Error
  case class CannotReadFile(underlying: Throwable) extends Error
  case class InvalidJson(underlying: io.circe.Error) extends Error
  case class InvalidAmount(amountString: String) extends Error
  case class InvalidCurrency(currencyString: String) extends Error

  implicit val showError: Show[Error] = Show.show {
    case FileTooLarge => s"File is too large"
    case CannotReadFile(underlying) => s"Can't read file : ${underlying.getMessage}"
    case InvalidJson(underlying) => s"Json file is invalid: $underlying"
    case InvalidAmount(amountString) => s"Invalid amount in $amountString"
    case InvalidCurrency(currencyString) => s"Invalid currency in $currencyString"
  }
}
