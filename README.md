# Application Overview
This is a simple spring client application written in `Kotlin`.
It uses Webclient to do a client call to the https://jsonplaceholder.typicode.com endpoint
and returns the Todo object at a given id.

This project is designed to showcase the use of MockWebserver and Wiremock as options to test spring clients.

## Running the application
If you want to run the app locally using docker, first, build the application jar using `gradle bootJar`, then
run the [docker-compose](docker-compose.yml) file with `docker compose up`.

You can access the swagger ui at http://localhost:8080/swagger-ui/index.html where you can do simple get requests.

## Tests

You will notice the Application tests uses a baseurl of `todo.baseurl=http://localhost:${port}` where the port is injected at runtime as the dynamic port of each mocked server.
This allows us to run tests ensuring a port is normally free when running the tests.

- [MockWebServerApplicationTests](src/test/kotlin/com/khanivorous/todowebclient/MockWebServerApplicationTests.kt)
  - This spins up the application and tests with mock responses by MockWebServer
  - You will notice a limitation in this, as the server states cannot be reset between tests which causes problems in mock states
    - There is currently an issue open with okhttp to add this feature which would be helpful for spring tests and dynamic ports: [okhttp issue](https://github.com/square/okhttp/pull/6736)
- [MockWebSeverClientImplTest](src/test/kotlin/com/khanivorous/todowebclient/MockWebSeverClientImplTest.kt)
  - This tests the client directly using mock responses by MockWebServer
- [WireMockApplicationTest](src/test/kotlin/com/khanivorous/todowebclient/WireMockApplicationTest.kt)
  - This spins up the application and tests with mock responses by WireMock
- [WireMockClientImplTest](src/test/kotlin/com/khanivorous/todowebclient/WireMockClientImplTest.kt)
  - This tests the client directly using mock responses by WireMock

## Xray Jira Junit reports

An `xray/report` file is created after each test run. To avoid uploading test results for _all_ tests and only selected tests,
we can run a particular test task. 

- In this case, the [WireMockApplicationTest](src/test/kotlin/com/khanivorous/todowebclient/WireMockApplicationTest.kt) contains a method annotated with 
    ```
    @XrayTest(key = "KHAN-1", summary = "Get Todo by id", description = "This gets the Todo response and checks the id matches")
    @Requirement("KHAN-45","KHAN-46")
    ```

When we run the integration test task `tasks.register<Test>("integrationTests")` in [build.gradle.kts](build.gradle.kts), this produces and `xray-report` file with only the results from that
test task.

- We can then run [upload-xray.sh](upload-xray.sh) which will upload the test results to xray jira and create a new test execution with those results. We can then choose to create a pipeline that runs this test and this script, allowing us to separate the tests we _want_ to tag for xray jira.

For xray reference see also:
- [xray jira extension github](https://github.com/Xray-App/xray-junit-extensions)
- [xray jira extension example](https://github.com/Xray-App/tutorial-java-junit5-selenium)
- [xray junit tutorial](https://docs.getxray.app/display/XRAYCLOUD/Testing+web+applications+using+Selenium+and+JUnit5+in+Java)
- [xray authentication](https://docs.getxray.app/display/XRAYCLOUD/Authentication+-+REST+v2)
- [xray upload test results](https://docs.getxray.app/display/XRAYCLOUD/Import+Execution+Results+-+REST+v2#ImportExecutionResultsRESTv2-JUnitXMLresults)
