package joegitau.http

import org.apache.pekko.http.scaladsl.server.Directives.{complete, path}
import org.apache.pekko.http.scaladsl.server.{Directives, Route}
import play.api.libs.json.JsObject

class DynamicFormRouter {
  def getDynamicForm(model: JsObject): Route =
    path("/api/form") {
      Directives.get {
        complete {
          model.toString()
        }
      }
    }
}

object DynamicFormRouter {
  def apply() = new DynamicFormRouter()
}
