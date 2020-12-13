package com.generic.bank.parsing

import cats.Show

import java.io.File

sealed trait Error {
  val file: File
}

object Error {
  case class FileTooLarge(file: File) extends Error
  case class CannotReadFile(file: File, underlying: Throwable) extends Error
  case class InvalidJson(file: File, underlying: io.circe.Error) extends Error
  case class InvalidAmount(file: File, amountString: String) extends Error
  case class InvalidCurrency(file: File, currencyString: String) extends Error

  implicit val showError: Show[Error] = Show.show {
    case FileTooLarge(file) => s"File ${file.getName} is too large"
    case CannotReadFile(file, underlying) => s"Can't read file ${file.getName} : ${underlying.getMessage}"
    case InvalidJson(file, underlying) => s"Json file ${file.getName} is invalid: $underlying"
    case InvalidAmount(file, amountString) => s"Invalid amount in file ${file.getName} in $amountString"
    case InvalidCurrency(file, currencyString) => s"Invalid currency in file ${file.getName} in $currencyString"
  }
}
