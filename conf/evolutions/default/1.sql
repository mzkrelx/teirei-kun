
# --- !Ups

CREATE SEQUENCE program_id_seq;
CREATE TABLE program (
    id             INTEGER      DEFAULT nextval('program_id_seq') PRIMARY KEY,
    title          VARCHAR(100)         NOT NULL,
    description    TEXT
);
 
CREATE SEQUENCE schedule_id_seq;
CREATE TABLE schedule (
    id             INTEGER      DEFAULT nextval('schedule_id_seq') PRIMARY KEY,
    program_id     INTEGER      NOT NULL REFERENCES program(id),
    date           TIMESTAMP    NOT NULL
);

CREATE SEQUENCE person_id_seq;
CREATE TABLE person (
    id             INTEGER      DEFAULT nextval('person_id_seq') PRIMARY KEY,
    person_name    VARCHAR(30)  NOT NULL
);

CREATE SEQUENCE attendance_id_seq;
CREATE TABLE attendance (
    id             INTEGER      DEFAULT nextval('attendance_id_seq') PRIMARY KEY,
    person_id      INTEGER      NOT NULL REFERENCES person(id),
    schedule_id    INTEGER      NOT NULL REFERENCES schedule(id),
    choice         INTEGER
);
CREATE UNIQUE INDEX attendance_idx1 ON attendance(schedule_id, person_id);


# --- !Downs

DROP SEQUENCE attendance_id_seq;
DROP SEQUENCE person_id_seq;
DROP SEQUENCE schedule_id_seq;
DROP SEQUENCE program_id_seq;
DROP TABLE attendance;
DROP TABLE person; 
DROP TABLE schedule; 
DROP TABLE program;