CREATE TABLE IF NOT EXISTS books (
  isbn                VARCHAR(21)  NOT NULL,
  book_title          VARCHAR(400) NOT NULL,
  date_of_publication DATE,
  PRIMARY KEY (isbn)
);
