package com.generic.bank.stream.incoming

import akka.actor.ActorSystem
import akka.stream.testkit.scaladsl.TestSink
import cats.implicits._
import com.generic.bank.config.ApplicationConfig
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, EitherValues}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.ExecutionContext.Implicits.global

class IncomingStreamSpec
    extends AnyWordSpec with Matchers with EitherValues with BeforeAndAfterAll with ScalaFutures {
  implicit val actorSystem: ActorSystem = ActorSystem()

  override def afterAll(): Unit = actorSystem.terminate().void.futureValue

  "IncomingStream#source" should {

    "return a Source of Files" in {
      val applicationConfig = ApplicationConfig(
        ApplicationConfig.MessageFolder("/messages")
      )
      val incomingStream = new IncomingStream(applicationConfig)

      val result = incomingStream.source()

      val testSource = result.getOrElse(null).map(_.getName).runWith(TestSink.probe[String])

      testSource
        .request(6)
        .expectNextUnorderedN(
          List(
            "mt103_1.json",
            "mt103_2.json",
            "mt103_3.json",
            "mt103_4.json",
            "mt202_1.json",
            "mt202_2.json"
          )
        )
        .expectComplete()

    }

    "return an Error.DirectoryNotFound" in {
      val applicationConfig = ApplicationConfig(
        ApplicationConfig.MessageFolder("not-a-directory")
      )
      val incomingStream = new IncomingStream(applicationConfig)

      val result = incomingStream.source()

      result.swap.getOrElse(null) shouldBe Error.DirectoryNotFound

    }
  }

}
