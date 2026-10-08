# Employee-related database tables and relationships

``` mermaid
erDiagram
EMPLOYEES {
BIGINT id PK
VARCHAR email
VARCHAR last_name
VARCHAR first_name
TIMESTAMPTZ created_at
TIMESTAMPTZ updated_at
}

    EMPLOYEE_ACCOUNTS {
        BIGINT id PK
        BIGINT employee_id FK
        VARCHAR username
        VARCHAR password_hash
        BOOLEAN enabled
        INT failed_login_attempts
        TIMESTAMPTZ lockout_until
        BOOLEAN locked
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        TIMESTAMPTZ last_access
    }

    ROLES {
        BIGINT id PK
        VARCHAR name
    }

    PRIVILEGES {
        BIGINT id PK
        VARCHAR name
    }

    EMPLOYEE_ROLES {
        BIGINT employee_id PK, FK
        BIGINT role_id PK, FK
    }

    ROLE_PRIVILEGES {
        BIGINT role_id PK, FK
        BIGINT privilege_id PK, FK
    }

    EMPLOYEES ||--o| EMPLOYEE_ACCOUNTS : has
    EMPLOYEES ||--o{ EMPLOYEE_ROLES : assigned
    ROLES ||--o{ EMPLOYEE_ROLES : assigned_to
    ROLES ||--o{ ROLE_PRIVILEGES : grants
    PRIVILEGES ||--o{ ROLE_PRIVILEGES : included_in
```