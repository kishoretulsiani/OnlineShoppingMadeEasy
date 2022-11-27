# Online Shopping Made Easy

This application helps to quickly build up the online presence for a Fruit shop / Any business.
Current focus of the implementation is to demonstrate ability to deliver readable, maintainable and testable code in
agile manner for organization.

Solution should be presentable and developer should be able to speak about approaches he/she took to take this in
production environment.

Current implementation is limited to following use cases.

1. Orders Service that’s able to receive simple orders of shopping goods via a REST API
2. The shop decides to introduce various new offers
3. The service should now store the orders that a customer submits

# Swagger for the project is available here

[OnlineShoppingMadeEasy](https://kishoretulsiani.github.io/OnlineShoppingMadeEasy/swagger/index.html)

## Local env setup

### Prerequisites

1. Java 11
2. Docker
3. maven
4. Intellij Idea IDE ( screen shots are from ultimate version)

### Run [docker-compose.yml](docker-compose.yml) file

Open the file in intellij and click on services.. it should create container and setup Mongo and Redis in that.

### Setup Mongo and Redis connections in Intellij

Just follow all default options.. you should be able to connect with Redis and Mongo DB for Docker running on your local

![Screen Shot 2022-11-26 at 8.17.12 PM.png](docs%2Fimages%2FScreen%20Shot%202022-11-26%20at%208.17.12%20PM.png)

![Screen Shot 2022-11-26 at 8.17.47 PM.png](docs%2Fimages%2FScreen%20Shot%202022-11-26%20at%208.17.47%20PM.png)

Run below commands for Mongo in Order

```
show dbs;

use fruits_shop;

db.createUser({
    user: "admin",
    pwd: "admin",
    roles: [
        {role: "readWrite", db: "fruits_shop"}
    ]
});

show dbs

```

## Build the project

Follow mvn clean install to build the project.

> Current state of the project require Docker to run on your machine even for build as spins up the server and connects
> to Mongo and Redis for integration test cases.

## Run the project

Open the `[gateway-service.run.xml](.run%2Fgateway-service.run.xml)` file. it contains Run configurations.

Just start with Intellij Run/Debug Application

you should see below log once server starts successfully.

```
20:20:10.048 [vert.x-eventloop-thread-1] INFO  io.vertx.core.impl.launcher.commands.VertxIsolatedDeployer - Succeeded in deploying verticle
```

# Http Client is included in project

[gateway-service.http](httpclients%2Fgateway-service.http)

## Developer Notes

### Git commit conventions

Please use following prefixes to your git commits.

* feat – a new feature is introduced with the changes
* test - test cases are added to the modules
* fix – a bug fix has occurred
* refactor – refactored code that neither fixes a bug nor adds a feature
* docs – updates to documentation such as a the README or other markdown files

