package version1

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.{Directives, Route}
import slick.jdbc.H2Profile.api._
import slick.lifted.Tag

import scala.io.StdIn

//#imports
import scala.collection.mutable.ArrayBuffer

object HttpWebServer extends Directives {

  def main(args: Array[String]): Unit = {

    // 액터시스템
    implicit val system = ActorSystem(Behaviors.empty, "my-system")

    // executionContext - needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.executionContext

    val lines = new ArrayBuffer[Any]()
    def println(s: Any) = lines += s

    // H2DB
    val db = Database.forURL("jdbc:h2:mem:test1;DB_CLOSE_DELAY=-1", driver="org.h2.Driver")

    // 핸들러
    val handler = new WaneeHandler(db)

    // get api 만들어져있고,
    val route: Route =
      path("base") {
        get {
//            Await.result(resultFuture, Duration.Inf)
            complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
          }
      } ~ path("select") {
        get {
          complete(handler.selectAll(db))
        }

      }~ path("select2") {
        get {
          complete(handler.selectByName(db, "Wanee"))
        }

      }~ path("insert") {
        get {
          complete(handler.insert(db))
        }

      }~ path("delete") {
        get {
          complete(handler.delete(db, "BigGuy"))
        }

      }~ path("update") {
        get {
          complete(handler.update(db))
        }

      }

    // 서버 시동
    val bindingFuture = Http().newServerAt("localhost", 8080).bind(route)

    println(s"Server now online. Please navigate to http://localhost:8080/hello\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

}

object ExtendsAppObject extends App {

  class Friends(tag: Tag) extends Table[(String, Int, String)](tag, "FRIENDS") {
    def name = column[String]("NAME")
    def age = column[Int]("AGE")
    def hobby = column[String]("HOBBY")
    def * = (name, age, hobby)
  }

    val friends = TableQuery[Friends]
    //#tables

    // Connect to the database and execute the following block within a session
    //#setup
    val db = Database.forConfig("h2mem1")

}