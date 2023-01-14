package org.scalastr.client

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws._
import akka.http.scaladsl.settings._
import akka.stream.OverflowStrategy
import akka.stream.scaladsl._
import grizzled.slf4j.Logging
import org.bitcoins.core.util.StartStopAsync
import org.bitcoins.tor.{Socks5ClientTransport, Socks5ProxyParams}
import org.scalastr.core._
import play.api.libs.json._

import java.net.URI
import scala.concurrent._
import scala.util._

abstract class NostrClient(
    val url: String,
    proxyParamsOpt: Option[Socks5ProxyParams])(implicit val system: ActorSystem)
    extends StartStopAsync[Unit]
    with Logging {
  implicit val ec: ExecutionContext = system.dispatcher

  private val http = Http(system)

  private var (queue, source) = Source
    .queue[Message](bufferSize = 10,
                    OverflowStrategy.backpressure,
                    maxConcurrentOffers = 2)
    .toMat(BroadcastHub.sink)(Keep.both)
    .run()

  private def setNewQueueAndSource(): Unit = {
    val (newQueue, newSource) = Source
      .queue[Message](bufferSize = 10,
                      OverflowStrategy.backpressure,
                      maxConcurrentOffers = 2)
      .toMat(BroadcastHub.sink)(Keep.both)
      .run()
    queue = newQueue
    source = newSource
  }

  private var subscriptionQueue: Option[(SourceQueueWithComplete[Message],
                                         Promise[Unit])] = None

  private def createHttpConnectionPoolSettings(): ConnectionPoolSettings = {
    Socks5ClientTransport.createConnectionPoolSettings(new URI(url),
                                                       proxyParamsOpt)
  }

  protected def processEvent(
      subscriptionId: String,
      event: NostrEvent): Future[Unit]

  protected def processNotice(notice: String): Future[Unit]

  def isStarted(): Boolean = subscriptionQueue.isDefined

  def shutdownPOpt: Option[Promise[Unit]] = subscriptionQueue.map(_._2)

  def publishEvent(event: NostrEvent): Future[Unit] = {
    require(isStarted(), "Need to start nostr client first")
    val json = Json.toJson(event)
    val message = JsArray(Seq(JsString("EVENT"), json))
    queue.offer(TextMessage(message.toString)).map(_ => ())
  }

  def subscribe(filter: NostrFilter): Future[String] = {
    require(isStarted(), "Need to start nostr client first")

    val id = java.util.UUID.randomUUID().toString

    val json = Json.toJson(filter)
    val message = JsArray(Seq(JsString("REQ"), JsString(id), json))
    queue.offer(TextMessage(message.toString)).map(_ => id)
  }

  def unsubscribe(id: String): Future[Unit] = {
    require(isStarted(), "Need to start nostr client first")

    val json = JsArray(Seq(JsString("CLOSE"), JsString(id)))
    queue.offer(TextMessage(json.toString)).map(_ => ())
  }

  override def start(): Future[Unit] = {
    require(subscriptionQueue.isEmpty, "Already started")
    setNewQueueAndSource()

    val sink = Sink.foreachAsync[Message](5) {
      case TextMessage.Strict(text) =>
        val jsArray = Try {
          Json.parse(text).as[JsArray].value.toVector
        }.getOrElse {
          throw new RuntimeException(s"Could not parse json: $text")
        }

        if (jsArray.nonEmpty) {
          val headStr = jsArray.head.as[String].toUpperCase
          val remaining = jsArray.tail

          headStr match {
            case "NOTICE" =>
              val notice = remaining.head.as[String]
              processNotice(notice)
            case "EVENT" =>
              val subscriptionId = remaining.head.as[String]
              val event = remaining.tail.head.as[NostrEvent]
              if (event.verify)
                processEvent(subscriptionId, event)
              else {
                logger.warn(s"Invalid signature for event: ${event.id.hex}")
                Future.unit
              }
            case "OK"   => Future.unit
            case "EOSE" => Future.unit
            case str =>
              logger.warn(s"Unknown message type: $str")
              Future.unit
          }
        } else {
          logger.warn(s"Received empty json array: $text")
          Future.unit
        }
      case streamed: TextMessage.Streamed =>
        streamed.textStream.runWith(Sink.ignore)
        Future.unit
      case bm: BinaryMessage =>
        bm.dataStream.runWith(Sink.ignore)
        logger.warn("Received unexpected message")
        Future.unit
    }

    val shutdownP = Promise[Unit]()

    val flow = Flow.fromSinkAndSourceMat(sink, source)(Keep.left)
    val wsFlow = flow.watchTermination() { (_, termination) =>
      termination.onComplete { _ =>
        shutdownP.success(())
        subscriptionQueue = None
      }
    }

    val httpConnectionPoolSettings = createHttpConnectionPoolSettings()

    val (upgradeResponse, _) =
      http.singleWebSocketRequest(
        WebSocketRequest(url),
        wsFlow,
        settings = httpConnectionPoolSettings.connectionSettings)
    subscriptionQueue = Some((queue, shutdownP))

    upgradeResponse.map {
      case _: ValidUpgrade => ()
      case InvalidUpgradeResponse(response, cause) =>
        throw new RuntimeException(
          s"Connection failed ${response.status}: $cause")
    }
  }

  override def stop(): Future[Unit] = {
    subscriptionQueue match {
      case Some((queue, closedP)) =>
        queue.complete()
        subscriptionQueue = None
        closedP.future
      case None => Future.unit
    }
  }
}
