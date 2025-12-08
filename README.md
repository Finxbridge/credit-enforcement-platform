# Credit Enforcement Platform

## Project Overview
The Credit Enforcement Platform is a comprehensive microservices-based application designed for managing collections and notices. It is built with a focus on scalability, resilience, and maintainability.

## Tech Stack
*   **Framework:** Spring Boot 3.2.0
*   **Language:** Java 21
*   **Database:** PostgreSQL
*   **Build Tool:** Maven
*   **Messaging:** Apache Kafka
*   **Cache:** Redis
*   **Cloud Storage:** AWS S3

## Microservices
This platform is composed of the following microservices:

*   **API Gateway (Port 8080):** Entry point for all client requests, handling routing, authentication, and rate limiting.
*   **Access Management Service (Port 8081):** Consists of two main modules: **Auth** for handling user authentication, JWT token generation, OTP, and session management; and **Management** for user CRUD, role and permission management.
*   **Master Data Service (Port 8082):** Manages all static and dynamic master data required across the platform.
*   **Case Service (Port 8083):** Manages case sourcing, validation, customer and loan details, strategies, campaigns, and allocations.
*   **Collections Service (Port 8084):** Manages telecalling, agency onboarding, repayment tracking, OTS, and dashboards.
*   **Notice Service (Port 8085):** Handles notice generation, batch processing, vendor dispatch, and tracking.
*   **Communication Service (Port 8086):** Integrates with SMS, WhatsApp, Email, Payment Gateways, and Dialers.
*   **Template Service (Port 8087):** Provides template management, rendering, and versioning for various communication channels.
*   **Report Service (Port 8088):** Generates scheduled and on-demand reports, and manages audit logs.

## Building and Running

### Prerequisites
*   Java 21 or higher
*   Maven 3.8+
*   PostgreSQL 14+
*   Redis 6+
*   Apache Kafka 3+

### Build Project
To build the entire project, navigate to the root directory (`credit-enforcement-platform`) and execute:
```bash
mvn clean install
```

### Run Application
The application supports multiple profiles (dev, staging, uat, preprod, prod).

#### Development Environment
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

#### Production Environment
```bash
java -jar target/credit-enforcement-platform-1.0.0-SNAPSHOT.jar --spring.profiles.active=prod
```

## Development Conventions

### Package Architecture
All packages follow a clean architecture pattern:
*   `domain/` - Entities, DTOs, Enums, Events
*   `repository/` - Data access layer (JPA Repositories)
*   `service/` - Business logic and services
*   `api/` - REST controllers
*   `config/` - Spring configuration
*   `util/` - Utility classes
*   `exception/` - Exception handling

### Testing
*   **Run all tests:** `mvn test`
*   **Run tests with coverage:** `mvn clean test jacoco:report`

## Contributing
1.  Create a feature branch from `develop`.
2.  Follow Java coding standards.
3.  Write unit tests.
4.  Submit a pull request.
