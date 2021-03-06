CREATE TYPE testing_state AS ENUM ('ACTIVE', 'REMOVE_DEPENDENCIES', 'REMOVE');
CREATE TYPE testing_entity_type AS ENUM ('USER', 'SERVICE', 'ROLE');

CREATE TABLE testing_session
(
    id                 VARCHAR(255) NOT NULL,
    session_key        VARCHAR NOT NULL,
    client_id          VARCHAR(255) NOT NULL,
    state              testing_state NOT NULL,
    create_date        TIMESTAMP NOT NULL,
    last_modified_date TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE testing_entity
(
    id                 VARCHAR(255) NOT NULL,
    entity_id          VARCHAR(255) NOT NULL,
    entity_type        testing_entity_type NOT NULL,
    testing_session_id VARCHAR(255),
    state              testing_state NOT NULL,
    create_date        TIMESTAMP NOT NULL,
    last_modified_date TIMESTAMP,
    PRIMARY KEY (id)
);
