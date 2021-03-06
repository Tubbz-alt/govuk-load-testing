package govuk

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class DynamicListsEmailSignup extends Simulation {
  val factor = sys.props.getOrElse("factor", "1").toFloat
  val duration = sys.props.getOrElse("duration", "0").toInt

  val paths = csv(dataDir + java.io.File.separatorChar + "get-ready-brexit-check-email-signup_paths.csv").readRecords

  val scale = factor / workers

  val scn =
    scenario("DynamicListsEmailSignup")
      .feed(cachebuster)
      .foreach(paths, "path") {
        exec(flattenMapIntoAttributes("${path}"))
          .repeat(session => math.ceil(session("hits").as[Int] * scale).toInt, "hit") {
            exec(
              get("${base_path}", "${cachebust}-${hit}")
                .check(
                  css("#checklist-email-signup", "action").saveAs("subscribeFormAction"),
                  css("#checklist-email-signup input[name=authenticity_token]", "value").saveAs("emailAlertFrontendAuthToken")
                )
                .check(status.is(200))
            )
              .exec(
                http("POST Subscribe")
                  .post("""${subscribeFormAction}""")
                  .formParam("authenticity_token", """${emailAlertFrontendAuthToken}""")
                  .check(
                    css(".checklist-email-signup", "action").saveAs("emailAlertFrontendUrl"),
                    css(".checklist-email-signup input[name=topic_id]", "value").saveAs("emailAlertFrontendTopicId"),
                    css(".checklist-email-signup input[name=authenticity_token]", "value").saveAs("emailAlertFrontendAuthToken")
                  )
                  .check(status.is(200))
              )
              .exec(
                http("POST Frequency")
                  .post("""${emailAlertFrontendUrl}""")
                  .formParam("authenticity_token", """${emailAlertFrontendAuthToken}""")
                  .formParam("topic_id", """${emailAlertFrontendTopicId}""")
                  .formParam("frequency", "immediately")
                  .check(
                    css(".checklist-email-signup", "action").saveAs("emailAlertFrontendEmailAddressUrl"),
                    css(".checklist-email-signup input[name=topic_id]", "value").saveAs("emailAlertFrontendTopicId"),
                    css(".checklist-email-signup input[name=authenticity_token]", "value").saveAs("emailAlertFrontendAuthToken")
                  )
                  .check(status.is(200))
              )
              .exec(
                http("POST Email address")
                  .post("""${emailAlertFrontendEmailAddressUrl}""")
                  .formParam("authenticity_token", """${emailAlertFrontendAuthToken}""")
                  .formParam("topic_id", """${emailAlertFrontendTopicId}""")
                  .formParam("frequency", "immediately")
                  .formParam("address", "alice@example.com")
                  .check(
                    css(".govuk-panel__title")
                  )
                  .check(status.is(200))
              )
          }
      }

  val scnWithDuration =
    scenario("DynamicListsEmailSignup")
      .during(duration, "Soak test") {
        feed(cachebuster)
        .foreach(paths, "path") {
          exec(flattenMapIntoAttributes("${path}"))
            .repeat(session => math.ceil(session("hits").as[Int] * scale).toInt, "hit") {
              exec(
                get("${base_path}", "${cachebust}-${hit}")
                  .check(
                    css("#checklist-email-signup", "action").saveAs("subscribeFormAction"),
                    css("#checklist-email-signup input[name=authenticity_token]", "value").saveAs("emailAlertFrontendAuthToken")
                  )
                  .check(status.is(200))
              )
              .exec(
                http("POST Subscribe")
                  .post("""${subscribeFormAction}""")
                  .formParam("authenticity_token", """${emailAlertFrontendAuthToken}""")
                  .check(
                    css(".checklist-email-signup", "action").saveAs("emailAlertFrontendUrl"),
                    css(".checklist-email-signup input[name=topic_id]", "value").saveAs("emailAlertFrontendTopicId"),
                    css(".checklist-email-signup input[name=authenticity_token]", "value").saveAs("emailAlertFrontendAuthToken")
                  )
                  .check(status.is(200))
              )
              .exec(
                http("POST Frequency")
                  .post("""${emailAlertFrontendUrl}""")
                  .formParam("authenticity_token", """${emailAlertFrontendAuthToken}""")
                  .formParam("topic_id", """${emailAlertFrontendTopicId}""")
                  .formParam("frequency", "immediately")
                  .check(
                    css(".checklist-email-signup", "action").saveAs("emailAlertFrontendEmailAddressUrl"),
                    css(".checklist-email-signup input[name=topic_id]", "value").saveAs("emailAlertFrontendTopicId"),
                    css(".checklist-email-signup input[name=authenticity_token]", "value").saveAs("emailAlertFrontendAuthToken")
                  )
                  .check(status.is(200))
              )
              .exec(
                http("POST Email address")
                  .post("""${emailAlertFrontendEmailAddressUrl}""")
                  .formParam("authenticity_token", """${emailAlertFrontendAuthToken}""")
                  .formParam("topic_id", """${emailAlertFrontendTopicId}""")
                  .formParam("frequency", "immediately")
                  .formParam("address", "alice@example.com")
                  .check(
                    css(".govuk-panel__title")
                  )
                  .check(status.is(200))
              )
            }
        }
      }

  if (duration == 0) run(scn) else run(scnWithDuration)
}
