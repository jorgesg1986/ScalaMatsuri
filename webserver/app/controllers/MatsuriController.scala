package controllers

import javax.inject.{Inject, Singleton}

import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, Subscription, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Source}
import com.jorgesg1986.matsuri.model.Tweet
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.logging.log4j.{LogManager, Logger}
import play.api.http.MimeTypes
import play.api.inject.ApplicationLifecycle
import play.api.libs.EventSource
import play.api.mvc._


@Singleton
class MatsuriController @Inject()(val controllerComponents: ControllerComponents, val lifecycle: ApplicationLifecycle) extends BaseController {

  val logger: Logger = LogManager.getLogger(classOf[MatsuriController])

  implicit val system: ActorSystem = ActorSystem("reactive-tweets")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  def index = Action {
    logger.info("GET / => Showing index.html")
    Ok(views.html.index("Your new application is ready."))
  }

  def stream = Action { implicit req =>

    logger.info("GET /stream => Retrieving stream from socket...")

    val (settings, subscription) = Functions.createSettingsSubscription(system)

    val source: Source[ConsumerRecord[String, Tweet], Consumer.Control] = Consumer
      .plainSource(settings, subscription)

    val flow: Flow[ConsumerRecord[String, Tweet], String, NotUsed] = Flow
      .fromFunction[ConsumerRecord[String, Tweet], String]{ cr  =>
      logger.info(s"Read message. Value: ${cr.value()}")
      cr.value().toString
    }

    val result: Source[String, Consumer.Control] = source.via(flow)


    Ok.chunked(result.via(EventSource.flow)).as(MimeTypes.EVENT_STREAM)
  }

}

object Functions {

  def createSettingsSubscription(system: ActorSystem): (ConsumerSettings[String, Tweet], Subscription) = {

    val keyDeserializer: StringDeserializer = new StringDeserializer()

    val valueDeserializer = new AvroDeserializer[Tweet]()

    val settings: ConsumerSettings[String, Tweet] = ConsumerSettings(system, keyDeserializer, valueDeserializer)
      .withBootstrapServers("localhost:9092")
      .withGroupId("play-server")
      .withProperty("auto.offset.reset", "earliest")

    val subscription = Subscriptions.topics(Set("matsuri"))

    (settings, subscription)

  }

}


