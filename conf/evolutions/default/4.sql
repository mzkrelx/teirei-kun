# --- !Ups
ALTER TABLE person ADD created_at TIMESTAMP NOT NULL DEFAULT '2013-07-01';
ALTER TABLE person ADD updated_at TIMESTAMP NOT NULL DEFAULT '2013-07-01';
 
# --- !Downs

ALTER TABLE person DROP created_at;
ALTER TABLE person DROP updated_at;
