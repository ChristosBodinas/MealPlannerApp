package org.example.mealplannerapp.repository;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest(showSql = true, properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class FoodRepositoryTest {

    private FoodRepository foodRepository;

}
