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

package computerdatabase;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;

public class WithoutBuffer extends Simulation {

    String processExecutionHost = "http://222.200.180.59:10238";
    String startQueryserverHost = "http://222.200.180.59:10239";

    HttpProtocolBuilder httpProtocol = http
        // Here is the root for all relative URLs
        .baseUrl(processExecutionHost)
        // Here are the common headers
        .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8").doNotTrackHeader("1")
        .acceptLanguageHeader("en-US,en;q=0.5").acceptEncodingHeader("gzip, deflate")
        .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:16.0) Gecko/20100101 Firefox/16.0");

    ChainBuilder Query =
        exec(http("QueryTaskList"))
            .get(startQueryserverHost + "/activiti-engine-query-start/getActiveTasks")
            .check(status().is(200))
            .check(jsonPath("$.taskId").saveAs("taskId"))

    // A scenario is a chain of requests and pauses
    ScenarioBuilder scn = scenario("Scenario Name").exec(Query);

    {
        setUp(scn.injectOpen(atOnceUsers(1)).protocols(httpProtocol));
    }
}
