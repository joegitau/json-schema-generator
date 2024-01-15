package joegitau

object server extends App {
  private lazy val interface = "localhost"
  private lazy val port      = 8080

  private def startHttpServer(routes: Route)(using system: ActorSystem[_]): Unit = {
    implicit val ec: ExecutionContextExecutor = system.executionContext

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
}
