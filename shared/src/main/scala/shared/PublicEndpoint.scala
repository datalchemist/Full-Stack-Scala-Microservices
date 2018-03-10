package shared
import endpoints.algebra.{Decoder,Encoder,JsonEntities, Endpoints}

import play.api.libs.json.{JsValue,Format, Json, Reads, Writes}

trait PublicEndpoints extends PlayJsonEntities {
  val count=        endpoint(get(path / "api" / "count"), jsonResponse[Int])
}
  
object SharedMessages {
  def itWorks = "It works!"
}
