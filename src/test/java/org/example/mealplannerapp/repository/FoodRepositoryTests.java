package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.mealplannerapp.fixture.UserTestFixtures.*;
import static org.example.mealplannerapp.fixture.FoodTestFixtures.*;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never",
})
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class FoodRepositoryTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FoodRepository foodRepository;

    private User owner;
    private User stranger;

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @BeforeEach
    void prepareAllTests() {
        owner = defaultUserBuilder().username("William59").build();
        stranger = defaultUserBuilder().username("Jack37").build();

        entityManager.persist(owner);
        entityManager.persist(stranger);
    }

    @Nested
    class fetchByIdVerified {

        private final String FOOD_NAME = "Lasagna";

        private Food food;

        @BeforeEach
        void prepareTests() {
            food = defaultFoodBuilder().owner(owner).name(FOOD_NAME).build();
            entityManager.persist(food);
            flushAndClear();
        }


        @Test
        void foodFetched() {
            // Act
            Optional<Food> result = foodRepository.fetchByIdVerified(owner.getId(), null)


        }

        @Test
        void foodNotFound() {

        }

        @Test
        void foodNotOwned() {

        }

    }

    @Nested
    class fetchShallowByTextVerified {

    }

    @Nested
    class deleteByIdVerified {

        void foodDeleted() {

        }

        void foodNotFound() {

        }

        void foodNotOwned() {

        }

    }

}