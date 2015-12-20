CREATE TABLE IF NOT EXISTS authors (
  author_id        BIGINT       NOT NULL AUTO_INCREMENT,
  author_firstname VARCHAR(100) NOT NULL,
  author_surname   VARCHAR(100) NOT NULL,
  PRIMARY KEY (author_id)
);
