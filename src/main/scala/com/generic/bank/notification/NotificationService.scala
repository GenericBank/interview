package com.generic.bank.notification

import cats.implicits._
import com.google.inject.Inject
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.{ExecutionContext, Future}

/**
  * Trait to implementation to send notification to an external alerting service in case of internal errors
  */
trait NotificationService {
  def notifyError(error: String): Future[Unit]
}

class DummyNotificationService @Inject() (implicit ec: ExecutionContext)
    extends NotificationService with LazyLogging {
  override def notifyError(error: String): Future[Unit] = logger.error(error).pure[Future]
}
