package org.example.mealplannerapp.repository;

import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.entity.*;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.entity.entry.ExerciseEntry;
import org.example.mealplannerapp.entity.entry.FoodEntry;
import org.example.mealplannerapp.projection.CategoryStats;
import org.example.mealplannerapp.projection.Placement;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.example.mealplannerapp.fixture.DayTestFixtures.defaultDayBuilder;
import static org.example.mealplannerapp.fixture.EntryTestFixtures.defaultExerciseEntryBuilder;
import static org.example.mealplannerapp.fixture.EntryTestFixtures.defaultFoodEntryBuilder;
import static org.example.mealplannerapp.fixture.ExerciseTestFixtures.defaultExerciseBuilder;
import static org.example.mealplannerapp.fixture.FoodTestFixtures.defaultFoodBuilder;
import static org.example.mealplannerapp.fixture.PlanTestFixtures.defaultPlanBuilder;
import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUserBuilder;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never",
        "spring.jpa.properties.hibernate.globally_quoted_identifiers=true"
})
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class EntryRepositoryTests {

    // BEANS
    @Autowired
    private EntryRepository entryRepository;

    @Autowired
    private TestEntityManager entityManager;

    // VARIABLES
    private User myUser;
    private User otherUser;
    private Plan myPlan;
    private Plan otherPlan;
    private Day myDay;
    private Day otherDay;

    // CONSTANTS
    private final String MY_USERNAME = "alice1";
    private final String OTHER_USERNAME = "bob2";

    // HELPER METHODS
    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private User lookupUser(Entry entry) {
        return entry.getDay().getPlan().getUser();
    }

    private void prepareMyUser() {
        myUser = defaultUserBuilder().username(MY_USERNAME).build();
        myPlan = defaultPlanBuilder().user(myUser).build();
        myDay = defaultDayBuilder().plan(myPlan).build();
        myPlan.getDays().add(myDay);

        entityManager.persist(myUser);
        entityManager.persist(myPlan);
    }

    private Day prepareMySecondDay() {
        Day mySecondDay = defaultDayBuilder().plan(myPlan).position(2).build();
        myPlan.getDays().add(mySecondDay);
        entityManager.persist(mySecondDay);
        return mySecondDay;
    }

    private void prepareOtherUser() {
        otherUser = defaultUserBuilder().username(OTHER_USERNAME).build();
        otherPlan = defaultPlanBuilder().user(otherUser).build();
        otherDay = defaultDayBuilder().plan(otherPlan).build();
        otherPlan.getDays().add(otherDay);

        entityManager.persist(otherUser);
        entityManager.persist(otherPlan);
    }

    private FoodEntry prepareFoodEntry(User owner, Day ownerDay) {
        Food food = defaultFoodBuilder().user(owner).build();
        FoodEntry entry = defaultFoodEntryBuilder().day(ownerDay).food(food).build();

        entityManager.persist(food);
        entityManager.persist(entry);

        return entry;
    }

    private ExerciseEntry prepareExerciseEntry(User owner, Day ownerDay) {
        Exercise exercise = defaultExerciseBuilder().user(owner).build();
        ExerciseEntry entry = defaultExerciseEntryBuilder().day(ownerDay).build();

        entityManager.persist(exercise);
        entityManager.persist(entry);

        return entry;
    }

    private static Stream<Arguments> provideBounds() {
        return Stream.of(
                argumentSet("Both sides bounded", 4, 8),
                argumentSet("Only upper side bounded", null, 8),
                argumentSet("Only lower side bounded", 4, null),
                argumentSet("Neither side bounded", null, null),
                argumentSet("Upper bound = Lower bound", 4, 4),
                argumentSet("Upper bound < Lower bound", 8, 4),
                argumentSet("Both bounds out of range", 100, 200)
        );
    }

    // TESTS PROPER
    @Nested
    @DisplayName("fetchByIdVerified")
    class FetchByIdVerified {

        @BeforeEach
        void prepareTests() {
            prepareMyUser();
        }

        @Test
        @DisplayName("Returns empty when the requested entry does not exist.")
        void entryNotFound() {
            // Arrange
            flushAndClear();

            // Act
            Optional<Entry> result = entryRepository.fetchByIdVerified(myUser.getId(), 999L);

            // Assert
            assertThat(result).isEmpty();
            assertThat(entryRepository.existsById(999L)).isFalse();
        }

        @Test
        @DisplayName("Returns empty when the requested entry exists but belongs to another user.")
        void entryNotOwned() {
            // Arrange
            prepareOtherUser();
            FoodEntry entry = prepareFoodEntry(otherUser, otherDay);
            flushAndClear();

            // Act
            Optional<Entry> result = entryRepository.fetchByIdVerified(myUser.getId(), entry.getId());

            // Assert
            assertThat(result).isEmpty();
            assertThat(entryRepository.existsById(entry.getId())).isTrue();
        }

        @Test
        @DisplayName("Returns a FoodEntry and its associated Food when given the id of an existing, owned FoodEntry.")
        void foodEntryFetched() {
            // Arrange
            FoodEntry entry = prepareFoodEntry(myUser, myDay);
            flushAndClear();

            // Act
            Optional<Entry> result = entryRepository.fetchByIdVerified(myUser.getId(), entry.getId());

            // Assert
            assertThat(result).isPresent();
            FoodEntry fetched = (FoodEntry) result.get();

            assertThat(fetched.getId()).isEqualTo(entry.getId());
            assertThat(lookupUser(fetched).getId()).isEqualTo(myUser.getId());

            assertThat(Hibernate.isInitialized(fetched.getFood())).isTrue();
            assertThat(Hibernate.isInitialized(fetched.getFood().getUnits())).isTrue();
            assertThat(Hibernate.isInitialized(fetched.getFood().getPrices())).isTrue();
        }

        @Test
        @DisplayName("Returns an ExerciseEntry and its associated Exercise when given the id of an existing, owned ExerciseEntry.")
        void exerciseEntryFetched() {
            // Arrange
            ExerciseEntry entry = prepareExerciseEntry(myUser, myDay);
            flushAndClear();

            // Act
            Optional<Entry> result = entryRepository.fetchByIdVerified(myUser.getId(), entry.getId());

            // Assert
            assertThat(result).isPresent();
            ExerciseEntry fetched = (ExerciseEntry) result.get();

            assertThat(fetched.getId()).isEqualTo(entry.getId());
            assertThat(lookupUser(fetched).getId()).isEqualTo(myUser.getId());

            assertThat(Hibernate.isInitialized(fetched.getExercise())).isTrue();
            assertThat(Hibernate.isInitialized(fetched.getExercise().getLevels())).isTrue();
        }

    }

    @Nested
    @DisplayName("fetchByDayOrdered")
    class FetchByDayOrdered {

        @Test
        @DisplayName("Only fetches entries that belong to the day with the given identifier.")
        void correctEntriesFetched() {
            // Arrange
            FoodEntry validFoodEntry = prepareFoodEntry(myUser, myDay);
            ExerciseEntry validExerciseEntry = prepareExerciseEntry(myUser, myDay);

            Day invalidDay = prepareMySecondDay();
            FoodEntry invalidEntry = prepareFoodEntry(myUser, invalidDay);

            entityManager.persist(validFoodEntry);
            entityManager.persist(validExerciseEntry);
            entityManager.persist(invalidEntry);
            flushAndClear();

            // Act
            List<Entry> result = entryRepository.fetchByDayOrdered(myDay.getId());

            // Assert
            assertThat(result).containsExactlyInAnyOrder(validFoodEntry, validExerciseEntry);
            // TODO: Assert that referenced entities and collections are initialized.

        }

        private FoodEntry prepareFoodEntryToOrder(Category category, int position) {
            FoodEntry entry = prepareFoodEntry(myUser, myDay);
            entry.setCategory(category);
            entry.setPosition(position);
            entityManager.persist(entry);
            return entry;
        }

        @Test
        @DisplayName("Fetches entries ordered by category and then by position.")
        void entriesFetchedInOrder() {
            // Arrange
            FoodEntry breakfast1 = prepareFoodEntryToOrder(Category.BREAKFAST, 1)
            FoodEntry breakfast2 = prepareFoodEntryToOrder(Category.BREAKFAST, 2);
            FoodEntry dinner1 = prepareFoodEntryToOrder(Category.DINNER, 1);
            FoodEntry dinner2 = prepareFoodEntryToOrder(Category.DINNER, 2);
            flushAndClear();

            // Act
            List<Entry> result = entryRepository.fetchByDayOrdered(myDay.getId());

            // Assert
            assertThat(result).containsExactly(breakfast1, breakfast2, dinner1, dinner2);
        }

        @Test
        @DisplayName("Eagerly loads the referenced Food and its associated units/prices for any fetched FoodEntry.")
        void foodEagerlyFetched() {
            // Arrange
            FoodEntry entry = prepareFoodEntry(myUser, myDay);
            flushAndClear();

            // Act
            List<Entry> result = entryRepository.fetchByDayOrdered(myDay.getId());

            // Assert
            assertThat(result).containsExactly(entry);

            FoodEntry fetched = (FoodEntry) result.get(0);

            assertThat(Hibernate.isInitialized(fetched.getFood())).isTrue();
            assertThat(Hibernate.isInitialized(fetched.getFood().getUnits())).isTrue();
            assertThat(Hibernate.isInitialized(fetched.getFood().getPrices())).isTrue();
        }

        @Test
        @DisplayName("Eagerly loads the referenced Exercise and its associated intensity levels for any fetched ExerciseEntry.")
        void exerciseEagerlyFetched() {
            // Arrange
            ExerciseEntry entry = prepareExerciseEntry(myUser, myDay);
            flushAndClear();

            // Act
            List<Entry> result = entryRepository.fetchByDayOrdered(myDay.getId());

            // Assert
            assertThat(result).containsExactly(entry);

            ExerciseEntry fetched = (ExerciseEntry) result.get(0);

            assertThat(Hibernate.isInitialized(fetched.getExercise())).isTrue();
            assertThat(Hibernate.isInitialized(fetched.getExercise().getLevels())).isTrue();
        }

    }

    @Nested
    @DisplayName("extractPlacementByIdVerified")
    class ExtractPlacementByIdVerified {

        private final Category TEST_CATEGORY = Category.DINNER;
        private final int TEST_POSITION = 7;

        @BeforeEach
        void prepareTests() {
            prepareMyUser();
        }

        @Test
        @DisplayName("Returns the requested entry's placement data when the entry exists and belongs to the given user.")
        void placementFetched() {
            // Arrange
            FoodEntry entry = prepareFoodEntry(myUser, myDay);
            entry.setCategory(TEST_CATEGORY);
            entry.setPosition(TEST_POSITION);
            flushAndClear();

            // Act
            Optional<Placement> result = entryRepository.extractPlacementByIdVerified(myUser.getId(), entry.getId());

            // Assert
            assertThat(result).isPresent();
            Placement placement = result.get();

            assertThat(placement.getDayId()).isEqualTo(myDay.getId());
            assertThat(placement.getCategory()).isEqualTo(TEST_CATEGORY);
            assertThat(placement.getPosition()).isEqualTo(TEST_POSITION);
        }

        @Test
        @DisplayName("Returns empty when the requested entry does not exist.")
        void entryNotFound() {
            // Arrange
            flushAndClear();

            // Act
            Optional<Placement> result = entryRepository.extractPlacementByIdVerified(myUser.getId(), 999L);

            // Assert
            assertThat(result).isEmpty();
            assertThat(entryRepository.existsById(999L)).isFalse();
        }

        @Test
        @DisplayName("Returns empty when the requested entry exists but belongs to a different user.")
        void entryNotOwned() {
            // Arrange
            prepareOtherUser();
            FoodEntry entry = prepareFoodEntry(otherUser, otherDay);
            flushAndClear();

            // Act
            Optional<Placement> result = entryRepository.extractPlacementByIdVerified(myUser.getId(), entry.getId());

            // Assert
            assertThat(result).isEmpty();
            assertThat(entryRepository.existsById(entry.getId())).isTrue();
        }

    }

    @Nested
    @DisplayName("countByDayAndCategory")
    class CountByDayAndCategory {

        private final int TOTAL_COUNT = 3;
        private final Category TEST_CATEGORY = Category.BREAKFAST;
        private final Category EXCLUDED_CATEGORY = Category.SNACK;

        List<FoodEntry> prepareEntriesToCount(int amount, User owner, Day ownerDay) {
            List<FoodEntry> entries = new ArrayList<>(amount);

            for (int i = 0; i < amount; i++) {
                FoodEntry entry = prepareFoodEntry(owner, ownerDay);
                entry.setCategory(TEST_CATEGORY);
                entries.add(entry);
            }

            return entries;
        }

        @BeforeEach
        void prepareTests() {
            prepareMyUser();
        }

        @Test
        @DisplayName("Does not count entries that belong to the same category on a different day.")
        void excludeOtherDays() {
            // Arrange
            Day excludedDay = defaultDayBuilder().plan(myPlan).position(2).build();
            myPlan.getDays().add(excludedDay);
            entityManager.persist(excludedDay);

            List<FoodEntry> entries = prepareEntriesToCount(TOTAL_COUNT, myUser, myDay);
            entries.getLast().setDay(excludedDay);
            flushAndClear();

            // Act
            int result = entryRepository.countByDayAndCategory(myDay.getId(), TEST_CATEGORY);

            // Assert
            assertThat(result).isEqualTo(TOTAL_COUNT - 1);
            assertThat(entryRepository.count()).isEqualTo(TOTAL_COUNT);
        }

        @Test
        @DisplayName("Does not count entries that belong to a different category in the same day.")
        void excludeOtherCategories() {
            // Arrange
            List<FoodEntry> entries = prepareEntriesToCount(TOTAL_COUNT, myUser, myDay);
            entries.getLast().setCategory(EXCLUDED_CATEGORY);
            flushAndClear();

            // Act
            int result = entryRepository.countByDayAndCategory(myDay.getId(), TEST_CATEGORY);

            // Assert
            assertThat(result).isEqualTo(TOTAL_COUNT - 1);
            assertThat(entryRepository.count()).isEqualTo(TOTAL_COUNT);
        }

    }

    @Nested
    @DisplayName("sumSnapshotsByDayGroupedByCategory")
    class SumSnapshotsByDayGroupedByCategory {
        
        FoodEntry prepareFoodEntryToSum(
            Category category,
            double calories, double protein, double carbs,
            double fat, double fiber, double price
        ) {
            Food food = defaultFoodBuilder().user(myUser).build();
            FoodEntry entry = defaultFoodEntryBuilder()
                .day(myDay).food(food).category(category)
                .calories(calories).protein(protein).carbs(carbs)
                .fat(fat).fiber(fiber).price(price)
                .build();

            entityManager.persist(food);
            entityManager.persist(entry);

            return entry;
        }

        @Test
        @DisplayName("Correctly calculates sums per category for the given day.")
        void sumsCalculatedCorrectly() {
            // Arrange
            FoodEntry breakfast1 = prepareFoodEntryToSum(Category.BREAKFAST, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0);
            FoodEntry breakfast2 = prepareFoodEntryToSum(Category.BREAKFAST, 5.0, 7.0, 4.0, 8.0, 3.0, 7.0);
            FoodEntry dinner1 = prepareFoodEntryToSum(Category.DINNER, 20.0, 20.0, 20.0, 20.0, 20.0, 20.0);
            FoodEntry dinner2 = prepareFoodEntryToSum(Category.DINNER, 13.0, 18.0, 15.0, 5.0, 7.0, 12.0);
            flushAndClear();

            // Act
            List<CategoryStats> result = entryRepository.sumSnapshotsByDayGroupedByCategory(myDay.getId());

            // Assert
            assertThat(result).extracting(
                CategoryStats::getCategory,
                CategoryStats::getCalories,
                CategoryStats::getProtein,
                CategoryStats::getCarbs,
                CategoryStats::getFat,
                CategoryStats::getFiber,
                CategoryStats::getPrice
            ).contains(
                tuple(Category.BREAKFAST, 15.0, 17.0, 14.0, 18.0, 13.0, 17.0),
                tuple(Category.LUNCH, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                tuple(Category.DINNER, 33.0, 38.0, 35.0, 25.0, 27.0, 32.0)
            );
        }

        @Test
        @DisplayName("Only uses values from entries in the given day.")
        void otherDaysNotInvolved() {
            // Arrange
            FoodEntry breakfast1 = prepareFoodEntryToSum(Category.BREAKFAST, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0);
            FoodEntry breakfast2 = prepareFoodEntryToSum(Category.BREAKFAST, 5.0, 7.0, 4.0, 8.0, 3.0, 7.0);
            
            Day invalidDay = prepareMySecondDay();
            breakfast2.setDay(invalidDay);
            flushAndClear();

            // Act
            List<CategoryStats> result = entryRepository.sumSnapshotsByDayGroupedByCategory(myDay.getId());

            // Assert
            assertThat(result).extracting(                
                CategoryStats::getCategory,
                CategoryStats::getCalories,
                CategoryStats::getProtein,
                CategoryStats::getCarbs,
                CategoryStats::getFat,
                CategoryStats::getFiber,
                CategoryStats::getPrice
            ).contains(
                tuple(Category.BREAKFAST, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0);
            );
        }

    }

    @Nested
    @DisplayName("shiftUpByDayAndCategory")
    class ShiftUpByDayAndCategory {

        private final int TEST_COUNT = 10;
        private final Category TEST_CATEGORY = Category.SNACK;
        private final Category EXCLUDED_CATEGORY = Category.UNSORTED;

        @BeforeEach
        void prepareTests() {
            prepareMyUser();
        }

        @ParameterizedTest
        @MethodSource("org.example.mealplannerapp.repository.EntryRepositoryTests#provideBounds")
        @DisplayName("Only increments the position of entries within range.")
        void onlyEntriesWithinBoundsShifted(Integer minPosition, Integer maxPosition) {
            // Arrange
            List<FoodEntry> entries = new ArrayList<>(TEST_COUNT);

            for (int i = 1; i <= TEST_COUNT; i++) {
                FoodEntry entry = prepareFoodEntry(myUser, myDay);
                entry.setCategory(TEST_CATEGORY);
                entry.setPosition(i);
                entries.add(entry);
            }

            flushAndClear();

            // Act
            int result = entryRepository.shiftUpByDayAndCategory(myDay.getId(), TEST_CATEGORY, minPosition, maxPosition);

            // Assert
            int expected = (int) entries.stream()
                    .filter(e -> minPosition == null || e.getPosition() >= minPosition)
                    .filter(e -> maxPosition == null || e.getPosition() < maxPosition)
                    .count();
            assertThat(result).isEqualTo(expected);

            assertSoftly(softly -> {
                List<Long> ids = entries.stream().map(Entry::getId).toList();
                Map<Long, FoodEntry> fetchedEntries = entryRepository.findAllById(ids).stream()
                        .map(e -> (FoodEntry) e)
                        .collect(Collectors.toMap(FoodEntry::getId, Function.identity()));

                for (FoodEntry entry : entries) {
                    FoodEntry fetched = fetchedEntries.get(entry.getId());
                    int initialPosition = entry.getPosition();
                    if ((minPosition == null || initialPosition >= minPosition) &&
                            (maxPosition == null || initialPosition < maxPosition)) {
                        softly.assertThat(fetched.getPosition()).isEqualTo(initialPosition + 1);
                    } else {
                        softly.assertThat(fetched.getPosition()).isEqualTo(initialPosition);
                    }
                }
            });
        }

        @Test
        @DisplayName("Does not affect entries outside the given day.")
        void otherDaysExcluded() {
            // Arrange
            Day excludedDay = defaultDayBuilder().plan(myPlan).position(2).build();
            myPlan.getDays().add(excludedDay);
            entityManager.persist(excludedDay);

            FoodEntry entry = prepareFoodEntry(myUser, excludedDay);
            entry.setPosition(5);

            flushAndClear();

            // Act
            int result = entryRepository.shiftUpByDayAndCategory(myDay.getId(), TEST_CATEGORY, 3, 7);

            // Assert
            assertThat(result).isZero();

            FoodEntry fetched = (FoodEntry) entryRepository.findById(entry.getId()).get();
            assertThat(fetched.getPosition()).isEqualTo(entry.getPosition());

        }

        @Test
        @DisplayName("Does not affect entries outside the given category.")
        void otherCategoriesExcluded() {
            // Arrange
            FoodEntry entry = prepareFoodEntry(myUser, myDay);
            entry.setCategory(EXCLUDED_CATEGORY);
            entry.setPosition(5);

            flushAndClear();

            // Act
            int result = entryRepository.shiftUpByDayAndCategory(myDay.getId(), TEST_CATEGORY, 3, 7);

            // Assert
            assertThat(result).isZero();

            FoodEntry fetched = (FoodEntry) entryRepository.findById(entry.getId()).get();
            assertThat(fetched.getPosition()).isEqualTo(entry.getPosition());
        }

    }

    @Nested
    @DisplayName("shiftDownByDayAndCategory")
    class ShiftDownByDayAndCategory {

        private final int TEST_COUNT = 10;
        private final Category TEST_CATEGORY = Category.SNACK;
        private final Category EXCLUDED_CATEGORY = Category.UNSORTED;

        @BeforeEach
        void prepareTests() {
            prepareMyUser();
        }

        @ParameterizedTest
        @MethodSource("org.example.mealplannerapp.repository.EntryRepositoryTests#provideBounds")
        @DisplayName("Only decrements the position of entries within range.")
        void onlyEntriesWithinBoundsShifted(Integer minPosition, Integer maxPosition) {
            // Arrange
            List<FoodEntry> entries = new ArrayList<>(TEST_COUNT);

            for (int i = 1; i <= TEST_COUNT; i++) {
                FoodEntry entry = prepareFoodEntry(myUser, myDay);
                entry.setCategory(TEST_CATEGORY);
                entry.setPosition(i);
                entries.add(entry);
            }

            flushAndClear();

            // Act
            int result = entryRepository.shiftDownByDayAndCategory(myDay.getId(), TEST_CATEGORY, minPosition, maxPosition);

            // Assert
            int expected = (int) entries.stream()
                    .filter(e -> minPosition == null || e.getPosition() > minPosition)
                    .filter(e -> maxPosition == null || e.getPosition() <= maxPosition)
                    .count();
            assertThat(result).isEqualTo(expected);

            assertSoftly(softly -> {
                List<Long> ids = entries.stream().map(Entry::getId).toList();
                Map<Long, FoodEntry> fetchedEntries = entryRepository.findAllById(ids).stream()
                        .map(e -> (FoodEntry) e)
                        .collect(Collectors.toMap(FoodEntry::getId, Function.identity()));

                for (FoodEntry entry : entries) {
                    FoodEntry fetched = fetchedEntries.get(entry.getId());
                    int initialPosition = entry.getPosition();
                    if ((minPosition == null || initialPosition > minPosition) &&
                            (maxPosition == null || initialPosition <= maxPosition)) {
                        softly.assertThat(fetched.getPosition()).isEqualTo(initialPosition - 1);
                    } else {
                        softly.assertThat(fetched.getPosition()).isEqualTo(initialPosition);
                    }
                }
            });
        }

        @Test
        @DisplayName("Does not affect entries outside the given day.")
        void otherDaysExcluded() {
            // Arrange
            Day excludedDay = defaultDayBuilder().plan(myPlan).position(2).build();
            myPlan.getDays().add(excludedDay);
            entityManager.persist(excludedDay);

            FoodEntry entry = prepareFoodEntry(myUser, excludedDay);
            entry.setPosition(5);

            flushAndClear();

            // Act
            int result = entryRepository.shiftDownByDayAndCategory(myDay.getId(), TEST_CATEGORY, 3, 7);

            // Assert
            assertThat(result).isZero();

            FoodEntry fetched = (FoodEntry) entryRepository.findById(entry.getId()).get();
            assertThat(fetched.getPosition()).isEqualTo(entry.getPosition());

        }

        @Test
        @DisplayName("Does not affect entries outside the given category.")
        void otherCategoriesExcluded() {
            // Arrange
            FoodEntry entry = prepareFoodEntry(myUser, myDay);
            entry.setCategory(EXCLUDED_CATEGORY);
            entry.setPosition(5);

            flushAndClear();

            // Act
            int result = entryRepository.shiftDownByDayAndCategory(myDay.getId(), TEST_CATEGORY, 3, 7);

            // Assert
            assertThat(result).isZero();

            FoodEntry fetched = (FoodEntry) entryRepository.findById(entry.getId()).get();
            assertThat(fetched.getPosition()).isEqualTo(entry.getPosition());
        }

    }

    @Nested
    @DisplayName("deleteByIdVerified")
    class DeleteByIdVerified {

        @BeforeEach
        void prepareTests() {
            prepareMyUser();
        }

        @Test
        @DisplayName("Deletes the requested entry when it exists and belongs to the given user.")
        void entryDeleted() {
            // Arrange
            FoodEntry entry = prepareFoodEntry(myUser, myDay);
            flushAndClear();

            // Act
            int result = entryRepository.deleteByIdVerified(myUser.getId(), entry.getId());

            // Assert
            assertThat(result).isOne();
            assertThat(entryRepository.existsById(entry.getId())).isFalse();
        }

        @Test
        @DisplayName("Does nothing when the requested entry does not exist.")
        void entryNotFound() {
            // Arrange
            flushAndClear();

            // Act
            int result = entryRepository.deleteByIdVerified(myUser.getId(), 999L);

            // Assert
            assertThat(result).isZero();
            assertThat(entryRepository.existsById(999L)).isFalse();
        }

        @Test
        @DisplayName("Does nothing when the requested entry exists but belongs to a different user.")
        void entryNotOwned() {
            // Arrange
            prepareOtherUser();
            FoodEntry entry = prepareFoodEntry(otherUser, otherDay);
            flushAndClear();

            // Act
            int result = entryRepository.deleteByIdVerified(myUser.getId(), entry.getId());

            // Assert
            assertThat(result).isZero();
            assertThat(entryRepository.existsById(entry.getId())).isTrue();
        }

    }

    @Nested
    @DisplayName("deleteByDay")
    class DeleteByDay {
        
        @Test
        @DisplayName("Deletes all entries that belong to the given day and only those entries.")
        void correctEntriesDeleted() {
            // Arrange
            FoodEntry valid1 = prepareFoodEntry(myUser, myDay);
            ExerciseEntry valid2 = prepareExerciseEntry(myUser, myDay);

            Day invalidDay = prepareMySecondDay();
            FoodEntry invalid1 = prepareFoodEntry(myUser, invalidDay);

            entityManager.persist(valid1);
            entityManager.persist(valid2);
            entityManager.persist(invalid1);
            flushAndClear();

            // Act
            int result = entryRepository.deleteByDay(myDay.getId());

            // Assert
            assertThat(result).isEqualTo(2);
            assertThat(entryRepository.existsById(valid1.getId())).isFalse();
            assertThat(entryRepository.existsById(valid2.getId())).isFalse();
            assertThat(entryRepository.existsById(invalid1.getId())).isTrue();
        }

    }

}