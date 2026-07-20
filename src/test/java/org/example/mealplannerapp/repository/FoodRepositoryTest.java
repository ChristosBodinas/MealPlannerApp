package org.example.mealplannerapp.repository;

import org.junit.jupiter.api.Nested;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

@DataJpaTest(showSql = true, properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class FoodRepositoryTest {

    private FoodRepository foodRepository;
    
    private TestEntityManager entityManager;

    @Nested
    class findByIdVerified {

    }

    @Nested
    class deleteByIdVerified {

    }

    @Nested
    class searchByText {

    }

}
