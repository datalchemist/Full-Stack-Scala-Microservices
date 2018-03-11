package example

import org.scalajs.dom.raw.XMLHttpRequest
import play.api.libs.json.{Format,Json}
import endpoints.xhr
import endpoints.algebra

import scala.scalajs.js
import endpoints.algebra.{Decoder, Encoder, MuxRequest}

/**
  * Implements [[algebra.CirceEntities]] for [[Endpoints]].
  */
trait JsonEntities extends algebra.Endpoints with algebra.JsonEntities {
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

trait DefaultXhrEndpoint extends xhr.future.Endpoints
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