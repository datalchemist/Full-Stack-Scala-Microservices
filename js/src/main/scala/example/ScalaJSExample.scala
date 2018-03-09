package example

import com.thoughtworks.binding.Binding.Var
import com.thoughtworks.binding.{Binding, dom}
import org.scalajs.dom.document
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.raw.{Event, HTMLElement}
import org.scalajs.dom.console

import scala.scalajs.js.annotation._
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSON

import org.scalajs.dom.raw.XMLHttpRequest
import play.api.libs.json.{Format,Json}
import endpoints.xhr
import endpoints.algebra
import endpoints.algebra.{Decoder, Encoder, MuxRequest}

import scala.scalajs.js

/**
  * Implements [[algebra.CirceEntities]] for [[Endpoints]].
  */
trait JsonEntities extends algebra.Endpoints with algebra.JsonEntities {

//  /** Builds a request entity by using the supplied codec */
//  def jsonRequest[A : CirceCodec] = (a: A, xhr: XMLHttpRequest) => {
//    xhr.setRequestHeader("Content-Type", "application/json")
//    CirceCodec[A].encoder.apply(a).noSpaces
//  }
//
//  /** Decodes the response entity by using the supplied codec */
//  def jsonResponse[A : CirceCodec]: js.Function1[XMLHttpRequest, Either[Exception, A]] =
//    xhr => parser.parse(xhr.responseText).right.flatMap(CirceCodec[A].decoder.decodeJson _)

  def jsonRequest[A : Format]: js.Function2[A, XMLHttpRequest, js.Any] = 
    (a: A, xhr: XMLHttpRequest) => {
      xhr.setRequestHeader("Content-Type", "application/json")
      Json.toJson(a).toString : js.Any
    }
  def jsonResponse[A : Format]: js.Function1[XMLHttpRequest, Either[Exception, A]] =
    xhr => 
      scala.util.Try{Json.parse(xhr.responseText)}
      .toEither.left.map(t => new Exception(t.getMessage))
      .flatMap(v => Json.fromJson[A](v).fold(err => Left(new Exception(s"Json read failure: $err")),v => Right(v)))
      

}
object PublicEndpoints
  extends shared.TestEndpoints
    with xhr.future.Endpoints
    with JsonEntities
    with xhr.OptionalResponses {
  class MuxEndpoint[Req <: MuxRequest, Resp, Transport](
    request: Request[Transport],
    response: Response[Transport]
  ) {
    def apply(
      req: Req
    )(implicit
      encoder: Encoder[Req, Transport],
      decoder: Decoder[Transport, Resp]
    ): scala.concurrent.Future[req.Response] = {
      val promise = scala.concurrent.Promise[req.Response]()
//      ((resolve, error) => {
      muxPerformXhr(request, response, req)(
        _.fold(exn => promise.failure(exn), resp => promise.success(resp)),
        xhr => promise.failure(new Exception(xhr.responseText))
      )
//      })
      promise.future
    }
  }

  def muxEndpoint[Req <: MuxRequest, Resp, Transport](
    request: Request[Transport],
    response: Response[Transport]
  ): MuxEndpoint[Req, Resp, Transport] =
    new MuxEndpoint[Req, Resp, Transport](request, response)

}

object ScalaJSExample {

  @JSExportTopLevel("ScalaJSExample")
  protected def getInstance(): this.type = this


  /**
    * Ajax Request to server, updates data state with number
    * of requests to count.
    * @param data
    */
  def countRequest(data: Var[String]) = {
    PublicEndpoints.count().foreach(v => data.value = s"$v COUNT")
  }

  @dom
  def render = {
    val data = Var("")
    countRequest(data) // initial population
    <div>
      <button onclick={event: Event => countRequest(data) }>
        Boop
      </button>
      From Play: The server has been booped { data.bind } times. Shared Message: {shared.SharedMessages.itWorks}.
    </div>
  }

    console.log("Init")
  @JSExport
  def main(args: Array[String]): Unit ={
    console.log("Started")
    dom.render(document.body, render)
  }
}
