CREATE TABLE media_types (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code        VARCHAR(50)  NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_media_types_code CHECK (code = UPPER(code))
);



CREATE TABLE publishers (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    website    VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_publishers_name ON publishers (name);



CREATE TABLE locations (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code        VARCHAR(100) NOT NULL UNIQUE,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    parent_id   BIGINT REFERENCES locations(id) ON DELETE RESTRICT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_locations_no_self_parent CHECK (parent_id IS DISTINCT FROM id)
);

CREATE INDEX idx_locations_parent_id ON locations (parent_id);



CREATE TABLE media (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    media_type_id    BIGINT NOT NULL REFERENCES media_types(id) ON DELETE RESTRICT,
    publisher_id     BIGINT REFERENCES publishers(id) ON DELETE RESTRICT,
    title            VARCHAR(500) NOT NULL,
    subtitle         VARCHAR(500),
    edition          VARCHAR(100),
    publication_year SMALLINT,
    language_code    VARCHAR(10),
    cover_image      VARCHAR(100),
    description      TEXT,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_media_publication_year CHECK (publication_year IS NULL OR publication_year BETWEEN 1 AND 9999)
);

CREATE INDEX idx_media_title ON media (title);
CREATE INDEX idx_media_type_id ON media (media_type_id);
CREATE INDEX idx_media_publisher_id ON media (publisher_id);
CREATE INDEX idx_media_publication_year ON media (publication_year);



CREATE TABLE authors (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name VARCHAR(255),
    last_name  VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_authors_last_name ON authors (last_name);



CREATE TABLE media_authors (
    media_id  BIGINT NOT NULL REFERENCES media(id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES authors(id) ON DELETE RESTRICT,
    role      VARCHAR(50) NOT NULL DEFAULT 'AUTHOR',
    sort_order SMALLINT NOT NULL DEFAULT 0,
    PRIMARY KEY (media_id, author_id, role),

    CONSTRAINT chk_media_authors_sort_order CHECK (sort_order >= 0)
);

CREATE INDEX idx_media_authors_author_id ON media_authors (author_id);



CREATE TABLE media_identifiers (
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    media_id    BIGINT NOT NULL REFERENCES media(id) ON DELETE CASCADE,
    identifier_type VARCHAR(30) NOT NULL,
    identifier_value VARCHAR(100) NOT NULL,

    CONSTRAINT uq_media_identifiers_type_value UNIQUE (identifier_type, identifier_value),
    CONSTRAINT chk_media_identifiers_type CHECK (identifier_type = UPPER(identifier_type) AND identifier_type <> '')
);

CREATE INDEX idx_media_identifiers_media_id ON media_identifiers (media_id);



CREATE TABLE inventory_items (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    media_id         BIGINT NOT NULL REFERENCES media(id) ON DELETE RESTRICT,
    inventory_number VARCHAR(100) NOT NULL UNIQUE,
    barcode          VARCHAR(100) UNIQUE,
    condition        VARCHAR(30) NOT NULL DEFAULT 'GOOD',
    location_id      BIGINT REFERENCES locations(id) ON DELETE RESTRICT,
    acquired_at      DATE,
    acquisition_cost NUMERIC(10, 2),
    withdrawn_at     DATE,
    notes            TEXT,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_inventory_items_condition CHECK (condition IN ('NEW', 'GOOD', 'WORN', 'DAMAGED', 'UNUSABLE', 'LOST')),
    CONSTRAINT chk_inventory_items_acquisition_cost CHECK (acquisition_cost IS NULL OR acquisition_cost >= 0),
    CONSTRAINT chk_inventory_items_withdrawn_at CHECK (withdrawn_at IS NULL OR acquired_at IS NULL OR withdrawn_at >= acquired_at)
);

CREATE INDEX idx_inventory_items_media_id ON inventory_items (media_id);

CREATE INDEX idx_inventory_items_location_id ON inventory_items (location_id);

CREATE INDEX idx_inventory_items_active ON inventory_items (media_id) WHERE withdrawn_at IS NULL;
