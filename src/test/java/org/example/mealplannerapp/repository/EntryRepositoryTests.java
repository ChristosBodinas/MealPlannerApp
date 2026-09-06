package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.entity.*;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.entity.entry.ExerciseEntry;
import org.example.mealplannerapp.entity.entry.FoodEntry;
import org.example.mealplannerapp.projection.Placement;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.example.mealplannerapp.fixture.DayTestFixtures.defaultDay;
import static org.example.mealplannerapp.fixture.EntryTestFixtures.defaultExerciseEntry;
import static org.example.mealplannerapp.fixture.EntryTestFixtures.defaultFoodEntry;
import static org.example.mealplannerapp.fixture.ExerciseTestFixtures.defaultExercise;
import static org.example.mealplannerapp.fixture.FoodTestFixtures.defaultFood;
import static org.example.mealplannerapp.fixture.PlanTestFixtures.defaultPlan;
import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUser;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class EntryRepositoryTests {

    // CONSTANTS
    private static final String MY_AUTH_ID = "MyAuthId";
    private static final String MY_USERNAME = "MyUsername";
    private static final String OTHER_AUTH_ID = "OtherAuthId";
    private static final String OTHER_USERNAME = "OtherUsername";

    // BEANS
    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private EntryRepository entryRepository;

    // VARIABLES
    private User myUser;
    private Plan myPlan;
    private Day myDay;
    private User otherUser;
    private Plan otherPlan;
    private Day otherDay;

    // HELPER METHODS
    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private void prepareOtherUser() {
        otherUser = defaultUser().authId(OTHER_AUTH_ID).username(OTHER_USERNAME).build();
        otherPlan = defaultPlan().user(otherUser).build();
        otherDay = defaultDay().plan(otherPlan).position(1).build();
        otherPlan.getDays().add(otherDay);

        entityManager.persist(otherUser);
        entityManager.persist(otherPlan);
    }

    private FoodEntry prepareFoodEntry(User owner, Day day) {
        Food food = defaultFood().user(owner).build();
        FoodEntry entry = defaultFoodEntry().day(day).food(food).build();

        entityManager.persist(food);
        entityManager.persist(entry);
        return entry;
    }

    private ExerciseEntry prepareExerciseEntry(User owner, Day day) {
        Exercise exercise = defaultExercise().user(owner).build();
        ExerciseEntry entry = defaultExerciseEntry().day(day).exercise(exercise).build();

        entityManager.persist(exercise);
        entityManager.persist(entry);
        return entry;
    }

    // TESTS PROPER
    @BeforeEach
    void prepareUser() {
        myUser = defaultUser().authId(MY_AUTH_ID).username(MY_USERNAME).build();
        myPlan = defaultPlan().user(myUser).build();
        myDay = defaultDay().plan(myPlan).position(1).build();
        myPlan.getDays().add(myDay);

        entityManager.persist(myUser);
        entityManager.persist(myPlan);
    }

    // TODO: Write tests.

    @Nested
    @DisplayName("fetchByIdVerified")
    class FetchByIdVerified {

        @Test
        @DisplayName("Given an existing FoodEntry ID owned by the current user, returns the FoodEntry, " +
                "its referenced Food, and the Food's associated units/vendors.")
        void foodEntryFetched() {
            // Arrange
            FoodEntry entry = prepareFoodEntry(myUser, myDay);
            flushAndClear();

            // Act
            Optional<Entry> result = entryRepository.fetchByIdVerified(myUser.getId(), entry.getId());

            // Assert
            assertThat(result).as("Method output should be present.").isPresent();
            assertThat(result.get()).as("Method output should contain a FoodEntry.")
                    .isInstanceOf(FoodEntry.class);

            FoodEntry fetched = (FoodEntry) result.get();
            assertThat(Hibernate.isInitialized(fetched.getFood()))
                    .as("Referenced food should be initialized.")
                    .isTrue();

            assertThat(Hibernate.isInitialized(fetched.getFood().getUnits()))
                    .as("Referenced food units should be initialized.")
                    .isTrue();

            assertThat(Hibernate.isInitialized(fetched.getFood().getVendors()))
                    .as("Referenced food vendors should be initialized.")
                    .isTrue();
        }

        @Test
        @DisplayName("Given an existing ExerciseEntry ID owned by the current user, returns the ExerciseEntry, " +
                "its referenced Exercise, and the Exercise's associated effort levels.")
        void exerciseEntryFetched() {
            // Arrange
            ExerciseEntry entry = prepareExerciseEntry(myUser, myDay);
            flushAndClear();

            // Act
            Optional<Entry> result = entryRepository.fetchByIdVerified(myUser.getId(), entry.getId());

            // Assert
            assertThat(result).as("Method output should be present.").isPresent();
            assertThat(result.get()).as("Method output should contain an ExerciseEntry.")
                    .isInstanceOf(ExerciseEntry.class);

            ExerciseEntry fetched = (ExerciseEntry) result.get();

            assertThat(Hibernate.isInitialized(fetched.getExercise()))
                    .as("Referenced exercise should be initialized.")
                    .isTrue();

            assertThat(Hibernate.isInitialized(fetched.getExercise().getLevels()))
                    .as("Referenced exercise effort levels should be initialized.")
                    .isTrue();
        }

        @Test
        @DisplayName("Given a non-existent entryId, returns empty.")
        void entryNotFound() {
            // Arrange
            flushAndClear();

            // Act
            Optional<Entry> result = entryRepository.fetchByIdVerified(myUser.getId(), 999L);

            // Assert
            assertThat(result).as("Method output should be empty.").isEmpty();
        }

        @Test
        @DisplayName("Given an existing entryId owned by another user, returns empty.")
        void entryNotOwned() {
            // Arrange
            prepareOtherUser();
            FoodEntry entry = prepareFoodEntry(otherUser, otherDay);
            flushAndClear();

            // Act
            Optional<Entry> result = entryRepository.fetchByIdVerified(myUser.getId(), entry.getId());

            // Assert
            assertThat(result).as("Method output should be empty.").isEmpty();
        }

    }

    @Nested
    @DisplayName("extractPlacementByIdVerified")
    class ExtractPlacementByIdVerified {

        @Test
        @DisplayName("Given an existing entryId owned by the current user, returns the entry's placement data.")
        void placementExtracted() {
            // Arrange
            FoodEntry entry = prepareFoodEntry(myUser, myDay);
            flushAndClear();

            // Act
            Optional<Placement> result = entryRepository.extractPlacementByIdVerified(myUser.getId(), entry.getId());

            // Assert
            assertThat(result).as("Method output should be present.").isPresent();

            Placement fetched = result.get();

            assertThat(fetched.dayId()).as("Fetched dayId should be correct.")
                    .isEqualTo(entry.getDay().getId());
            assertThat(fetched.category()).as("Fetched category should be correct.")
                    .isEqualTo(entry.getCategory());
            assertThat(fetched.position()).as("Fetched position should be correct.")
                    .isEqualTo(entry.getPosition());
        }

        @Test
        @DisplayName("Given a non-existent entryId, returns empty.")
        void entryNotFound() {
            // Arrange
            flushAndClear();

            // Act
            Optional<Placement> result = entryRepository.extractPlacementByIdVerified(myUser.getId(), 999L);

            // Assert
            assertThat(result).as("Method output should be empty.").isEmpty();
        }

        @Test
        @DisplayName("Given an existing entryId owned by another user, returns empty.")
        void entryNotOwned() {
            // Arrange
            prepareOtherUser();
            FoodEntry entry = prepareFoodEntry(otherUser, otherDay);
            flushAndClear();

            // Act
            Optional<Placement> result = entryRepository.extractPlacementByIdVerified(myUser.getId(), entry.getId());

            // Assert
            assertThat(result).as("Method output should be empty.").isEmpty();
        }
    }

    @Nested
    @DisplayName("countByDayAndCategory")
    class CountByDayAndCategory {

        private final Category TARGET_CATEGORY = Category.BREAKFAST;
        private final Category EXCLUDED_CATEGORY = Category.SNACK;

        List<Entry> prepareEntriesToCount(Day day) {
            List<Entry> entries = new ArrayList<>(2);

            FoodEntry entry1 = defaultFoodEntry()
                    .day(day).category(TARGET_CATEGORY).position(1)
                    .build();
            entries.add(entry1);

            ExerciseEntry entry2 = defaultExerciseEntry()
                    .day(day).category(TARGET_CATEGORY).position(2)
                    .build();
            entries.add(entry2);

            entityManager.persist(entry1);
            entityManager.persist(entry2);

            return entries;
        }

        @Test
        @DisplayName("Does not count entries from other days.")
        void otherDaysExcluded() {
            // Arrange
            List<Entry> validEntries = prepareEntriesToCount(myDay);

            Day excludedDay = defaultDay().plan(myPlan).position(2).build();
            myPlan.getDays().add(excludedDay);
            entityManager.persist(excludedDay);

            FoodEntry invalidEntry = prepareFoodEntry(myUser, excludedDay);
            invalidEntry.setCategory(TARGET_CATEGORY);

            flushAndClear();

            // Act
            int result = entryRepository.countByDayAndCategory(myDay.getId(), TARGET_CATEGORY);

            // Assert
            assertThat(result).as("Method output should be equal to number of valid entries.")
                    .isEqualTo(validEntries.size());
        }

        @Test
        @DisplayName("Does not count entries from other categories.")
        void otherCategoriesExcluded() {
            // Arrange
            List<Entry> validEntries = prepareEntriesToCount(myDay);

            FoodEntry invalidEntry = defaultFoodEntry()
                    .day(myDay).category(EXCLUDED_CATEGORY)
                    .build();
            entityManager.persist(invalidEntry);

            flushAndClear();

            // Act
            int result = entryRepository.countByDayAndCategory(myDay.getId(), TARGET_CATEGORY);

            // Assert
            // Assert
            assertThat(result).as("Method output should be equal to number of valid entries.")
                    .isEqualTo(validEntries.size());
        }

    }

    @Nested
    @DisplayName("shiftUpByDayAndCategory")
    class ShiftUpByDayAndCategory {
        // TODO: Write tests.
    }

    @Nested
    @DisplayName("shiftDownByDayAndCategory")
    class ShiftDownByDayAndCategory {
        // TODO: Write tests.
    }
}
