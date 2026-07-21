INSERT IGNORE INTO user (id, username, password, nickname, gender, birth_date, sex, height)
       VALUES (1, 'TestUser', 'TestPass', 'Test', 'NON_BINARY', '1995-01-01', 'MALE', 1.75);

INSERT IGNORE into user (id, username, password, nickname, gender, birth_date, sex, height)
       VALUES (2, 'SomeoneElse', 'OtherPass', 'Other', 'FEMALE', '2001-01-01', 'FEMALE', 1.68);

INSERT IGNORE into food (id, name, brand, user_id, calories_per100g, protein_per100g, carbs_per100g, fat_per100g, fiber_per100g, edible_ratio)
       VALUES (1, 'Not Your Food', 'Not Your Brand', 2, 235.0, 8.0, 34.0, 11.0, 4.0, 0.8);