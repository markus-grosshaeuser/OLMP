# ADR 004: Use Flyway for Database Migrations

Status: Accepted

Date: 2026-10-08

Developer: M. Grosshaeuser

Tags: #Database, #DevOps, #Persistence

## 1. Context
   As the Library Management System (OLMP) evolves, the database schema will inevitably change (e.g., adding new tables for fine management, adding columns to the books table, or modifying constraints). There is a need for a reliable, repeatable, and automated way to evolve the database schema across different environments without manual intervention or risk of data loss.

## 2. Decision
   - Flyway will be used as the database migration tool.
   - Hibernate's automatic schema generation capabilities will be explicitly disabled (spring.jpa.hibernate.ddl-auto=none) to ensure that Flyway is the sole authority on the database structure.

## 3. Rationale
   ### Flyway was chosen for the following reasons:
   - Version Control for Schema: It treats database changes with the same rigor as application code.
   - Reliability and Repeatability: It ensures that every environment—from a developer's local machine to the production server—is running the exact same schema version.
   - Audit Trail: Flyway maintains a flyway_schema_history table within the database, providing a built-in audit log of all applied migrations.
   - Simplicity: It uses plain SQL, which allows leveraging PostgreSQL-specific features (like advanced indexing or triggers) directly in the migration scripts without being limited by an abstraction layer.
   ### Alternatives considered:
   - Hibernate ddl-auto (update/create): Rejected because it is non-deterministic and unsafe for production. It can make destructive changes or fail to handle complex refactoring (like renaming columns) correctly.
   - Liquibase: A powerful alternative that uses XML/YAML/JSON for migrations. However, Flyway was preferred for its simplicity and the fact that it allows writing pure SQL, which is more direct and easier for database administrators to review.

## 4. Consequences
   ### Advantages (Pros):
   - Predictable Deployments: Migrations are applied automatically as part of the application lifecycle, reducing manual deployment errors.
   - Safe Evolution: Allows for controlled, step-by-step changes to the schema that can be tested in isolation.
   ### Disadvantages (Cons):
   - Migration Management: Developers must be disciplined in creating new migration files rather than modifying existing ones.
   - Handling Failures: If a migration fails in a production environment, it requires careful manual intervention to resolve the state of the schema_history table and the database.

## 5. Open Issues
   None.