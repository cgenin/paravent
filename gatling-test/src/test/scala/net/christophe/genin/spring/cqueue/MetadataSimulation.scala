package net.christophe.genin.spring.cqueue

import scala.concurrent.duration._
import io.gatling.core.Predef._
import io.gatling.core.controller.inject.open._
import io.gatling.core.feeder.SourceFeederBuilder
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._

class MetadataSimulation extends Simulation {

	val httpProtocol = http
		.baseUrl("http://localhost:8080")
		.inferHtmlResources(BlackList(""".*\.js""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.woff2""", """.*\.(t|o)tf""", """.*\.png""", """.*detectportal\.firefox\.com.*"""), WhiteList())
		.acceptHeader("*/*")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("fr-FR,fr;q=0.9,en-US;q=0.8,en;q=0.7")
		.contentTypeHeader("application/json")
		.userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.131 Safari/537.36")

	val headers_0 = Map(
		"Origin" -> "http://localhost:8080",
		"Proxy-Connection" -> "keep-alive")

	val feeder = ssv("base.csv", '|').random // 1,

	val scn = scenario("MetadataSimulation")
  	.feed(feeder)
		.exec(http("Ajout metadata")
			.post("/api/paravent/queue/metadata/test")

			.headers(headers_0)
			//.body(RawFileBody("RecordedSimulation_0000_request.txt")))
			.body(StringBody("${BODY}")))

//	val injection: RampOpenInjection = rampUsers(10000) during  (60 second )
val injection: RampOpenInjection = rampUsers(3600) during  (30 minute )
	setUp(scn.inject(injection)).protocols(httpProtocol)
}