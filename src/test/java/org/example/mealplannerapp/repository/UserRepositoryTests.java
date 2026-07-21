package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.entity.User;
import org.junit.jupiter.api.BeforeEach;
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

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never",
})
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class UserRepositoryTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    class findByUsername {

        private final String RIGHT_USERNAME = "william5";

        @BeforeEach
        void prepareTests() {
            User user = defaultUserBuilder().username(RIGHT_USERNAME).build();
            entityManager.persist(user);
            flushAndClear();
        }

        @Test
        void happyFlow() {
            // Act
            Optional<User> result = userRepository.findByUsername(RIGHT_USERNAME);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getUsername()).isEqualTo(RIGHT_USERNAME);
        }

        @Test
        void userNotFound() {
            // Act
            String WRONG_USERNAME = "willy";
            Optional<User> result = userRepository.findByUsername(WRONG_USERNAME);

            // Assert
            assertThat(result).isEmpty();
        }

    }
}
