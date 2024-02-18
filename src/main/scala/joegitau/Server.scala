package joegitau

import joegitau.http.{CoreRouter, DynamicFormRouter, JsonSchemaFormRouter}
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.server.Route

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object Server extends App {
  private lazy val interface = "localhost"
  private lazy val port      = 8080

  implicit val system: ActorSystem = ActorSystem("generic-playground")

  private def startHttpServer(routes: Route)(implicit system: ActorSystem): Unit = {
    implicit val ec: ExecutionContextExecutor = system.dispatcher

    val serverBinding = Http().newServerAt(interface, port).bind(routes)
    serverBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info(s"Server running at http://${address.getHostString}:${address.getPort}/")
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }

  // Create instances of routers
  private val dynamicFormRouter    = DynamicFormRouter()
  private val jsonSchemaFormRouter = JsonSchemaFormRouter()

  private val coreRouter = new CoreRouter(dynamicFormRouter, jsonSchemaFormRouter)

  // Start the server with CoreRouter's routes
  startHttpServer(coreRouter.coreRoutes)
}
