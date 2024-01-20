# Ship it!

ShipIt is a backend service functioning as a platform where users can organize shippings by having items available in
stock and choosing a transporter. Each transporter is characterized by a maximum capacity, while items are defined by
their volume and price. The primary objective of ShipIt is to optimize the allocation of items to a transporter. This
involves calculating an optimal selection of items from the stock such that the total volume fits within the
transporter's capacity, and the total value of the shipment is maximized.

It operates as an OAuth2 resource server and requires JWT access tokens for authenticating and authorizing access to its
API endpoints. It is essential to have a running OAuth Authorization Server. ShipIt is designed to lazily fetch public
keys upon first use of an endpoint. An implementation of the required OAuth Authorization Server can be found at
[auth-service](https://github.com/codeperfection/auth-service).

## Authors
- [Arshak Nazaryan](https://github.com/nazaryan)
- [Hayk Khachatryan](https://github.com/haykart)

## High-Level Description Of Used Frameworks And Libraries
- **Spring Boot (v3.2.0)**: For rapid application development, focusing on web services.
- **Kotlin (v1.9.20)**: As the primary programming language with Kotlin-specific Spring and JPA plugins.
- **Spring Boot Starters**: Including Actuator, Data JPA, Web, Validation, Security, and OAuth2 Resource Server.
- **Java Version**: Compatibility with Java 21.

Use the Postman collection and environment in [postman](postman) directory to try out the endpoints.

## How To Build And Run
**Prerequisite**: Auth Service Docker image must be built first. See details in 
[auth-service](https://github.com/codeperfection/auth-service).

### Running Both Service And Database With Docker
Required Docker version is 24.0.6 or above.

1. **Build Docker Image**
    - Navigate to the project root directory.
    - Run `docker compose build --no-cache ship-it-app-service`

2. **Run Docker Image**
   (at this point you should have both `codeperfection/auth-service` and `codeperfection/ship_it` docker images)
    - Run `docker compose up -d` to start both the application and the database services.
    - The application will be accessible on `http://localhost:8082`.

### Running Only Database With Docker
Note that we still need Auth Service up and running
1. **Starting Auth Service and the database**:
    - Run `docker compose up -d auth-app-service auth-db-service ship-it-db-service` in a Terminal.
    - The database will be accessible on `localhost:5434`.

2. **Configuring and running the application locally**:
    - Default application properties are configured to connect to `localhost:5434` for the database.
    - Run the application through your IDE or command line (`./gradlew bootRun`).