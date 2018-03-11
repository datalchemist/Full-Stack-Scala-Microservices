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

import endpoints.xhr

//public endpoint served with xhr
object PublicEndpoints extends DefaultXhrEndpoint with shared.PublicEndpoints

object ScalaJSExample {

  @JSExportTopLevel("ScalaJSExample")
  protected def getInstance(): this.type = this


  def countRequest(data: Var[String]) = {
    PublicEndpoints.count(()).foreach(v => data.value = s"$v COUNT")
  }
  def helloRequest(data: Var[String]) = {
    PublicEndpoints.hello(data.value).foreach(v => data.value = s"$v")
  }

  @dom
  def render = {
    val data = Var("")
    countRequest(data) // initial population
    <div class="container-fluid">
  		<div class="row">
    		<div class="col">
      		1 of 3
    		</div>
    		<div class="col-6">
      		<button onclick={event: Event => countRequest(data) }>
        		Call Play service
      		</button>
      		<button onclick={event: Event => helloRequest(data) }>
        		Call lagom service
      		</button>
    		</div>
    		<div class="col">
      		From Play: The server has been booped { data.bind } times. Shared Message: {shared.SharedMessages.itWorks}.
    		</div>
		  </div>
    </div>
  }

    console.log("Init")
  @JSExport
  def main(args: Array[String]): Unit ={
    console.log("Started")
    dom.render(document.body, render)
  }
}
