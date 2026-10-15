# ADR 005: Use of Separate Security Flows for Employees and Members

Status: Accepted

Date: 2026-10-14

Developer: M. Grosshaeuser

Tags: #Security, #Backend, #Authentication

## 1. Context
The Library Management System (OLMP) has two different types of users with different access patterns and security requirements.

Employees use the server-side Vaadin user interface to manage internal library processes. This part of the application benefits from session-based authentication that integrates directly with Vaadin and Spring Security.

Members, on the other hand, access protected member functionality through REST-style API endpoints. These endpoints should be stateless and suitable for API-based clients. Therefore, they require a token-based authentication mechanism that does not depend on a server-side web session.

In addition, both user groups have separate account models, login identifiers, repositories, and principal types. The system also needs centralized handling of successful and failed authentication attempts in order to reset failed login counters, update last-access timestamps, and lock accounts after repeated bad credentials.

## 2. Decision
It has been decided to implement separate Spring Security flows for employees and members.

- Employee authentication is handled by a dedicated authentication manager using the employee user details service.
- Member authentication is handled by a separate authentication manager using the member user details service.
- The Vaadin-based employee UI uses Spring Security together with Vaadin's security integration.
- Member API endpoints use stateless security based on JWT authentication.
- Public member authentication endpoints are handled separately from protected member API endpoints.
- Authentication success and bad-credential failure events are published through Spring Security's AuthenticationEventPublisher and handled by a central security event listener.

The security filter chains are ordered so that more specific API paths are evaluated before the general Vaadin security chain.

## 3. Rationale
### Separate security flows were selected for the following reasons:
- Different User Groups: Employees and members represent different domains and have different account entities, repositories, and principal types.
- Different Access Patterns: Employees work through the Vaadin UI, while members access stateless API endpoints.
- Strong Separation of Concerns: Keeping employee and member authentication separate avoids mixing account lookup logic, roles, and session/token behavior.
- Stateless Member API: JWT-based authentication allows member API requests to be authenticated without relying on server-side HTTP sessions.
- Vaadin Integration: The employee UI can use Vaadin's Spring Security integration, which is designed for server-side UI applications.
- Centralized Login Auditing: Spring Security authentication events provide a common mechanism for updating last-access timestamps, resetting failed login counters, and applying account lockouts.

### Alternatives considered:
- Single Authentication Manager for all users: Rejected because employees and members use different account models and login identifiers. Combining them would add conditional lookup logic and increase the risk of security bugs.
- Session-Based Authentication for Members: Rejected because member API endpoints should remain stateless and usable by API clients without depending on server-side sessions.
- JWT Authentication for Employees: Rejected because the employee interface is implemented with Vaadin, where server-side session-based security integrates more naturally.
- Manual Login Attempt Tracking in Controllers or Services: Rejected because it would duplicate security logic and make it easier to miss authentication paths. Spring Security events provide a more centralized and consistent integration point.

## 4. Consequences
### Advantages (Pros):
- Clear Separation: Employee and member authentication can evolve independently.
- Better Fit for Each Client Type: Vaadin UI security remains session-oriented, while member APIs remain stateless.
- Improved Maintainability: Dedicated user details services, principals, and authentication managers make the responsibilities explicit.
- Centralized Account Protection: Login success and failure handling is implemented in one place using authentication events.
- Reduced Accidental Exposure: API security rules and Vaadin UI rules are separated by explicit filter chains and URL matchers.

### Disadvantages (Cons):
- Increased Configuration Complexity: Multiple security filter chains, authentication managers, and principal types require careful configuration.
- Ordering Matters: The security filter chains must remain correctly ordered so that specific API endpoints are matched before the general Vaadin security chain.
- Event Publishing Must Be Explicit: Manually created authentication managers must be configured with an AuthenticationEventPublisher, otherwise authentication listeners will not be executed.
- More Tests Required: Both security flows need dedicated tests to ensure authentication, authorization, JWT validation, and lockout behavior work as expected.

## 5. Open Issues
- The exact JWT lifetime and refresh strategy should be reviewed before production use.
- The account lockout duration and maximum number of failed login attempts may need to become configurable.
- Additional audit logging may be required for security-relevant events such as account lockouts, disabled accounts, and repeated failed login attempts.