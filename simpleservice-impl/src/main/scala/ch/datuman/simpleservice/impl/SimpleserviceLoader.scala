package ch.datuman.simpleservice.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraPersistenceComponents
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import ch.datuman.simpleservice.api.SimpleserviceService
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.softwaremill.macwire._

class SimpleserviceLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new SimpleserviceApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new SimpleserviceApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[SimpleserviceService])
}

abstract class SimpleserviceApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with CassandraPersistenceComponents
    with LagomKafkaComponents
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[SimpleserviceService](wire[SimpleserviceServiceImpl])

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = SimpleserviceSerializerRegistry

  // Register the SimpleService persistent entity
  persistentEntityRegistry.register(wire[SimpleserviceEntity])
}
