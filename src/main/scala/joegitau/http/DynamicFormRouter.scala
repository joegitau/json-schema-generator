package joegitau.http

import joegitau.utils.FormHelper
import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.Directives.{complete, path}
import org.apache.pekko.http.scaladsl.server.{Directives, Route}
import play.api.libs.json.{JsObject, Json}

class DynamicFormRouter extends FormHelper {
  private lazy val publisherForm = _publisherForm
  private val publisherModel = loadValidatedModelAsJson(publisherForm)

  private def getDynamicForm(model: JsObject): Route = {
    path("model") {
      Directives.get {
        complete(StatusCodes.OK, Json.prettyPrint(model))
      }
    }
  }

  // val jsObj = Json.toJson(publisherForm).as[JsObject]
  val routes: Route = getDynamicForm(publisherModel)
}

object DynamicFormRouter {
  def apply() = new DynamicFormRouter()
}
