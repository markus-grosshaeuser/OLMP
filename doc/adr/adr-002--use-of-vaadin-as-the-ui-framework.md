# ADR 002: Use of Vaadin as the User Interface Framework

Status: Accepted

Date: 2026-10-08

Developer: M. Grosshaeuser

Tags: #Frontend, #UI

## 1. Context
   The Library Management System (OLMP) requires a web-based user interface to allow users (such as administrators and librarians) to interact with the system's core features, including book searches, lending processes, and user management. The goal is to provide a modern, highly interactive user experience while maintaining a development workflow that is efficient and leverages existing Java expertise.

## 2. Decision
   It has been decided to use Vaadin as the web UI framework. Vaadin allows building modern web applications using a component-based model where the UI logic is written in Java and executed on the server side. The framework automatically handles the communication between the browser and the server, providing a seamless single-page application (SPA) experience.

## 3. Rationale
   ### Vaadin was selected for the following reasons:
   - Unified Language Stack: By using Vaadin, the entire application—from database access to the user interface—can be developed in Java. This eliminates the need to manage a separate JavaScript/TypeScript codebase.
   - Strong Integration: Vaadin provides excellent, first-class integration with Spring Boot, allowing for easy use of dependency injection and Spring-managed services directly within the UI components.
   - Type Safety: The end-to-end Java approach ensures type safety from the backend services through to the UI components, which significantly reduces the risk of runtime errors.
   - Enhanced Security: Since the UI state and business logic reside on the server, the application is inherently more secure against certain types of client-side attacks, as the internal application logic is not exposed through public-facing API endpoints.
   ### Alternatives considered:
   - Modern JavaScript Frameworks (e.g., React, Angular, Vue): These are powerful for creating highly dynamic UIs but require managing a complex frontend build pipeline, maintaining a separate codebase, and developing a comprehensive REST or GraphQL API layer. This would significantly increase the architectural complexity and the total development effort.
   - Server-Side Template Engines (e.g., Thymeleaf): While simpler to implement, they lack the rich, highly interactive component model and the smooth "single-page" user experience that a modern management system requires.

## 4. Consequences
   ### Advantages (Pros):
   - Increased Developer Productivity: Rapid development of complex UIs without the overhead of managing a separate frontend stack.
   - Simplified Architecture: A unified project structure and a single build process (Maven).
   - Robust Security: Reduced attack surface by keeping the UI logic and application state on the server.
   ### Disadvantages (Cons):
   - Server-Side Resource Usage: Because the UI state is maintained in the server's memory for each active user session, the application will require more server RAM as the number of concurrent users increases.
   - Latency Sensitivity: UI interactions require a round-trip to the server, which may be more perceptible to users in environments with high network latency compared to client-side heavy SPAs.

## 5. Open Issues
   None.