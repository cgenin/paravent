CREATE TYPE queue_state AS ENUM ('created', 'running', 'success', 'error');
CREATE TYPE queue_type AS ENUM ('metadata', 'document');

CREATE TABLE IF NOT EXISTS queue
(
    id           varchar(256),
    uuid         UUID,
    collected_at TIMESTAMP,
    state        queue_state,
    start        TIMESTAMP,
    stop         TIMESTAMP,
    type         queue_type,
    PRIMARY KEY (id, collected_at, uuid)
);

CREATE TABLE IF NOT EXISTS metadatas
(
    uuid    UUID primary key,
    payload text
);


CREATE TABLE IF NOT EXISTS documents
(
    uuid      UUID primary key,
    name      varchar(256),
    extension varchar(32),
    metadata  UUID,
    bin    bytea
);

