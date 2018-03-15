
import akka.NotUsed
import akka.stream.scaladsl.Source
import com.google.inject.AbstractModule
import controllers.MatsuriController

class Module extends AbstractModule {
  override def configure(): Unit = {

    bind(classOf[MatsuriController]).asEagerSingleton()

  }
}