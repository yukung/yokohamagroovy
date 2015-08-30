DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS author;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS books_out_on_loan;

CREATE TABLE IF NOT EXISTS users (
    user_id IDENTITY PRIMARY KEY,
    user_name VARCHAR(100) NOT NULL,
    user_address VARCHAR(255) NOT NULL,
    phone_number VARCHAR(32) NOT NULL,
    email_address VARCHAR(255) NOT NULL,
    other_user_details VARCHAR(255) DEFAULT '' NOT NULL
);

CREATE TABLE IF NOT EXISTS books (
    isbn VARCHAR(255) PRIMARY KEY,
    book_title VARCHAR(255) NOT NULL,
    date_of_publication DATE NOT NULL
);

CREATE TABLE IF NOT EXISTS authors (
    author_id IDENTITY PRIMARY KEY,
    author_firstname VARCHAR(100) NOT NULL,
    author_surname VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS categories (
    category_id IDENTITY PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS books_out_on_loan (
    book_borrowing_id IDENTITY PRIMARY KEY,
    date_issued DATE NOT NULL,
    date_due_for_return DATE NOT NULL,
    date_returned DATE NOT NULL,
    amount_of_fine INT NOT NULL
);
