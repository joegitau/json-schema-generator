package joegitau.http

import org.apache.pekko.http.scaladsl.server.Directives.pathPrefix
import org.apache.pekko.http.scaladsl.server.{Directives, Route}

class CoreRouter(dynamicFormRouter: DynamicFormRouter)  {
  val coreRoutes: Route = pathPrefix("api") {
    Directives.concat(
      dynamicFormRouter.routes
    )
  }
}
