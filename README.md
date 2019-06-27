# Spring Task-Launcher

An exemple of how to execute a batch (a "task") contained in an exernal jar from a Spring Boot app, in a sub-process.
This example can be used to launch, monitor, cancel, and execute mutliple external jar in parallel.

The status of the batchs, the last succeeded execution date are persisted in the database (embeded h2), and can be retrieved by making Rest requests (Defined in TaskController) 

## Stack

1. Java 8
2. Spring Boot >= 2.x

## How to use it
### Basic launch
The example can be used as-it and provides a minimal working example. You just have to:

* Modify the `jar-path` property in the `application.yml` file to set the path of the java jar you want to execute
* Launch the app
  * `./gradlew bootRun` on linux / MacOS
  * `.\gradlew.bat bootRun` on Windows
* Launch the Task by calling this url : `POST http://localhost:8080/v1/tasks/DEFAULT/launch`
    * A simple `java -jar` is performed on the specified file. The current spring profile is set as param of the launched jar: `-Dspring.profiles.active=...`
* That's all ! Now you can use the another endpoints to monitor the execution

### Monitoring

You can:
* Get the last successful execution date of a task, e.g. `GET http://localhost:8080/v1/tasks/DEFAULT/last-exec`
* Get the status of a task (running or stopped), e.g. `GET http://localhost:8080/v1/tasks/DEFAULT/status`
* Stop a running task, e.g. `POST http://localhost:8080/v1/tasks/DEFAULT/cancel`
* A database lock is used to prevent launching the same task twice, by checking the `running` field.
  * You can unlock it by changing the `runnning` value in DB, e.g `POST http://localhost:8080/v1/tasks/DEFAULT/in-progress/false `

### Tune it

This example is very basic and show how to launch and monitor a sigle task. You can use this code in your existing app and modify the `TASK` enum, and the controller to make it launching any batch you want

