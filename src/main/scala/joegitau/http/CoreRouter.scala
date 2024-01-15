package joegitau.http

import joegitau.utils.FormHelper
import org.apache.pekko.http.scaladsl.server.Directives.pathPrefix
import org.apache.pekko.http.scaladsl.server.{Directives, Route}

class CoreRouter(dynamicFormRouter: DynamicFormRouter) extends FormHelper {
  private lazy val publisherForm = _publisherForm
  private val publisherModel = loadValidatedModelAsJson(publisherForm)

  val coreRoutes: Route = pathPrefix("api") {
    Directives.concat(
      dynamicFormRouter.getDynamicForm(publisherModel)
    )
  }
}
