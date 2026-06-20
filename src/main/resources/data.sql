INSERT IGNORE INTO user (id, username, password, nickname, gender, birth_date, sex, height)
       VALUES (1, 'TestUser', 'TestPass', 'Test', 'NON_BINARY', '1995-01-01', 'MALE', 1.75);

INSERT IGNORE into user (id, username, password, nickname, gender, birth_date, sex, height)
       VALUES (2, 'SomeoneElse', 'OtherPass', 'Other', 'FEMALE', '2001-01-01', 'FEMALE', 1.68);

INSERT IGNORE into food (id, name, brand, user_id, calories100g, protein100g, carbs100g, fat100g, fiber100g, price100g)
       VALUES (1, 'Not Your Food', 'Not Your Brand', 2, 235.0, 8.0, 34.0, 11.0, 4.0, 5.30);