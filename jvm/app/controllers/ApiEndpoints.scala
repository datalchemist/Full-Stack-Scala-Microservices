package controllers

import play.api.mvc.BodyParsers

import endpoints.algebra.JsonEntities
import endpoints.play.server.{ Endpoints}

import play.api.libs.json.{JsValue,Format, Json, Reads}
import play.api.mvc.{Result, BodyParser,Action, RequestHeader, Handler, Results, Request => PlayRequest}
import play.api.routing.{SimpleRouter,Router => PlayRouter}
import scala.concurrent.Future

trait PlayJsonEntities extends Endpoints with JsonEntities {

  def jsonRequest[A : Format]: RequestEntity[A] = BodyParsers.parse.json[A]
  def jsonResponse[A : Format]: Response[A] = a => Results.Ok(Json.toJson(a))

}
class ApiEndpointServer(counter: services.Counter)(implicit ec:scala.concurrent.ExecutionContext) 
extends shared.PublicEndpoints with Endpoints with PlayJsonEntities {

  val routes: PlayRouter.Routes = routesFromEndpoints(
    count.implementedBy(_ => counter.nextCount())
  )

}
object ApiEndpoints {
  
}