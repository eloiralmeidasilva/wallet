# Wallet Microservice

## Overview

This repository contains the implementation of the Wallet microservice, responsible for managing digital wallets. It provides functionalities for deposits, withdrawals, and transfers between accounts.

## Technologies Used

- **Java 21**
- **Spring Boot 3.4.3**
- **Spring Data JPA**
- **Spring Security**
- **H2 Database** (for testing environment)
- **JWT** (JSON Web Token)
- **Resilience4j** (circuit breaker and retry)
- **SpringDoc OpenAPI** (API documentation)
- **Spring Boot Actuator** (monitoring)
- **Spring Boot Validation**

## Installation and Execution

### Prerequisites

- **Java 21**
- **Maven 3.8+**

### Steps to Run

1. Clone the repository:
    ```bash
    git clone https://github.com/your-user/wallet.git
    cd wallet
    ```

2. Compile and run tests:
    ```bash
    mvn clean install
    ```

3. Run the application:
    ```bash
    mvn spring-boot:run
    ```

   The API will be available at [http://localhost:8080](http://localhost:8080).

   - Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
   - Metrics and Health (Actuator): [http://localhost:8080/actuator](http://localhost:8080/actuator)

## Authentication

This application uses mocked users, and only two exist:

- **User 1**:
  - Username: `user`
  - Password: `password`

- **User 2**:
  - Username: `user2`
  - Password: `password2`

Authentication must be performed using the credentials above.

### Example authentication via cURL:
```bash
curl -X POST http://localhost:8080/authenticate -d '{"username":"user", "password":"password"}' -H "Content-Type: application/json"

## Testing

To run automated tests, use the command:

```bash
mvn test

## Design and Architectural Decisions

The microservice was designed following best practices to ensure modularity, security, and scalability:

- **REST-based architecture**: Communication via HTTP following RESTful patterns.
- **JWT security**: Authentication and authorization managed via JWT tokens.
- **Persistence with JPA**: Simplified interface for database access.
- **Fault tolerance with Resilience4j**: Implementation of circuit breaker and retry to prevent propagated failures.
- **Documentation with OpenAPI**: Interactive interface available for API consumption.
- **Monitoring with Actuator**: Endpoints for service metrics and health monitoring.

## Design Decisions and Trade-offs

- **H2 database for testing**: To facilitate local execution, H2 was chosen. In production, a relational database like PostgreSQL or MySQL should be used.
- **Pessimistic locking for concurrency**: The service implements locking to ensure integrity during financial transactions. This approach was chosen to prevent update conflicts in critical transactions.
- **Balancing security and performance**: JWT usage enhances security but may impact performance. Caching could be a future option to optimize token verification.

## Next Steps

1. **Health Check Configuration**: Adjust Actuator settings to expose only the `/health` and `/info` endpoints, ensuring better control over exposed information.

2. **Separation of Transfer Implementations**:
   - Create a dedicated service for internal transfers within the system.
   - Develop a new implementation that calls external APIs to enable transfers to accounts in other financial institutions.

3. **Improvement in Exception Handling**:
   - Implement an enum containing well-defined business errors.
   - Capture these exceptions in the `ExceptionHandler` and return more user-friendly messages.

4. **Integration with the User Registration Service**:
   - Implement an API call to a user registration microservice.
   - Use this integration to validate and retrieve user information before processing transactions.

5. **Creation of a Core Security Application**:
   - Develop a separate module containing all security-related classes.
   - Allow reuse of the security implementation across other company microservices.

6. **Improvement in Test Coverage**:
   - Add integration tests to validate the complete transaction flow.
   - Implement load tests to assess system performance under high request volume.

