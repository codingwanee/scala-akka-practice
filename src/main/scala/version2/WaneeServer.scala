package version2

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.server.Directives
import slick.jdbc.H2Profile.api._
import slick.lifted.Tag
import spray.json._

import scala.concurrent.Future
import scala.io.StdIn

//#imports
import scala.collection.mutable.ArrayBuffer


final case class PFriends(name: String, age: Int, hobby: String)

// collect your json format instances into a support trait:
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val friendFormat = jsonFormat3(PFriends)
}



object HttpWebServer2 extends Directives with JsonSupport {

  def main(args: Array[String]): Unit = {

    // 액터시스템
    implicit val system = ActorSystem(Behaviors.empty, "my-system")

    // executionContext - needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.executionContext

    val lines = new ArrayBuffer[Any]()

    def println(s: Any) = lines += s

    // H2DB
    val db = Database.forURL("jdbc:h2:mem:test1;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

    // 핸들러
    val handler = new WaneeHandler(db)


    /**
     * Http Route
     */
    //////////////////////////////   기본 HTTP 메소드   //////////////////////////////////

    // 서버 동작하는지 간단 테스트용
    val testRoute =
      get {
        complete("This is a GET request.")
      } ~ post {
        complete("This is a POST request.")
      } ~ put {
        complete("This is a PUT request.")
      } ~ delete {
        complete("This is a DELETE request.")
      }

    // URI로 인자가 전달될 때의 여러 상황
    val withParamRoute =
      (path("get" / "param") & parameters("name")) { name =>
        complete(handler.selectByName(db, name))
      } ~ (path("get" / "paramdefault") & parameters("name".withDefault("wanee"))) { name => // 디폴트값을 갖는 경우
        complete(handler.selectByName(db, name))
      } ~ (path("get" / "paramoption") & parameters("name".optional)) { name => // 값이 없을수도 있는 경우
        val nameStr = name.getOrElse("") // 파라미터가 없을 시 조회될 기본값을 넣어줄 수도 있다.
        complete(handler.selectByName(db, nameStr))
      }

    // selectAll로 조회한 데이터를 response에 담아서 client에게 전달
    val selectRoute =
      path("select") {
        complete(handler.selectAll(db)) // 모든 행을 조회
      }

    // delete로 원하는 데이터 삭제
    val deleteRoute =
      delete {
        (path("delete") & parameters("name")) { param =>
          complete(handler.delete(db, param))
        }
      }

    // post로 json 객체 받아와서 DB에 insert
    val postRoute =
      post {
        (path("test") & entity(as[PFriends])) { friends => // will unmarshal JSON to Order
          val name = friends.name
          val age = friends.age
          val hobby = friends.hobby
          complete(s"introducing new friend: $name, $age, $hobby")
        }
      } ~ post { // 받아 온 데이터 insert
        (path("insert") & entity(as[PFriends])) { f =>
          complete(handler.postInsert(db, f))

        }
      }

    // put 메소드로 원하는 데이터 변경
    val putRoute =
      put {
        (path("put") & entity(as[PFriends])) { f =>
          complete(handler.putMethod(db, f))
        }
      }

    //////////////////////////////   기      타   //////////////////////////////////

    // 반환되는 자료가 없을 때 / 에러코드 반환 시
    val failRoute =
    path("foo") {
      failWith(new RuntimeException("Oops."))
    }

    // 입력되는 URI와 일치하면 값 반환, 일치하지 않으면 다른값 반환
    val matchHandler: PartialFunction[HttpRequest, Future[HttpResponse]] = {
      case HttpRequest(HttpMethods.GET, Uri.Path("/value"), _, _, _) =>
        Future.successful(HttpResponse(entity = "23"))
    }

    // Map형식 파라미터
    val mapRoute =
    (path("map") & parameterMap) { params =>
      def paramString(param: (String, String)): String = s"""${param._1} = '${param._2}'""" // 키, 값을 모두 URL로 결정 가능

      complete(s"The parameters are ${params.map(paramString).mkString(", ")}") // 파라미터 개수는 입력하는 대로 계속 늘려갈 수 있다.
    } ~ (path("map") & parameterMultiMap) { params =>
      complete(s"There are parameters ${params.map(x => x._1 + " -> " + x._2.size).mkString(", ")}")
    }

    // 헤더에 데이터 넣기
    val lona = RawHeader("name", "Lona")
    val julie = RawHeader("name", "Julie")

    val setHeaderRoute =
      respondWithDefaultHeader(lona) { // 아래에 해당하지 않는 기타 상황에서 헤드에 적용할 default값
        respondWithHeader(julie) {
          path("lona") { // default path로 들어갔을 때
            complete("header of lona!")
          } ~
            complete("check header") // root path로 들어갔을 때
        }
      }


    /////////////////////////////////////////////////////////////////////////////////////////////////////////////


    // 서버 시동
    val bindingFuture = Http().newServerAt("localhost", 8080).bind(testRoute) // 원하는 route의 변수명으로 실행

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

}
