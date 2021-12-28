import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.http.Predef._

object RandomGenerator {
  def randomString(length: Int): String = {
    val SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
    val salt = new StringBuilder
    val rnd = new scala.util.Random
    while (salt.length < length) { // length of the random string.
      val index = (rnd.nextFloat() * SALTCHARS.length).asInstanceOf[Int]
      salt.append(SALTCHARS.charAt(index))
    }
    val saltStr = salt.toString
    saltStr
  }

  def randomEmail(): String = randomString(10) + "@gmail.com"

  def randomUserRequest() : String = """{"post_id":42,
                                   |"name":"""".stripMargin + RandomGenerator.randomString(25) + """",
                                   |"email":"""".stripMargin + RandomGenerator.randomEmail() + """",
                                   |"body":"""".stripMargin + RandomGenerator.randomString(25) + """"}""".stripMargin
}

class UserSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("https://gorest.co.in")
    .authorizationHeader("Bearer 496c7a7093e61a800cdccb6cc369a2cdce09e1e62f5fba1a7f1a543764fe5c19")


  val post = scenario("Post comment")
    .exec(sessionPost => {
      val sessionPostUpdate = sessionPost.set("postrequest", RandomGenerator.randomUserRequest())
      sessionPostUpdate
    })
    .exec(
      http("Post comment")
        .post("/public/v1/comments")
        .body(StringBody("${postrequest}")).asJson
    )

  val get = scenario("Get comment")
    .exec(sessionPost => {
      val sessionPostUpdate = sessionPost.set("postrequest", RandomGenerator.randomUserRequest())
      sessionPostUpdate
    })
    .exec(
      http("Post comment")
        .post("/public/v1/comments")
        .body(StringBody("${postrequest}")).asJson
        .check(jsonPath("$.data.id").saveAs("commentId"))
    )
    .exitHereIfFailed
    .exec(
      http("Get comment")
        .get("/public/v1/comments/${commentId}")
    )

  val put = scenario("Put comment")
    .exec(sessionPost => {
      val sessionPostUpdate = sessionPost.set("postrequest", RandomGenerator.randomUserRequest())
      sessionPostUpdate
    })
    .exec(
      http("Post comment")
        .post("/public/v1/comments")
        .body(StringBody("${postrequest}")).asJson
        .check(jsonPath("$.data.id").saveAs("commentId"))
    )
    .exitHereIfFailed
    .exec(sessionPut => {
      val sessionPutUpdate = sessionPut.set("putrequest", RandomGenerator.randomUserRequest())
      sessionPutUpdate
    })
    .exec(
      http("Put comment")
        .put("/public/v1/comments/${commentId}")
        .body(StringBody("${putrequest}")).asJson
    )

  val delete = scenario("Delete comment")
    .exec(sessionPost => {
      val sessionPostUpdate = sessionPost.set("postrequest", RandomGenerator.randomUserRequest())
      sessionPostUpdate
    })
    .exec(
      http("Post comment")
        .post("/public/v1/comments")
        .body(StringBody("${postrequest}")).asJson
        .check(jsonPath("$.data.id").saveAs("commentId"))
    )
    .exitHereIfFailed
    .exec(sessionPut => {
      val sessionPutUpdate = sessionPut.set("putrequest", RandomGenerator.randomUserRequest())
      sessionPutUpdate
    })
    .exec(
      http("Delete comment")
        .delete("/public/v1/users/${commentId}")
    )

  setUp(post.inject(rampUsers(15).during(10.seconds)).protocols(httpProtocol),
    get.inject(rampUsers(15).during(10.seconds)).protocols(httpProtocol),
    put.inject(rampUsers(15).during(10.seconds)).protocols(httpProtocol),
    delete.inject(rampUsers(15).during(10.seconds)).protocols(httpProtocol))
}
