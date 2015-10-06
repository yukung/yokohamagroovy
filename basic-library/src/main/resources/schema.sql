CREATE TABLE users(
  user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_name VARCHAR(100) NOT NULL,
  user_address VARCHAR(255) NOT NULL,
  phone_number VARCHAR(16) NOT NULL,
  email_address VARCHAR(255) NOT NULL,
  other_user_details TEXT
);

CREATE TABLE books(
  isbn VARCHAR(13) PRIMARY KEY,
  book_title VARCHAR(255) NOT NULL,
  date_of_publication DATE
);

CREATE TABLE author(
  author_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  author_firstname VARCHAR(64) NOT NULL,
  author_surname VARCHAR(64) NOT NULL
);

CREATE TABLE categories(
  category_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  category_name VARCHAR(100) NOT NULL
);