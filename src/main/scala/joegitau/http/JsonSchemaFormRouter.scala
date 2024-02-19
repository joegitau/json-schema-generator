package joegitau.http

import joegitau.utils.SchemaFormHelper
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.Directives.{complete, path}
import org.apache.pekko.http.scaladsl.server.{Directives, Route}
import play.api.libs.json.{JsObject, Json}

class JsonSchemaFormRouter extends SchemaFormHelper {
  // private val schema = loadSchemaAsJson(userSchema)

  private def getJsonSchemaForm(model: JsObject): Route = {
    path("schema") {
     Directives.get {
        complete((StatusCodes.OK, Json.prettyPrint(model)))
      }
    }
  }

  val routes: Route = getJsonSchemaForm(schemaAsJsObject)
}

object JsonSchemaFormRouter {
  def apply() = new JsonSchemaFormRouter()
}
