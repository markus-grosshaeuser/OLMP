# OLMP – The Open Library Management Platform

🚧 Status: **Under Construction** 🚧

OLMP is a comprehensive library management platform designed to handle both internal administrative workflows and external member services.

## 🚀 Overview

The system provides two distinct interfaces to meet the needs of different user personas:

1.  **Internal Management (Employees):** A highly interactive, server-side web interface built with **Vaadin**, allowing librarians and administrators to manage books, users, and library operations seamlessly.
2.  **External Services (Members):** A robust **REST API** designed to support external applications (web or mobile) where members can manage their profiles, view memberships, and interact with library services.

## 🛠 Tech Stack

The project leverages a modern, enterprise-grade Java stack:

*   **Backend:** [Spring Boot](https://spring.io/projects/spring-boot)
*   **UI Framework (Internal):** [Vaadin](https://vaadin.com/)
*   **API Layer:** Spring MVC (REST)
*   **Persistence:** [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
*   **Database:** [PostgreSQL](https://www.postgresql.org/)
*   **Database Migrations:** [Flyway](https://flywaydb.org/)
*   **Security:** [Spring Security](https://spring.io/projects/spring-security)
*   **Language:** Java 25
*   **Build Tool:** Maven

## 🏗 Architecture

The system follows a unified backend architecture where business logic is centralized in a service layer, shared by both the Vaadin UI and the REST API. This ensures data integrity and consistent rule enforcement across all entry points.

*   **Presentation Layer:** Dual-entry (Vaadin for stateful server-side UI; REST for stateless client-side interaction).
*   **Service Layer:** Core business logic and orchestration.
*   **Data Access Layer:** Repository pattern implemented via Spring Data JPA.
*   **Persistence Layer:** Relational storage managed by PostgreSQL and evolved via versioned SQL migrations.

## 🚦 Getting Started

### Prerequisites

*   **JDK 25** or higher
*   **Maven** (though the included `./mvnw` wrapper can be used)
*   **PostgreSQL** instance running locally or via Docker (a docker compose file is provided in the repository)

### Local Setup

1.  **Clone the repository:**
    ```bash
    git clone <repository-url>
    cd OLMP
    ```

2.  **Configure the Database:**
    Update `src/main/resources/application.properties` with your PostgreSQL credentials.

3.  **Run the application:**
    ```bash
    ./mvnw spring-boot:run
    ```

The application will automatically run database migrations via Flyway upon startup.

## 📚 Documentation

*   **Architecture Overview:** See the `doc/architecture.md` file for a high-level overview of the system architecture.
*   **Architectural Decisions:** See the `doc/adr/` directory for the rationale behind major technology choices.
*   **Database Schema:** Entity-relationship diagrams and schema details can be found in `doc/db_schema/`.