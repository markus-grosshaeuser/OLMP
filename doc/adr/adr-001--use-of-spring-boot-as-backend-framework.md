# ADR 001: Use of Spring Boot as the Backend Framework

Status: Accepted

Date: 2026-10-08

Developer: M. Grosshaeuser

Tags: #Backend, #Framework


## 1. Context
   The project aims to develop a Library Management System (OLMP). To ensure a reliable, scalable, and maintainable backend, a framework is required that provides robust support for core enterprise concerns, including dependency injection, data persistence, security, and web service orchestration. The choice of framework will impact developer productivity, application startup time, and the ease of integrating various third-party libraries.

## 2. Decision
   It has been decided to use Spring Boot as the primary application framework. This decision includes leveraging the Spring Boot ecosystem through "starter" dependencies to manage core functionalities such as:
   - Web Layer: Spring MVC for handling web requests.
   - Data Access: Spring Data JPA for object-relational mapping.
   - Database Migrations: Flyway for versioned database schema management.
   - Security: Spring Security for authentication and authorization.
   - Validation: Bean Validation (JSR 380) for data integrity.

## 3. Rationale
   ### Spring Boot was selected for the following reasons:
   - Rapid Development: The "convention over configuration" approach and autoconfiguration capabilities significantly reduce the amount of boilerplate code required to set up a production-ready application.
   - Mature Ecosystem: Spring provides a vast ecosystem of well-tested libraries and extensive community support, which simplifies solving common development challenges.
   - Seamless Integration: It provides first-class support for the technologies chosen for this project, such as JPA, Flyway, and Vaadin.
   - Testing Excellence: The framework offers comprehensive testing utilities (via spring-boot-starter-test) that facilitate unit, integration, and slice testing.
   ### Alternatives considered:
   - Micronaut / Quarkus: While these are highly efficient for microservices and serverless environments due to lower memory footprints, Spring Boot's maturity and the depth of its integration with enterprise libraries make it a more productive choice for this implementation.
   - Plain Spring Framework: Refused due to the high overhead of manual configuration required compared to the streamlined Spring Boot approach.

## 4. Consequences
   ### Advantages (Pros):
   - Increased Productivity: Faster time-to-market due to automated configuration and dependency management.
   - Maintainability: A standardized project structure that is familiar to most Java developers.
   - Robustness: Access to battle-tested security and data access modules.
   ### Disadvantages (Cons):
   - Resource Overhead: A slightly larger memory footprint and longer startup times compared to "lightweight" frameworks.
   - Complexity/Abstraction: The "magic" of auto-configuration can sometimes make debugging the underlying configuration logic more complex for developers unfamiliar with the Spring internals.

## 5. Open Issues
   None.