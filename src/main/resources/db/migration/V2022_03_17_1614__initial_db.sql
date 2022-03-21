CREATE TYPE testing_session_state AS ENUM ('OPEN', 'CLOSED', 'EXPIRED', 'DELETE');
CREATE TYPE testing_entity_type AS ENUM ('USER', 'SERVICE', 'ROLE');

CREATE TABLE testingsession
(
    id               VARCHAR(255) NOT NULL,
    sessionkey       VARCHAR NOT NULL,
    clientid         VARCHAR(255) NOT NULL,
    state            testing_session_state NOT NULL,
    createdate       TIMESTAMP NOT NULL,
    lastmodifieddate TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE testingentity
(
    id               VARCHAR(255) NOT NULL,
    entityid         VARCHAR(255) NOT NULL,
    entitytype       testing_entity_type NOT NULL,
    testingsessionid VARCHAR(255) NOT NULL,
    createdate       TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
);
