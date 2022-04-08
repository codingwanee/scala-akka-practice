package version1

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives
import version1.ExtendsAppObject.Friends
import slick.dbio.Effect
import slick.jdbc.H2Profile.api._
import slick.sql.{FixedSqlAction, FixedSqlStreamingAction}

import scala.concurrent.{ExecutionContext, Future}

// Controller에 호출되어 서비스 실행
class WaneeHandler(db: Database) extends Directives {
  // 테이블
  val friends = TableQuery[Friends]

  // 테이블 스키마, 샘플데이터 생성
  val setup = DBIO.seq(
    // 테이블 스키마
    friends.schema.create,

    // 샘플데이터
    friends += ("Wanee", 32, "riding bike"),
    friends += ("Fanee", 32, "driving"),
    friends += ("BigGuy", 32, "horse riding")
  )

  // 실행
  val setupFuture = db.run(setup)


  /**
   * Slick으로 만들어 본 것
   */
  def selectAll(db: Database)(implicit ec: ExecutionContext): Future[HttpResponse] = Future {

    val q = friends.map(_.name)
    val action: FixedSqlStreamingAction[Seq[String], String, Effect.Read] = q.result
    val results: Future[Seq[String]] = db.run(action)
    results.foreach(println)

    HttpResponse(
      StatusCodes.OK, // status code
      Nil, // headers
      HttpEntity(ContentType.WithCharset(MediaTypes.`text/plain`, HttpCharsets.`UTF-8`), "selectAll 실행") // entity
      //, HttpProtocols.`HTTP/1.1` // protocol
    )

  }


  // select문 - 이름으로 조회
  def selectByName(db: Database, name: String)(implicit ec: ExecutionContext): Future[HttpResponse] = Future {
    val q = for {
      c <- friends if c.name === "Wanee"
    } yield (c.name)

    val action = q.result

    val results: Future[Seq[String]] = db.run(action)
    results.foreach(println)

    HttpResponse(
      StatusCodes.OK, // status code
      Nil, // headers
      HttpEntity(ContentType.WithCharset(MediaTypes.`text/plain`, HttpCharsets.`UTF-8`), "select문 실행") // entity
      //, HttpProtocols.`HTTP/1.1` // protocol
    )

  }

  // update문
  def update(db: Database)(implicit ec: ExecutionContext) = Future {
    val q = for {a <- friends if a.name === "BigGuy"} yield a.age
    val action = q.update(999)
    val sql = q.updateStatement

    val sq = friends.map(_.name)
    val saction = sq.result
    val results: Future[Seq[String]] = db.run(saction)
    results.foreach(println)

    // println("sql is like : " + sql)

    HttpResponse(
      StatusCodes.OK, // status code
      Nil, // headers
      HttpEntity(ContentType.WithCharset(MediaTypes.`text/plain`, HttpCharsets.`UTF-8`), "update 실행") // entity
      //, HttpProtocols.`HTTP/1.1` // protocol
    )

  }


  // insert문
  def insert(db: Database)(implicit ec: ExecutionContext) = Future {

    val action: DBIOAction[Unit, NoStream, Effect.Write] = DBIO.seq(
      friends += ("화니", 30, "노래부르기")
    )

    db.run(action)

    val sq = friends.map(_.name)
    val saction = sq.result
    val results: Future[Seq[String]] = db.run(saction)
    results.foreach(println)

    HttpResponse(
      StatusCodes.OK, // status code
      Nil, // headers
      HttpEntity(ContentType.WithCharset(MediaTypes.`text/plain`, HttpCharsets.`UTF-8`), "insert문 실행") // entity
      //, HttpProtocols.`HTTP/1.1` // protocol
    )

  }

  // delete문
  def delete(db: Database, a: String)(implicit ec: ExecutionContext): Future[HttpResponse] = Future {

    val q = friends.filter(_.name === a)
    val action: FixedSqlAction[Int, NoStream, Effect.Write] = q.delete
    db.run(action)
    //    val count: Future[Int] = db.run(action)
    //    val sql = action.statements

    val sq = friends.map(_.name)
    val saction = sq.result
    val results: Future[Seq[String]] = db.run(saction)
    results.foreach(println)

    HttpResponse(
      StatusCodes.OK, // status code
      Nil, // headers
      HttpEntity(ContentType.WithCharset(MediaTypes.`text/plain`, HttpCharsets.`UTF-8`), "delete문 실행") // entity
      //, HttpProtocols.`HTTP/1.1` // protocol
    )

  }

}
