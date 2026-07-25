package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.entity.entry.FoodEntry;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Plan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.mealplannerapp.fixture.UserTestFixtures.*;
import static org.example.mealplannerapp.fixture.EntryTestFixtures.*;
import static org.example.mealplannerapp.fixture.DayTestFixtures.*;
import static org.example.mealplannerapp.fixture.PlanTestFixtures.*;
import static org.example.mealplannerapp.fixture.FoodTestFixtures.*;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"
})
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class EntryRepositoryTests {

        @Autowired
        private EntryRepository entryRepository;

        @Autowired
        private TestEntityManager entityManager;

        private User currentUser;
        private User otherUser;
        private Plan currentUserPlan;
        private Plan otherUserPlan;
        private Day currentUserDay;
        private Day otherUserDay;

        private void flushAndClear() {
                entityManager.flush();
                entityManager.clear();
        }

        @BeforeEach
        void prepareAllTests() {
                currentUser = defaultUserBuilder().username("William59").build();
                otherUser = defaultUserBuilder().username("Jack37").build();

                currentUserPlan = defaultPlanBuilder().user(currentUser).build();
                otherUserPlan = defaultPlanBuilder().user(otherUser).build();

                currentUserDay = defaultDayBuilder().plan(currentUserPlan).build();
                currentUserPlan.setDays(List.of(currentUserDay));

                otherUserDay = defaultDayBuilder().plan(otherUserPlan).build();
                otherUserPlan.setDays(List.of(otherUserDay));

                entityManager.persist(currentUser);
                entityManager.persist(otherUser);
                entityManager.persist(currentUserPlan);
                entityManager.persist(otherUserPlan);
                entityManager.persist(currentUserDay);
                entityManager.persist(otherUserDay);
        }

        @Nested
        class fetchFoodEntryByIdVerified {

                @Test
                void foodEntryFetched() {
                        // Arrange
                        FoodEntry entry = defaultFoodEntryBuilder().day(currentUserDay).build();
                        Food food = defaultFoodBuilder().build();

                        entityManager.persist(food);
                        entityManager.persist(entry);
                        flushAndClear();

                        // Act
                        Optional<Entry> result = entryRepository.fetchFoodEntryByIdVerified(currentUser.getId(), entry.getId());

                        // Assert
                        assertThat(result).isPresent();
                        assertThat(result).isInstanceOf(FoodEntry.class);
                        assertThat(result.get().getId()).isEqualTo(entry.getId());
                        assertThat(result.get().getDay().getPlan().getUser().getId()).isEqualTo(currentUser.getId());
                }

                @Test
                void foodEntryNotFound() {

                }

                @Test
                void foodEntryNotOwned() {

                }

        }

}