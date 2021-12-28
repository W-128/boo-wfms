/*
 * Copyright 2011-2021 GatlingCorp (https://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package computerdatabase

import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._


class startProcessInstanceAndCompleteWithdelayDiffTanent extends Simulation {

  val serverHost = "http://222.200.180.59:10238"
  val localHost = "http://localhost:10237"
  var startQueryserverHost = "http://222.200.180.59:10239"
  val httpProtocol = http
    // Here is the root for all relative URLs
    .baseUrl(serverHost)
    // Here are the common headers
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0")

  val processDefinitionKey = "online-shopping-five-task"

  object Query {

    val tanentAquery = exec(
      http("getCurrentSingleTask")
        .get(startQueryserverHost+"/activiti-engine-query-start/getCurrentSingleTask/${tanentA-processInstanceId}")
        .check(status.is(200))
        .check(jsonPath("$.taskId").saveAs("tanentA-taskId"))
    )
    val tanentBquery = exec(
      http("getCurrentSingleTask")
        .get(startQueryserverHost+"/activiti-engine-query-start/getCurrentSingleTask/${tanentB-processInstanceId}")
        .check(status.is(200))
        .check(jsonPath("$.taskId").saveAs("tanentB-taskId"))
    )
    val tanentAisEnded = exec(
      http("isEnded")
        .get(startQueryserverHost+"/activiti-engine-query-start/isEnded/${tanentA-processInstanceId}")
        .check(status.is(200))
        .check(jsonPath("$.isEnded").saveAs("tanentA-isEnded"))
    )

    val tanentBisEnded = exec(
      http("isEnded")
        .get(startQueryserverHost+"/activiti-engine-query-start/isEnded/${tanentB-processInstanceId}")
        .check(status.is(200))
        .check(jsonPath("$.isEnded").saveAs("tanentB-isEnded"))
    )
  }

  object CompleteTask {

    val tanentAcomplete = tryMax(100){
      exec(
        http("completeTaskWithDelay-tanentA-rar200-rtl0")
          .post("/activiti-process-execution/completeTaskWithDelay/${tanentA-processDefinitionId}/${tanentA-processInstanceId}/${tanentA-taskId}?rar=200&rtl=0")
          .check(status.is(200))
      )
    }.exitHereIfFailed

    val tanentBcomplete = tryMax(100){
      exec(
        http("completeTaskWithDelay-tanentB-rar200-rtl5")
          .post("/activiti-process-execution/completeTaskWithDelay/${tanentB-processDefinitionId}/${tanentB-processInstanceId}/${tanentB-taskId}?rar=200&rtl=5")
          .check(status.is(200))
      )
    }.exitHereIfFailed
  }

  val tanentA = scenario("TanentA")
    .exec(
      http("startProcessInstanceByKey")
        .post(startQueryserverHost+"/activiti-engine-query-start/startProcessInstanceByKey/online-shopping-five-task")
        .check(status.is(200))
        .check(jsonPath("$.processInstanceId").saveAs("tanentA-processInstanceId"))
        .check(jsonPath("$.processDefinitionId").saveAs("tanentA-processDefinitionId"))
    )
    .exec(session => {
      session.set("tanentA-previousTaskId",-1)
    })
    .exec(session => {
      session.set("tanentA-isEnded", 0)
    })
    .doWhile(session => session("tanentA-isEnded").as[String].equals("0")) {
      // exec {
      //   session =>
      //     println("tanentA-isEnded:"+session("tanentA-isEnded").as[String])
      //     println("tanentA-previousTaskId:"+session("tanentA-previousTaskId").as[String])
      //     session
      // }
    exec(Query.tanentAquery)
    // .exec {
    //   session =>
    //    println("tanentA-taskId:"+session("tanentA-taskId").as[String])
    //    session
    // }
    .doIfEqualsOrElse(session => session("tanentA-previousTaskId").as[String],session => session("tanentA-taskId").as[String]){
    //现查询的taskId==原查询的taskId
    pause(2000 milliseconds)
    }{
    //现查询的taskId!=原查询的taskId
    //执行现在的task
    //模拟任务执行3s
    pause(3)
    .exec(session => {
      session.set("tanentA-previousTaskId",session("tanentA-taskId").as[String])
    })
    //任务完成提交
    .exec(CompleteTask.tanentAcomplete)
    }
    .exec(Query.tanentAisEnded)
    }


    val tanentB = scenario("TanentB")
    .exec(
      http("startProcessInstanceByKey")
        .post(startQueryserverHost+"/activiti-engine-query-start/startProcessInstanceByKey/online-shopping-five-task")
        .check(status.is(200))
        .check(jsonPath("$.processInstanceId").saveAs("tanentB-processInstanceId"))
        .check(jsonPath("$.processDefinitionId").saveAs("tanentB-processDefinitionId"))
    )
    .exec(session => {
      session.set("tanentB-previousTaskId",-1)
    })
    .exec(session => {
      session.set("tanentB-isEnded", 0)
    })
    .doWhile(session => session("tanentB-isEnded").as[String].equals("0")) {
      // exec {
      //   session =>
      //     println("tanentB-isEnded:"+session("tanentB-isEnded").as[String])
      //     println("tanentB-previousTaskId:"+session("tanentB-previousTaskId").as[String])
      //     session
      // }
    exec(Query.tanentBquery)
    // .exec {
    //   session =>
    //    println("tanentB-taskId:"+session("tanentB-taskId").as[String])
    //    session
    // }
    .doIfEqualsOrElse(session => session("tanentB-previousTaskId").as[String],session => session("tanentB-taskId").as[String]){
    //现查询的taskId==原查询的taskId
    pause(2000 milliseconds)
    }{
    //现查询的taskId!=原查询的taskId
    //执行现在的task
    //模拟任务执行3s
    pause(3)
    .exec(session => {
      session.set("tanentB-previousTaskId",session("tanentB-taskId").as[String])
    })
    //任务完成提交
    .exec(CompleteTask.tanentBcomplete)
    }
    .exec(Query.tanentBisEnded)
    }



  
  setUp(
    tanentA.inject(constantUsersPerSec(5).during(1200.seconds)),
    tanentB.inject(constantUsersPerSec(5).during(1200.seconds)),
  ).protocols(httpProtocol)
  
  // setUp(
  //   tanentA.inject(rampUsersPerSec(1) to (6) during(1200.seconds)),
  //   tanentB.inject(rampUsersPerSec(1) to (6) during(1200.seconds))
  // ).protocols(httpProtocol)

  // setUp(
  //   tanentA.inject(
  //     rampUsersPerSec(1) to (6) during(600.seconds),
  //     rampUsersPerSec(6) to (1) during(600.seconds)
  //     ),
  //   tanentB.inject(
  //     rampUsersPerSec(1) to (6) during(600.seconds),
  //     rampUsersPerSec(6) to (1) during(600.seconds)

  //     )
  // ).protocols(httpProtocol)

  // setUp(
  //   tanentA.inject(
  //     constantUsersPerSec(1).during(200.seconds),
  //     constantUsersPerSec(2).during(200.seconds),
  //     constantUsersPerSec(3).during(200.seconds),
  //     constantUsersPerSec(4).during(200.seconds),
  //     constantUsersPerSec(3).during(200.seconds),
  //     constantUsersPerSec(2).during(200.seconds),
  //     constantUsersPerSec(1).during(200.seconds),
  //     ),
  //   tanentB.inject(
  //     constantUsersPerSec(1).during(200.seconds),
  //     constantUsersPerSec(2).during(200.seconds),
  //     constantUsersPerSec(3).during(200.seconds),
  //     constantUsersPerSec(4).during(200.seconds),
  //     constantUsersPerSec(3).during(200.seconds),
  //     constantUsersPerSec(2).during(200.seconds),
  //     constantUsersPerSec(1).during(200.seconds),
  //     )
  //   ).protocols(httpProtocol)
}
