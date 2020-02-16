# Ship it!

ShipIt! is backend service written in Java and Spring Boot. It represents a platform where users can create shippings
using items available in the stock and a transporter. Each transporter has a maximum capacity, items have a volume and a
price. Given all the items available in stock and a transporter, the goal is to determine the number of some items to
include in the transporter, so that the total volume is less than or equal to the transporter capacity and the total
price value is as large as possible.

## Used technologies

The following technologies have been used:
- Java 11
- Spring Boot 2
- PostgreSQL 12

The service is fully stateless with a custom JWT based authentication filter.

The shipping creation is an example of [0-1 knapsack problem](https://en.wikipedia.org/wiki/Knapsack_problem), which is
solved using [dynamic programming](https://en.wikipedia.org/wiki/Dynamic_programming).

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing
purposes.

### Prerequisites

The project can be built and run in a number of ways described below. Depending on which way you choose, you'll need
a subset of the following installed on your machine:
- Docker 19.03
- JDK 11
- PostgreSQL 12

### Build and run with docker

With the provided [docker compose](docker-compose.yml) file you can build and run both the app and the database. It will
build the app using the [Dockerfile](Dockerfile), then start the app and db as services. Just run
```shell script
docker-compose up
```
in the project directory.

### Run with JDK

With JDK 11 installed, you can build and run the app directly. However, PostgreSQL needs to be running in advance.
It can run as a separate service with the provided [docker compose](docker-compose.yml) file:
```shell script
docker-compose up -d db
```
Alternatively you can install it directly on your machine.

#### Build and run with gradle

Use the gradle wrapper to build and run the app:
```shell script
./gradlew bootRun
```

#### Build with gradle and run with JDK

Use the gradle wrapper to build the app:
```shell script
./gradlew build
```
It will run all the tests and create an uber JAR under `build/libs/ship-it-<version>.jar`
This jar file can run with java 11 or higher
```shell script
java -jar build/libs/ship-it-<version>.jar
```
It's also possible to import the project into an IDE and run the main application
`com.codeperfection.shipit.ShipItApplication`

For that you need to have a [Lombok](https://projectlombok.org) plugin installed on your IDE. For IntelliJ IDEA you can
install it from [here](https://plugins.jetbrains.com/plugin/6317-lombok) and enable annotation processing. 

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
