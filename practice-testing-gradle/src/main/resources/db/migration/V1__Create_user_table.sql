CREATE TABLE IF NOT EXISTS users (
  user_id BIGINT NOT NULL AUTO_INCREMENT,
  user_name VARCHAR(50) NOT NULL,
  user_address VARCHAR(255),
  phone_number VARCHAR(50) NOT NULL,
  email_address VARCHAR(100) NOT NULL,
  other_user_details TEXT,
  PRIMARY KEY (user_id)
);