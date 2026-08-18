package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.entity.Exercise;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import static org.example.mealplannerapp.fixture.FoodTestFixtures.defaultFood;
import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUser;

@DataJpaTest(properties = { // TODO: Learn what these settings do.
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"
})
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class FoodRepositoryTests {

    // CONSTANTS
    private static final String MY_AUTH_ID = "alice_auth";
    private static final String MY_USERNAME = "Alice1";

    private static final String OTHER_AUTH_ID = "bob_auth";
    private static final String OTHER_USERNAME = "Bob2";

    // VARIABLES
    @Autowired
    private TestEntityManager entityManager;
    @Autowired private ExerciseRepository exerciseRepository;

    private User myUser;
    private User otherUser;

    // HELPER METHODS
    private void flushAndClear() {
        entityManager.flush();  // TODO: Explain.
        entityManager.clear();
    }

    private void prepareOtherUser() {
        otherUser = defaultUser().authId(OTHER_AUTH_ID).username(OTHER_USERNAME).build();
        entityManager.persist(otherUser);
    }

    private Food prepareFood(User owner) {
        Food food = defaultFood().user(owner).build();
        entityManager.persist(food);
        return food;
    }

    // TESTS PROPER
    @BeforeEach
    void prepareMyUser() {
        myUser = defaultUser().authId(MY_AUTH_ID).username(MY_USERNAME).build();
        entityManager.persist(myUser);
    }

    @Nested
    @DisplayName("fetchByIdVerified")
    class FetchByIdVerified {}

    @Nested
    @DisplayName("fetchShallowByUserAndText")
    class FetchShallowByUserAndText {}

    @Nested
    @DisplayName("deleteByIdVerified")
    class DeleteByIdVerified {}
}
