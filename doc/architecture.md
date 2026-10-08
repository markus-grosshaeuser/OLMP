# Architecture

## System Context diagram for OLMP – The Open Library Management Platform

``` mermaid
C4Context
    Person(employee, "Employee", "Internal library staff responsible for managing books, loans, and users.")
    System(olmp, "OLMP (Library Management System)", "Provides tools for library resource management and member self-service.")
    Person(member, "Member", "External users who borrow books and manage their own profiles.")

    Rel(employee, olmp, "Uses", "Web browser")
    Rel(member, olmp, "Uses", "Mobile/Web application")
```


## Container diagram for OLMP – The Open Library Management Platform

``` mermaid
flowchart TD
    subgraph Users [Users]
        Employee((Employee))
        Member((Member))
    end

    subgraph Client_Side [Client Side]
        Browser[Web Browser]
        ExternalApp[Mobile / Web App]
    end

    subgraph OLMP_Server [OLMP - The Open Library Management Platform]
        direction TB
        subgraph Presentation_Layer [Presentation Layer]
            Vaadin[Vaadin UI Server-side]
            REST[REST API - Spring MVC]
        end
        
        Service[Service Layer - Business Logic]
        JPA[Data Access Layer - Spring Data JPA]
    end

    subgraph Data_Layer [Persistence Layer]
        DB[(PostgreSQL Database)]
    end

    %% Connections
    Employee --> Browser
    Browser <-->|HTTP / WebSockets| Vaadin
    
    Member --> ExternalApp
    ExternalApp -->|HTTP / JSON| REST
    
    Vaadin --> Service
    REST --> Service
    
    Service --> JPA
    JPA --> DB

    %% Styling
    style OLMP_Server fill:#eee,stroke:#333,stroke-width:2px
    style Data_Layer fill:#fff,stroke:#333,stroke-width:2px
    style Users fill:#fff,stroke:#333,stroke-dasharray: 5 5
```

### Users & Clients:
- Employees interact with the system via a standard Web Browser. Since Vaadin is a server-side framework, the "UI logic" actually stays on the server, and the browser acts as a thin client receiving component updates.
- Members (external users) interact via an External App (which could be a mobile app or a separate web frontend; neither is part of this project) that communicates with the system via a standard REST API.
### The Core - OLMP:
- ***Presentation Layer:*** This is split into two entry points within the same application. The Vaadin UI handles the internal employee workflows, while the Spring MVC REST Controllers handle the external member requests.
- ***Service Layer:*** This is where the business logic lives. Both the Vaadin components and the REST controllers call into this layer to ensure that business rules are applied consistently regardless of how the request was initiated.
- ***Data Access Layer:*** Using Spring Data JPA, this layer abstracts the complexity of SQL and manages the mapping between Java objects and the database tables.
### Persistence Layer:
- The PostgreSQL database acts as the single source of truth, managed and evolved via Flyway migrations.