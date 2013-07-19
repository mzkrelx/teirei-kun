# --- !Ups
ALTER TABLE attendance ADD memo TEXT;
 
# --- !Downs

ALTER TABLE attendance DROP memo;
