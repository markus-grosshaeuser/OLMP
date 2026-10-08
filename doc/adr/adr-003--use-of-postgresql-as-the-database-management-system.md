# ADR 003: Use of PostgreSQL as the Database Management System

Status: Accepted

Date: 2026-10-08

Developer: M. Grosshaeuser

Tags: #Database, #Persistence

## 1. Context
   The Library Management System (OLMP) requires a reliable and robust relational database to manage highly structured and interconnected data, such as books, authors, users, loans, and fine transactions. It is critical that the database ensures data integrity and provides full ACID (Atomicity, Consistency, Isolation, Durability) compliance to prevent data corruption during concurrent operations, such as multiple users attempting to borrow the same book simultaneously.

## 2. Decision
   It has been decided to use PostgreSQL as the primary relational database management system (RDBMS).

## 3. Rationale
   ### PostgreSQL was chosen for the following reasons:
   - Data Integrity and Reliability: PostgreSQL is renowned for its strict adherence to SQL standards and its robust implementation of ACID properties, which is essential for maintaining the accuracy of library transaction records.
   - Advanced Feature Set: It offers a wide array of advanced data types (including JSONB for semi-structured data), sophisticated indexing options, and powerful query optimization capabilities that can handle complex relational queries as the system grows.
   - Strong Ecosystem Support: PostgreSQL has excellent integration with the Spring Boot ecosystem (via the PostgreSQL driver and Spring Data JPA).
   - Open Source and Community-Driven: As an open-source database, it avoids vendor lock-in and licensing costs while benefiting from a massive, active community that ensures continuous improvement and security updates.
   ### Alternatives considered:
   - MySQL / MariaDB: While highly capable and widely used, PostgreSQL was preferred due to its superior handling of complex queries and more advanced feature set for data integrity and extensibility.
   - Commercial RDBMS (e.g., Oracle, SQL Server): These were rejected due to the high cost of licensing and the risk of vendor lock-in.

## 4. Consequences
   ### Advantages (Pros):
   - High Reliability: Ensures that critical library data remains consistent and safe.
   - Scalability and Flexibility: Provides a strong foundation that can grow in complexity without requiring a migration to a different database engine.
   - Standardization: Using an industry-standard database simplifies DevOps, deployment, and hiring.
   ## Disadvantages (Cons):
   - Operational Overhead: Requires management of a database server (e.g., via Docker containers or managed cloud services), which adds a layer of infrastructure to maintain.
   - Configuration Complexity: While powerful, tuning PostgreSQL for optimal performance in a high-concurrency environment may require specialized knowledge.

## 5. Open Issues
   None.