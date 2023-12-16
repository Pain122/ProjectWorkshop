import cats.data.ReaderT
import cats.effect.{ExitCode, IO, IOApp, Resource}
import config.AppConfig
import controller.TodoController
import dao.TodoSql
import doobie.util.transactor.Transactor
import com.comcast.ip4s._
import eu.timepit.refined.string.IPv4
import com.comcast.ip4s._
import domain.RequestContext
import domain.RequestContext.ContextualIO
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import service.TodoStorage
import sttp.tapir.server.http4s.Http4sServerInterpreter
import tofu.logging.Logging

object Main extends IOApp {

  private val mainLogs =
    Logging.Make.plain[IO].byName("Main")

  override def run(args: List[String]): IO[ExitCode] =
    (for {
      _ <- Resource.eval(mainLogs.info("Starting todos service...."))
      config <- Resource.eval(AppConfig.load)
      transactor = Transactor.fromDriverManager[ContextualIO](
        config.db.driver,
        config.db.url,
        config.db.user,
        config.db.password
      )
      sql = TodoSql.make
      storage: TodoStorage[ContextualIO] = TodoStorage.make[ContextualIO](sql, transactor)
      controller: TodoController[IO] = TodoController.make(storage)
      routes = Http4sServerInterpreter[IO]().toRoutes(controller.all)
      httpApp = Router("/" -> routes).orNotFound
      _ <- EmberServerBuilder
        .default[IO]
        .withHost(
          Ipv4Address.fromString(config.server.host).getOrElse(ipv4"0.0.0.0")
        )
        .withPort(Port.fromInt(config.server.port).getOrElse(port"80"))
        .withHttpApp(httpApp)
        .build
    } yield ()).useForever.as(ExitCode.Success)
}
