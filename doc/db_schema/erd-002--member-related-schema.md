# Member-related database tables and relationships

``` mermaid
erDiagram
    MEMBERS {
        BIGINT id PK
        VARCHAR last_name
        VARCHAR first_name
        DATE date_of_birth
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
    }

    MEMBER_ACCOUNTS {
        BIGINT id PK
        BIGINT member_id FK
        VARCHAR email
        VARCHAR password_hash
        BOOLEAN verified
        BOOLEAN enabled
        INT failed_login_attempts
        TIMESTAMPTZ lockout_until
        BOOLEAN locked
        TIMESTAMPTZ created_at
        TIMESTAMPTZ updated_at
        TIMESTAMPTZ last_access
    }

    MEMBER_CONTACT_INFORMATION {
        BIGINT id PK
        BIGINT member_id FK
        VARCHAR phone_number
        VARCHAR street
        VARCHAR house_number
        VARCHAR postal_code
        VARCHAR city
        VARCHAR country
    }

    MEMBERSHIPS {
        BIGINT id PK
        BIGINT member_id FK
        VARCHAR membership_number
        VARCHAR type
        VARCHAR status
        DATE start_date
        DATE expiration_date
        DATE renewal_date
        DATE cancellation_date
    }

    MEMBERS ||--o| MEMBER_ACCOUNTS : has
    MEMBERS ||--o| MEMBER_CONTACT_INFORMATION : has
    MEMBERS ||--o{ MEMBERSHIPS : holds
```