package com.generic.bank.fraud.client

import cats.Show
import com.generic.bank.fraud.api.{Error => ApiError}

sealed trait Error

object Error {
  case class ApiInternalError(underlying: ApiError) extends Error
  case class ApiCallFailure(underlying: Throwable) extends Error

  implicit val showError: Show[Error] = Show.show {
    case ApiInternalError(underlying) => s"Error while calling fraud api: $underlying"
    case ApiCallFailure(underlying) => s"Exception while calling fraud api: ${underlying.getMessage}"
  }
}

