
import play.api.{ApplicationLoader, BuiltInComponentsFromContext, Mode}
import play.api.ApplicationLoader.Context
import play.api.i18n.I18nComponents
//import play.api.libs.ws.ahc.AhcWSComponents
import play.api.mvc.{BodyParsers,ControllerComponents,DefaultCookieHeaderEncoding}
import play.filters.HttpFiltersComponents
import play.filters.https.{RedirectHttpsConfiguration,RedirectHttpsFilter}
import com.softwaremill.macwire._
import controllers.{ApiEndpointServer,AssetsComponents,CountController,Application}
import router.{Routes => BaseRoutes}

import scala.collection.immutable
import scala.concurrent.ExecutionContext

import endpoints.algebra.JsonEntities
import endpoints.play.server.{ Endpoints}

import play.api.libs.json.{JsValue,Format, Json, Reads}
import play.api.mvc.{Result, BodyParser,Action, RequestHeader, Handler, Results, Request => PlayRequest}
import play.api.routing.{SimpleRouter,Router => PlayRouter}
import scala.concurrent.Future

class WebApp(context: Context) extends BuiltInComponentsFromContext(context)
  with I18nComponents
//  with AhcWSComponents
//  with LagomServiceClientComponents
//  with LagomConfigComponent
  with AssetsComponents 
  with HttpFiltersComponents{
  
//  override lazy val serviceInfo: ServiceInfo = ServiceInfo(
//    "bcucq-web-server",
//    Map(
////      "bcucq-web-server" -> immutable.Seq(ServiceAcl.forPathRegex("(?!/api/).*"))
//      "bcucq-web-server" -> immutable.Seq(ServiceAcl.forPathRegex("(/web|/assets|/api-gw|/api-endpoint).*"))
//    )
//  )
//  override implicit lazy val executionContext: ExecutionContext = actorSystem.dispatcher
  lazy val ep:ApiEndpointServer=  wire[ApiEndpointServer]
  lazy val baseRouter : PlayRouter  = {
    val prefix = "/"
    wire[BaseRoutes]
  }
  override lazy val router =  {//: PartialFunction[RequestHeader, Handler]= {

//    val rr=  //.routes.orElse(TestEndPointServer.routes)
    new SimpleRouter {
      def routes = 
        baseRouter.routes
        .orElse(
            ep.routes
        )
      override def documentation=baseRouter.documentation
    }
  }

//  lazy val almaGateway  = serviceClient.implement[ch.datuman.almagateway.api.AlmagatewayService]
  
  lazy val clock = java.time.Clock.systemDefaultZone
  
  lazy val counterService = wire[services.AtomicCounter]
  
  lazy val main = wire[Application]
  lazy val countCtrl = wire[CountController]
}

class WebAppLoader extends ApplicationLoader {
  override def load(context: Context) = new WebApp(context).application 
//  context.environment.mode match {
//    case Mode.Dev =>
//      new WebApp(context).application //with LagomDevModeComponents {}.application
//    case _ =>
//      new BCUcqWebUI(context) 
//      with ConfigurationServiceLocatorComponents {
//        lazy val redirectFilter = new RedirectHttpsFilter(new RedirectHttpsConfiguration())
//        override def httpFilters = {
//          super.httpFilters :+ redirectFilter
//        }
////        override lazy val serviceLocator :ServiceLocator = NoServiceLocator
//      }.application
//  }
}