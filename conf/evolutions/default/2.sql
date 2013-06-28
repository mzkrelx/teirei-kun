# --- !Ups
ALTER TABLE person ADD github_user_id LONG;

CREATE TABLE github_user (
    id             LONG                 PRIMARY KEY,
    login          TEXT                 NOT NULL,
    name           TEXT,
    email          TEXT,
    avatar_url     TEXT
);
 
# --- !Downs

ALTER TABLE person DROP github_user_id;

DROP TABLE github_user;
