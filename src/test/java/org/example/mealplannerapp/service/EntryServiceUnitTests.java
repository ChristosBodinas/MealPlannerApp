package org.example.mealplannerapp.service;

import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.dto.entry.request.DuplicateEntryRequest;
import org.example.mealplannerapp.dto.entry.request.create.CreateExerciseEntryRequest;
import org.example.mealplannerapp.dto.entry.request.create.CreateFoodEntryRequest;
import org.example.mealplannerapp.dto.entry.request.edit.EditExerciseEntryRequest;
import org.example.mealplannerapp.dto.entry.request.edit.EditFoodEntryRequest;
import org.example.mealplannerapp.dto.entry.response.EntryResponse;
import org.example.mealplannerapp.dto.entry.response.ExerciseEntryResponse;
import org.example.mealplannerapp.dto.entry.response.FoodEntryResponse;
import org.example.mealplannerapp.embeddable.EffortLevel;
import org.example.mealplannerapp.embeddable.ReferenceUnit;
import org.example.mealplannerapp.embeddable.VendorData;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Exercise;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.entity.entry.ExerciseEntry;
import org.example.mealplannerapp.entity.entry.FoodEntry;
import org.example.mealplannerapp.exception.InvalidReferenceException;
import org.example.mealplannerapp.exception.MappingMismatchException;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.mapper.EntryMapper;
import org.example.mealplannerapp.mapper.EntryMapperImpl;
import org.example.mealplannerapp.mapper.ExerciseMapperImpl;
import org.example.mealplannerapp.mapper.FoodMapperImpl;
import org.example.mealplannerapp.projection.Placement;
import org.example.mealplannerapp.repository.DayRepository;
import org.example.mealplannerapp.repository.EntryRepository;
import org.example.mealplannerapp.repository.ExerciseRepository;
import org.example.mealplannerapp.repository.FoodRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.example.mealplannerapp.fixture.DayTestFixtures.defaultDay;
import static org.example.mealplannerapp.fixture.EntryTestFixtures.defaultExerciseEntry;
import static org.example.mealplannerapp.fixture.EntryTestFixtures.defaultFoodEntry;
import static org.example.mealplannerapp.fixture.ExerciseTestFixtures.defaultExercise;
import static org.example.mealplannerapp.fixture.FoodTestFixtures.defaultFood;
import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUser;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EntryServiceUnitTests {

    // CONSTANT
    private static final long USER_ID = 1L;
    private static final long ENTRY_ID = 99L;
    private static final long DAY_ID = 88L;
    private static final long FOOD_ID = 77L;
    private static final long EXERCISE_ID = 66L;

    // BEANS
    private EntryService entryService;
    private EntryMapper entryMapper;

    @Mock
    private EntryRepository entryRepository;

    @Mock
    private DayRepository dayRepository;

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private ExerciseRepository exerciseRepository;

    // VARIABLES
    private User myUser;

    // HELPER METHODS

    // TESTS PROPER
    @BeforeEach
    void prepareServiceUser() {
        entryMapper = new EntryMapperImpl(new FoodMapperImpl(), new ExerciseMapperImpl());
        entryService = new EntryService(entryRepository, dayRepository,
                foodRepository, exerciseRepository, entryMapper);

        myUser = defaultUser().id(USER_ID).build();
    }

    @Nested
    @DisplayName("createEntry")
    class CreateEntry {

        private final Category TEST_CATEGORY = Category.LUNCH;
        private final int TEST_COUNT = 4;

        @Test
        @DisplayName("Given a non-existent or non-owned dayId, throws a ResourceNotFoundException.")
        void dayNotFound() {
            // Arrange
            CreateFoodEntryRequest request = CreateFoodEntryRequest.builder().build();

            when(dayRepository.fetchByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.createEntry(myUser, DAY_ID, request))
                    .as("Method should throw a ResourceNotFoundException.")
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Nested
        @DisplayName("with CreateFoodEntryRequest")
        class CreateFoodEntry {

            private final BigDecimal TEST_QUANTITY = BigDecimal.valueOf(100.0);
            private final String TEST_UNIT_NAME = "tbsp";
            private final ReferenceUnit TEST_UNIT = new ReferenceUnit(TEST_UNIT_NAME, BigDecimal.TEN);
            private final String TEST_VENDOR_NAME = "Masoutis";
            private final VendorData TEST_VENDOR = new VendorData(TEST_VENDOR_NAME, BigDecimal.TEN, BigDecimal.TEN);

            private CreateFoodEntryRequest request;

            private CreateFoodEntryRequest prepareRequestWithGrams() {
                return CreateFoodEntryRequest.builder()
                        .category(TEST_CATEGORY)
                        .foodId(FOOD_ID)
                        .quantity(TEST_QUANTITY)
                        .vendorName(TEST_VENDOR_NAME)
                        .build();
            }

            private CreateFoodEntryRequest prepareRequestWithUnit() {
                return CreateFoodEntryRequest.builder()
                        .category(TEST_CATEGORY)
                        .foodId(FOOD_ID)
                        .quantity(TEST_QUANTITY)
                        .unitName(TEST_UNIT_NAME)
                        .vendorName(TEST_VENDOR_NAME)
                        .build();
            }

            // TODO: Rewrite display names.

            @Test
            @DisplayName("Given an existing dayId owned by the current user and a valid request, " +
                    "creates a new FoodEntry, saves it to the database, and returns an FoodEntryResponse.")
            void foodEntryCreatedWithGrams() {
                // Arrange
                request = prepareRequestWithGrams();

                Day day = defaultDay().id(DAY_ID).build();
                Food food = defaultFood().id(EXERCISE_ID).user(myUser)
                        .units(new HashSet<>(Set.of(TEST_UNIT)))
                        .vendors(new HashSet<>(Set.of(TEST_VENDOR)))
                        .build();

                FoodEntry saved = defaultFoodEntry().id(ENTRY_ID)
                        .day(day).category(TEST_CATEGORY).position(TEST_COUNT + 1)
                        .food(food)
                        .build();
                saved.snapshotInfo();

                when(dayRepository.fetchByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.of(day));
                when(foodRepository.fetchByIdVerified(USER_ID, FOOD_ID)).thenReturn(Optional.of(food));
                when(entryRepository.countByDayAndCategory(DAY_ID, TEST_CATEGORY)).thenReturn(TEST_COUNT);
                when(entryRepository.save(any())).thenReturn(saved);

                // Act
                EntryResponse response = entryService.createEntry(myUser, DAY_ID, request);

                // Assert
                assertThat(response).as("Method should return a FoodEntryResponse.")
                        .isInstanceOf(FoodEntryResponse.class);

                assertThat(response).as("Method output should match mapper output.")
                        .isEqualTo(entryMapper.toResponse(saved));

                ArgumentCaptor<FoodEntry> captor = ArgumentCaptor.forClass(FoodEntry.class);
                verify(entryRepository).save(captor.capture());
                FoodEntry created = captor.getValue();

                assertThat(created.getUnitQuantity()).as("Unit quantity should be null.")
                        .isNull();
                assertThat(created.getGrams()).as("Grams should be equal to request quantity.")
                        .isEqualTo(TEST_QUANTITY);

                assertThat(created).as("Snapshot fields should have been set.")
                        .extracting(Entry::getName, Entry::getCalories, Entry::getProtein, Entry::getCarbs,
                                Entry::getFat, Entry::getFiber, Entry::getPrice)
                        .doesNotContainNull();

                assertThat(created).as("New entry has should have been placed at the right day, category, and position.")
                        .extracting(Entry::getDay, Entry::getCategory, Entry::getPosition)
                        .containsExactly(day, TEST_CATEGORY, TEST_COUNT + 1);
            }

            @Test
            @DisplayName("Given an existing dayId owned by the current user and a valid request, " +
                    "creates a new FoodEntry, saves it to the database, and returns an FoodEntryResponse.")
            void foodEntryCreatedWithUnit() {
                // Arrange
                request = prepareRequestWithUnit();

                Day day = defaultDay().id(DAY_ID).build();
                Food food = defaultFood().id(EXERCISE_ID).user(myUser)
                        .units(new HashSet<>(Set.of(TEST_UNIT)))
                        .vendors(new HashSet<>(Set.of(TEST_VENDOR)))
                        .build();

                FoodEntry saved = defaultFoodEntry().id(ENTRY_ID)
                        .day(day).category(TEST_CATEGORY).position(TEST_COUNT + 1)
                        .food(food)
                        .build();
                saved.snapshotInfo();

                when(dayRepository.fetchByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.of(day));
                when(foodRepository.fetchByIdVerified(USER_ID, FOOD_ID)).thenReturn(Optional.of(food));
                when(entryRepository.countByDayAndCategory(DAY_ID, TEST_CATEGORY)).thenReturn(TEST_COUNT);
                when(entryRepository.save(any())).thenReturn(saved);

                // Act
                EntryResponse response = entryService.createEntry(myUser, DAY_ID, request);

                // Assert
                assertThat(response).as("Method should return a FoodEntryResponse.")
                        .isInstanceOf(FoodEntryResponse.class);

                assertThat(response).as("Method output should match mapper output.")
                        .isEqualTo(entryMapper.toResponse(saved));

                ArgumentCaptor<FoodEntry> captor = ArgumentCaptor.forClass(FoodEntry.class);
                verify(entryRepository).save(captor.capture());
                FoodEntry created = captor.getValue();

                assertThat(created.getUnitQuantity()).as("Unit quantity should be equal to request quantity.")
                        .isEqualTo(TEST_QUANTITY);
                assertThat(created.getGrams()).as("Grams should be calculated properly.")
                        .isEqualTo(TEST_QUANTITY.multiply(BigDecimal.TEN));

                assertThat(created).as("Snapshot fields should have been set.")
                        .extracting(Entry::getName, Entry::getCalories, Entry::getProtein, Entry::getCarbs,
                                Entry::getFat, Entry::getFiber, Entry::getPrice)
                        .doesNotContainNull();

                assertThat(created).as("New entry has should have been placed at the right day, category, and position.")
                        .extracting(Entry::getDay, Entry::getCategory, Entry::getPosition)
                        .containsExactly(day, TEST_CATEGORY, TEST_COUNT + 1);
            }

            @Test
            @DisplayName("Given a non-existent or non-owned foodId, throws a ResourceNotFoundException.")
            void foodNotFound() {
                // Arrange
                request = prepareRequestWithUnit();

                Day day = defaultDay().id(DAY_ID).build();

                when(dayRepository.fetchByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.of(day));
                when(foodRepository.fetchByIdVerified(USER_ID, FOOD_ID)).thenReturn(Optional.empty());

                // Act + Assert
                assertThatThrownBy(() -> entryService.createEntry(myUser, DAY_ID, request))
                        .as("Method should throw a ResourceNotFoundException.")
                        .isInstanceOf(ResourceNotFoundException.class);

                verify(entryRepository, never().description("Nothing should be saved to the database."))
                        .save(any());
            }

            @Test
            @DisplayName("Given a non-null unitName that doesn't match the referenced food's unit names, " +
                    "throws an InvalidReferenceException.")
            void invalidUnitName() {
                // Arrange
                request = prepareRequestWithUnit();

                Day day = defaultDay().id(DAY_ID).build();
                Food food = defaultFood()
                        .id(FOOD_ID).user(myUser)
                        .units(new HashSet<>())
                        .build();

                when(dayRepository.fetchByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.of(day));
                when(foodRepository.fetchByIdVerified(USER_ID, FOOD_ID)).thenReturn(Optional.of(food));

                // Act + Assert
                assertThatThrownBy(() -> entryService.createEntry(myUser, DAY_ID, request))
                        .as("Method should throw an InvalidReferenceException.")
                        .isInstanceOf(InvalidReferenceException.class);

                verify(entryRepository, never().description("Nothing should be saved to the database."))
                        .save(any());
            }

            @Test
            @DisplayName("Given a non-null vendorName that doesn't match the referenced food's vendor names, " +
                    "throws an InvalidReferenceException.")
            void invalidVendorName() {
                // Arrange
                request = prepareRequestWithUnit();

                Day day = defaultDay().id(DAY_ID).build();
                Food food = defaultFood()
                        .id(FOOD_ID).user(myUser)
                        .vendors(new HashSet<>())
                        .build();

                when(dayRepository.fetchByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.of(day));
                when(foodRepository.fetchByIdVerified(USER_ID, FOOD_ID)).thenReturn(Optional.of(food));

                // Act + Assert
                assertThatThrownBy(() -> entryService.createEntry(myUser, DAY_ID, request))
                        .as("Method should throw an InvalidReferenceException.")
                        .isInstanceOf(InvalidReferenceException.class);

                verify(entryRepository, never().description("Nothing should be saved to the database."))
                        .save(any());
            }

        }

        @Nested
        @DisplayName("with CreateExerciseEntryRequest")
        class CreateExerciseEntry {

            private final BigDecimal TEST_DURATION = BigDecimal.valueOf(30.0);
            private final String TEST_LEVEL_NAME = "Slow";
            private final EffortLevel TEST_LEVEL = new EffortLevel(TEST_LEVEL_NAME, BigDecimal.TEN);

            private CreateExerciseEntryRequest request;

            @BeforeEach
            void prepareRequest() {
                request = CreateExerciseEntryRequest.builder()
                        .category(TEST_CATEGORY)
                        .exerciseId(EXERCISE_ID)
                        .duration(TEST_DURATION)
                        .levelName(TEST_LEVEL_NAME)
                        .build();
            }

            @Test
            @DisplayName("Given an existing dayId owned by the current user and a valid request, " +
                    "creates a new ExerciseEntry, saves it to the database, and returns an ExerciseEntryResponse.")
            void exerciseEntryCreated() {
                // Arrange
                Day day = defaultDay().id(DAY_ID).build();
                Exercise exercise = defaultExercise().id(EXERCISE_ID).user(myUser)
                        .levels(new HashSet<>(Set.of(TEST_LEVEL)))
                        .build();

                ExerciseEntry saved = defaultExerciseEntry().id(ENTRY_ID)
                        .day(day).category(TEST_CATEGORY).position(TEST_COUNT + 1)
                        .exercise(exercise)
                        .build();
                saved.snapshotInfo();

                when(dayRepository.fetchByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.of(day));
                when(exerciseRepository.fetchByIdVerified(USER_ID, EXERCISE_ID)).thenReturn(Optional.of(exercise));
                when(entryRepository.countByDayAndCategory(DAY_ID, TEST_CATEGORY)).thenReturn(TEST_COUNT);
                when(entryRepository.save(any())).thenReturn(saved);

                // Act
                EntryResponse response = entryService.createEntry(myUser, DAY_ID, request);

                // Assert
                assertThat(response).as("Method should return an ExerciseEntryResponse.")
                        .isInstanceOf(ExerciseEntryResponse.class);

                assertThat(response).as("Method output should match mapper output.")
                        .isEqualTo(entryMapper.toResponse(saved));

                ArgumentCaptor<ExerciseEntry> captor = ArgumentCaptor.forClass(ExerciseEntry.class);
                verify(entryRepository).save(captor.capture());
                Entry created = captor.getValue();

                assertThat(created).as("New entry has should have been placed at the right day, category, and position.")
                        .extracting(Entry::getDay, Entry::getCategory, Entry::getPosition)
                        .containsExactly(day, TEST_CATEGORY, TEST_COUNT + 1);

                assertThat(created).as("Snapshot fields should have been set.")
                        .extracting(Entry::getName, Entry::getCalories, Entry::getProtein, Entry::getCarbs,
                                Entry::getFat, Entry::getFiber, Entry::getPrice)
                        .doesNotContainNull();
            }

            @Test
            @DisplayName("Given a non-existent or non-owned exerciseId, throws a ResourceNotFoundException.")
            void exerciseNotFound() {
                // Arrange
                Day day = defaultDay().id(DAY_ID).build();

                when(dayRepository.fetchByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.of(day));
                when(exerciseRepository.fetchByIdVerified(USER_ID, EXERCISE_ID)).thenReturn(Optional.empty());

                // Act + Assert
                assertThatThrownBy(() -> entryService.createEntry(myUser, DAY_ID, request))
                        .as("Method should throw a ResourceNotFoundException.")
                        .isInstanceOf(ResourceNotFoundException.class);

                verify(entryRepository, never().description("Nothing should be saved to the database."))
                        .save(any());
            }

            @Test
            @DisplayName("Given a non-null levelName that doesn't match the referenced exercise's level names, " +
                    "throws an InvalidReferenceException.")
            void invalidLevelName() {
                // Arrange
                Day day = defaultDay().id(DAY_ID).build();
                Exercise exercise = defaultExercise()
                        .id(EXERCISE_ID).user(myUser)
                        .levels(new HashSet<>())
                        .build();

                when(dayRepository.fetchByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.of(day));
                when(exerciseRepository.fetchByIdVerified(USER_ID, EXERCISE_ID)).thenReturn(Optional.of(exercise));

                // Act + Assert
                assertThatThrownBy(() -> entryService.createEntry(myUser, DAY_ID, request))
                        .as("Method should throw an InvalidReferenceException.")
                        .isInstanceOf(InvalidReferenceException.class);

                verify(entryRepository, never().description("Nothing should be saved to the database."))
                        .save(any());
            }
        }

    }

    @Nested
    @DisplayName("duplicateEntry")
    class DuplicateEntry {

        private final int TEST_COUNT = 5;
        private final Category TEST_CATEGORY = Category.SNACK;
        private final DuplicateEntryRequest request = new DuplicateEntryRequest(ENTRY_ID, TEST_CATEGORY);

        @Test
        @DisplayName("Given existing and owned dayId and food entry Id, duplicate the entry onto the requested day, " +
                "save it to the database, and return a FoodEntryResponse.")
        void foodEntryDuplicated() {
            // Arrange
            Day day = defaultDay().id(DAY_ID).build();
            Food food = defaultFood().id(FOOD_ID).user(myUser).build();
            FoodEntry source = defaultFoodEntry().id(ENTRY_ID).food(food).build();

            FoodEntry saved = defaultFoodEntry().id(ENTRY_ID).food(food).build();

            when(dayRepository.fetchByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.of(day));
            when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(source));
            when(entryRepository.countByDayAndCategory(DAY_ID, TEST_CATEGORY)).thenReturn(TEST_COUNT);
            when(entryRepository.save(any(FoodEntry.class))).thenReturn(saved);

            // Act
            EntryResponse response = entryService.duplicateEntry(myUser, DAY_ID, request);

            // ASSERT
            assertThat(response).as("Method should return a FoodEntryResponse.")
                    .isInstanceOf(FoodEntryResponse.class);

            assertThat(response).as("Method output should match mapper output.")
                    .isEqualTo(entryMapper.toResponse(saved));

            ArgumentCaptor<FoodEntry> captor = ArgumentCaptor.forClass(FoodEntry.class);
            verify(entryRepository).save(captor.capture());
            FoodEntry copy = captor.getValue();

            assertThat(copy).as("Snapshot fields should have been set.")
                    .extracting(Entry::getName, Entry::getCalories, Entry::getProtein, Entry::getCarbs,
                            Entry::getFat, Entry::getFiber, Entry::getPrice)
                    .doesNotContainNull();

            assertThat(copy).as("New entry has should have been placed at the right day, category, and position.")
                    .extracting(Entry::getDay, Entry::getCategory, Entry::getPosition)
                    .containsExactly(day, TEST_CATEGORY, TEST_COUNT + 1);
        }

        @Test
        @DisplayName("Given the id of a FoodEntry referencing a deleted food, throws an InvalidReferenceException.")
        void foodHasBeenDeleted() {
            // Arrange
            Day day = defaultDay().id(DAY_ID).build();
            FoodEntry source = defaultFoodEntry().id(ENTRY_ID).food(null).build();

            when(dayRepository.fetchByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.of(day));
            when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(source));

            // Act + Assert
            assertThatThrownBy(() -> entryService.duplicateEntry(myUser, DAY_ID, request))
                    .as("Method should throw an InvalidReferenceException.")
                    .isInstanceOf(InvalidReferenceException.class);

            verify(entryRepository, never().description("Nothing should be saved to the database."))
                    .save(any());
        }

        @Test
        @DisplayName("Given existing and owned dayId and exercise entry Id, duplicate the entry onto the requested day, " +
                "save it to the database, and return an ExerciseEntryResponse.")
        void exerciseEntryDuplicated() {
            // Arrange
            Day day = defaultDay().id(DAY_ID).build();
            Exercise exercise = defaultExercise().id(EXERCISE_ID).user(myUser).build();
            ExerciseEntry source = defaultExerciseEntry().id(ENTRY_ID).exercise(exercise).build();

            ExerciseEntry saved = defaultExerciseEntry().id(ENTRY_ID).exercise(exercise).build();

            when(dayRepository.fetchByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.of(day));
            when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(source));
            when(entryRepository.countByDayAndCategory(DAY_ID, TEST_CATEGORY)).thenReturn(TEST_COUNT);
            when(entryRepository.save(any(ExerciseEntry.class))).thenReturn(saved);

            // Act
            EntryResponse response = entryService.duplicateEntry(myUser, DAY_ID, request);

            // ASSERT
            assertThat(response).as("Method should return an ExerciseEntryResponse.")
                    .isInstanceOf(ExerciseEntryResponse.class);

            assertThat(response).as("Method output should match mapper output.")
                    .isEqualTo(entryMapper.toResponse(saved));

            ArgumentCaptor<ExerciseEntry> captor = ArgumentCaptor.forClass(ExerciseEntry.class);
            verify(entryRepository).save(captor.capture());
            ExerciseEntry copy = captor.getValue();

            assertThat(copy).as("Snapshot fields should have been set.")
                    .extracting(Entry::getName, Entry::getCalories, Entry::getProtein, Entry::getCarbs,
                            Entry::getFat, Entry::getFiber, Entry::getPrice)
                    .doesNotContainNull();

            assertThat(copy).as("New entry has should have been placed at the right day, category, and position.")
                    .extracting(Entry::getDay, Entry::getCategory, Entry::getPosition)
                    .containsExactly(day, TEST_CATEGORY, TEST_COUNT + 1);
        }

        @Test
        @DisplayName("Given the id of an ExerciseEntry referencing a deleted exercise, throws an InvalidReferenceException.")
        void exerciseHasBeenDeleted() {
            // Arrange
            Day day = defaultDay().id(DAY_ID).build();
            ExerciseEntry source = defaultExerciseEntry().id(ENTRY_ID).exercise(null).build();

            when(dayRepository.fetchByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.of(day));
            when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(source));

            // Act + Assert
            assertThatThrownBy(() -> entryService.duplicateEntry(myUser, DAY_ID, request))
                    .as("Method should throw an InvalidReferenceException.")
                    .isInstanceOf(InvalidReferenceException.class);

            verify(entryRepository, never().description("Nothing should be saved to the database."))
                    .save(any());
        }

        @Test
        @DisplayName("Given a non-existent or non-owned dayId, throws a ResourceNotFoundException.")
        void dayNotFound() {
            // Arrange
            when(dayRepository.fetchByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.duplicateEntry(myUser, DAY_ID, request))
                    .as("Method should throw a ResourceNotFoundException.")
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(entryRepository, never().description("Nothing should be saved to the database."))
                    .save(any());
        }
    }

    @Nested
    @DisplayName("editEntrry")
    class EditEntry {

        /**
         * Sets the given {@link Entry}'s snapshot fields to arbitrary, stale values.
         * Used for testing that snapshotInfo has been properly called.
         */
        private void snapshotStaleInfo(Entry entry) {
            entry.setName("STALE");
            entry.setCalories(BigDecimal.ONE.negate());
            entry.setProtein(BigDecimal.ONE.negate());
            entry.setCarbs(BigDecimal.ONE.negate());
            entry.setFat(BigDecimal.ONE.negate());
            entry.setFiber(BigDecimal.ONE.negate());
            entry.setProtein(BigDecimal.ONE.negate());
        }

        @Test
        @DisplayName("Given a non-existent or non-owned entryId, throws a ResourceNotFoundException.")
        void entryNotFound() {
            // Arrange
            EditFoodEntryRequest request = EditFoodEntryRequest.builder().build();

            when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.editEntry(myUser, ENTRY_ID, request))
                    .as("Method should throw a ResourceNotFoundException.")
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Nested
        @DisplayName("with EditFoodEntryRequest")
        class EditFoodEntry {

            private EditFoodEntryRequest request;

            private FoodEntry prepareFoodEntry() {
                Food food = defaultFood()
                        .id(FOOD_ID).user(myUser)
                        .units(new HashSet<>(Set.of(
                                new ReferenceUnit("tbsp", BigDecimal.ONE),
                                new ReferenceUnit("cup", BigDecimal.TWO)
                        )))
                        .vendors(new HashSet<>(Set.of(
                                new VendorData("Masoutis", BigDecimal.ONE, BigDecimal.TEN),
                                new VendorData("Lidl", BigDecimal.TWO, BigDecimal.TEN)
                        )))
                        .build();

                FoodEntry entry = defaultFoodEntry()
                        .id(ENTRY_ID)
                        .food(food)
                        .grams(BigDecimal.TEN)
                        .unitQuantity(BigDecimal.TEN)
                        .unitName("tbsp")
                        .vendorName("Masoutis")
                        .build();

                snapshotStaleInfo(entry);
                return entry;
            }

            @Test
            @DisplayName("PLACEHOLDER EDIT WITH GRAMS")
            void foodEntryEditedWithGrams() {
                // Arrange
                FoodEntry entry = prepareFoodEntry();
                request = EditFoodEntryRequest.builder()
                        .quantity(entry.getGrams().add(BigDecimal.TEN))
                        .unitName(null)
                        .vendorName("Lidl")
                        .build();

                when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(entry));

                // Act
                EntryResponse response = entryService.editEntry(myUser, ENTRY_ID, request);

                // Assert
                assertThat(response).as("Method should return a FoodEntryResponse.")
                        .isInstanceOf(FoodEntryResponse.class);

                assertThat(response).as("Method output should match mapper output.")
                        .isEqualTo(entryMapper.toResponse(entry));

                assertThat(entry).as("Grams, unitQuantity, unitName, and levelName should be updated.")
                        .extracting(FoodEntry::getGrams, FoodEntry::getUnitQuantity, FoodEntry::getUnitName, FoodEntry::getVendorName)
                        .containsExactly(request.quantity(), null, null, request.vendorName());

                FoodEntry stale = prepareFoodEntry();

                assertAll("Snapshot fields should be properly set.",
                        () -> assertThat(entry.getName()).as("name").isNotEqualTo(stale.getName()),
                        () -> assertThat(entry.getCalories()).as("calories").isNotEqualTo(stale.getCalories()),
                        () -> assertThat(entry.getProtein()).as("protein").isNotEqualTo(stale.getProtein()),
                        () -> assertThat(entry.getCarbs()).as("carbs").isNotEqualTo(stale.getCarbs()),
                        () -> assertThat(entry.getFat()).as("fat").isNotEqualTo(stale.getFat()),
                        () -> assertThat(entry.getFiber()).as("fiber").isNotEqualTo(stale.getFiber()),
                        () -> assertThat(entry.getPrice()).as("price").isNotEqualTo(stale.getPrice())
                );
            }

            @Test
            @DisplayName("PLACEHOLDER EDIT WITH UNIT")
            void foodEntryEditedWithUnit() {
                // Arrange
                FoodEntry entry = prepareFoodEntry();
                request = EditFoodEntryRequest.builder()
                        .quantity(entry.getUnitQuantity().add(BigDecimal.TEN))
                        .unitName("cup")
                        .vendorName("Lidl")
                        .build();

                when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(entry));

                // Act
                EntryResponse response = entryService.editEntry(myUser, ENTRY_ID, request);

                // Assert
                assertThat(response).as("Method should return a FoodEntryResponse.")
                        .isInstanceOf(FoodEntryResponse.class);

                assertThat(response).as("Method output should match mapper output.")
                        .isEqualTo(entryMapper.toResponse(entry));

                assertThat(entry).as("Grams, unitQuantity, unitName, and levelName should be updated.")
                        .extracting(FoodEntry::getGrams, FoodEntry::getUnitQuantity, FoodEntry::getUnitName, FoodEntry::getVendorName)
                        .containsExactly(request.quantity().multiply(entry.getFood().mapUnitsToGrams().get(request.unitName())),
                                request.quantity(), request.unitName(), request.vendorName());

                FoodEntry stale = prepareFoodEntry();

                assertAll("Snapshot fields should be properly set.",
                        () -> assertThat(entry.getName()).as("name").isNotEqualTo(stale.getName()),
                        () -> assertThat(entry.getCalories()).as("calories").isNotEqualTo(stale.getCalories()),
                        () -> assertThat(entry.getProtein()).as("protein").isNotEqualTo(stale.getProtein()),
                        () -> assertThat(entry.getCarbs()).as("carbs").isNotEqualTo(stale.getCarbs()),
                        () -> assertThat(entry.getFat()).as("fat").isNotEqualTo(stale.getFat()),
                        () -> assertThat(entry.getFiber()).as("fiber").isNotEqualTo(stale.getFiber()),
                        () -> assertThat(entry.getPrice()).as("price").isNotEqualTo(stale.getPrice())
                );
            }

            @Test
            @DisplayName("Given the Id of a food entry referencing a deleted food, throws an InvalidReferenceException.")
            void foodHasBeenDeleted() {
                // Arrange
                request = EditFoodEntryRequest.builder().build();
                FoodEntry entry = defaultFoodEntry().id(ENTRY_ID).food(null).build();

                when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(entry));

                // Act + Assert
                assertThatThrownBy(() -> entryService.editEntry(myUser, ENTRY_ID, request))
                        .as("Method should throw an InvalidReferenceException.")
                        .isInstanceOf(InvalidReferenceException.class);

                FoodEntry original = defaultFoodEntry().id(ENTRY_ID).food(null).build();
                assertThat(entry).as("Requested entry should remain unchanged.")
                        .usingRecursiveComparison()
                        .isEqualTo(original);
            }

            @Test
            @DisplayName("Given a non-null unitName that doesn't match the referenced food's unit names, " +
                    "throws an InvalidReferenceException.")
            void invalidUnitName() {
                // Arrange
                FoodEntry entry = prepareFoodEntry();
                request = EditFoodEntryRequest.builder()
                        .quantity(entry.getUnitQuantity().add(BigDecimal.TWO))
                        .unitName(entry.getUnitName() + "_edited")
                        .vendorName(entry.getVendorName())
                        .build();

                when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(entry));

                // Act + Assert
                assertThatThrownBy(() -> entryService.editEntry(myUser, ENTRY_ID, request))
                        .as("Method should throw an InvalidReferenceException.")
                        .isInstanceOf(InvalidReferenceException.class);

                FoodEntry original = prepareFoodEntry();
                assertThat(entry).as("Requested entry should remain unchanged.")
                        .usingRecursiveComparison()
                        .isEqualTo(original);
            }

            @Test
            @DisplayName("Given a non-null vendorName that doesn't match the referenced food's vendor names, " +
                    "throws an InvalidReferenceException.")
            void invalidVendorName() {
                // Arrange
                FoodEntry entry = prepareFoodEntry();
                request = EditFoodEntryRequest.builder()
                        .quantity(entry.getUnitQuantity().add(BigDecimal.TWO))
                        .unitName(entry.getUnitName())
                        .vendorName(entry.getVendorName() + "_edited")
                        .build();

                when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(entry));

                // Act + Assert
                assertThatThrownBy(() -> entryService.editEntry(myUser, ENTRY_ID, request))
                        .as("Method should throw an InvalidReferenceException.")
                        .isInstanceOf(InvalidReferenceException.class);

                FoodEntry original = prepareFoodEntry();
                assertThat(entry).as("Requested entry should remain unchanged.")
                        .usingRecursiveComparison()
                        .isEqualTo(original);
            }

            @Test
            @DisplayName("Given an existing exercise entry Id owned by the current user, throws a MappingMismatchException.")
            void mappingMismatch() {
                // Arrange
                Exercise exercise = defaultExercise().id(EXERCISE_ID).user(myUser).build();
                ExerciseEntry entry = defaultExerciseEntry().id(ENTRY_ID).exercise(exercise).build();
                request = EditFoodEntryRequest.builder().build();

                when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(entry));

                // Act + Assert
                assertThatThrownBy(() -> entryService.editEntry(myUser, ENTRY_ID, request))
                        .as("Method should throw a MappingMismatchException.")
                        .isInstanceOf(MappingMismatchException.class);

                ExerciseEntry original = defaultExerciseEntry().id(ENTRY_ID).exercise(exercise).build();
                assertThat(entry).as("Requested entry should remain unchanged.")
                        .usingRecursiveComparison()
                        .isEqualTo(original);
            }

        }

        @Nested
        @DisplayName("with EditExerciseEntryRequest")
        class EditExerciseEntry {

            private EditExerciseEntryRequest request;

            private ExerciseEntry prepareExerciseEntry() {
                Exercise exercise = defaultExercise()
                        .id(EXERCISE_ID).user(myUser)
                        .levels(new HashSet<>(Set.of(
                                new EffortLevel("Slow", BigDecimal.ONE),
                                new EffortLevel("Fast", BigDecimal.TWO)
                        )))
                        .build();

                ExerciseEntry entry = defaultExerciseEntry()
                        .id(ENTRY_ID)
                        .exercise(exercise)
                        .levelName("Slow")
                        .build();

                snapshotStaleInfo(entry);
                return entry;
            }

            @Test
            @DisplayName("Given an existing exercise entry Id owned by the current user, updates the requested entry " +
                    "and returns an ExerciseEntryResponse.")
            void exerciseEntryEdited() {
                // Arrange
                ExerciseEntry entry = prepareExerciseEntry();
                request = EditExerciseEntryRequest.builder()
                        .duration(entry.getDuration().add(BigDecimal.TEN))
                        .levelName("Fast")
                        .build();

                when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(entry));

                // Act
                EntryResponse response = entryService.editEntry(myUser, ENTRY_ID, request);

                // Assert
                assertThat(response).as("Method should return an ExerciseEntryResponse.")
                        .isInstanceOf(ExerciseEntryResponse.class);
                assertThat(response).as("Method output should match mapper output.")
                        .isEqualTo(entryMapper.toResponse(entry));

                assertThat(entry).as("Duration and levelName should be updated.")
                        .extracting(ExerciseEntry::getDuration, ExerciseEntry::getLevelName)
                        .containsExactly(request.duration(), request.levelName());

                ExerciseEntry stale = prepareExerciseEntry();

                assertAll("Snapshot fields should be properly set.",
                        () -> assertThat(entry.getName()).as("name").isNotEqualTo(stale.getName()),
                        () -> assertThat(entry.getCalories()).as("calories").isNotEqualTo(stale.getCalories()),
                        () -> assertThat(entry.getProtein()).as("protein").isNotEqualTo(stale.getProtein()),
                        () -> assertThat(entry.getCarbs()).as("carbs").isNotEqualTo(stale.getCarbs()),
                        () -> assertThat(entry.getFat()).as("fat").isNotEqualTo(stale.getFat()),
                        () -> assertThat(entry.getFiber()).as("fiber").isNotEqualTo(stale.getFiber()),
                        () -> assertThat(entry.getPrice()).as("price").isNotEqualTo(stale.getPrice())
                );
            }

            @Test
            @DisplayName("Given the Id of an exercise entry referencing a deleted exercise, throws an InvalidReferenceException.")
            void exerciseHasBeenDeleted() {
                // Arrange
                request = EditExerciseEntryRequest.builder().build();
                ExerciseEntry entry = defaultExerciseEntry().id(ENTRY_ID).exercise(null).build();

                when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(entry));

                // Act + Assert
                assertThatThrownBy(() -> entryService.editEntry(myUser, ENTRY_ID, request))
                        .as("Method should throw an InvalidReferenceException.")
                        .isInstanceOf(InvalidReferenceException.class);

                ExerciseEntry original = defaultExerciseEntry().id(ENTRY_ID).exercise(null).build();
                assertThat(entry).as("Requested entry should remain unchanged.")
                        .usingRecursiveComparison()
                        .isEqualTo(original);
            }

            @Test
            @DisplayName("Given a non-null levelName that doesn't match the referenced exercise's level names, " +
                    "throws an InvalidReferenceException.")
            void invalidLevelName() {
                // Arrange
                ExerciseEntry entry = prepareExerciseEntry();
                request = EditExerciseEntryRequest.builder()
                        .duration(entry.getDuration().add(BigDecimal.TEN))
                        .levelName(entry.getLevelName() + "_edited")
                        .build();

                when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(entry));

                // Act + Assert
                assertThatThrownBy(() -> entryService.editEntry(myUser, ENTRY_ID, request))
                        .as("Method should throw an InvalidReferenceException.")
                        .isInstanceOf(InvalidReferenceException.class);

                ExerciseEntry original = prepareExerciseEntry();
                assertThat(entry).as("Requested entry should remain unchanged.")
                        .usingRecursiveComparison()
                        .isEqualTo(original);
            }

            @Test
            @DisplayName("Given an existing food entry Id owned by the current user, throws a MappingMismatchException.")
            void mappingMismatch() {
                // Arrange
                Food food = defaultFood().id(FOOD_ID).user(myUser).build();
                FoodEntry entry = defaultFoodEntry().id(ENTRY_ID).food(food).build();
                request = EditExerciseEntryRequest.builder().build();

                when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(entry));

                // Act + Assert
                assertThatThrownBy(() -> entryService.editEntry(myUser, ENTRY_ID, request))
                        .as("Method should throw a MappingMismatchException.")
                        .isInstanceOf(MappingMismatchException.class);

                FoodEntry original = defaultFoodEntry().id(ENTRY_ID).food(food).build();
                assertThat(entry).as("Requested entry should remain unchanged.")
                        .usingRecursiveComparison()
                        .isEqualTo(original);
            }

        }

    }

    @Nested
    @DisplayName("deleteEntry")
    class DeleteEntry {

        private final Category TEST_CATEGORY = Category.DINNER;
        private final int TEST_POSITION = 5;

        @Test
        @DisplayName("Given an existing entryId owned by the current user, deletes the entry and closes the resulting gap.")
        void entryDeleted() {
            // Arrange
            Placement extracted = new Placement(DAY_ID, TEST_CATEGORY, TEST_POSITION);
            when(entryRepository.extractPlacementByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(extracted));

            // Act + Assert
            assertThatCode(() -> entryService.deleteEntry(myUser, ENTRY_ID))
                    .as("Method should throw no exceptions.")
                    .doesNotThrowAnyException();

            verify(entryRepository, description("Placement should be extracted from the database."))
                    .extractPlacementByIdVerified(USER_ID, ENTRY_ID);
            verify(entryRepository, description("Requested entry should be deleted."))
                    .deleteById(ENTRY_ID);
            verify(entryRepository, description("Gap should be closed after the deletion."))
                    .closeGapAfterRemoval(DAY_ID, TEST_CATEGORY, TEST_POSITION);
        }

        @Test
        @DisplayName("Given a non-existent or non-owned entryId, throws a ResourceNotFoundException.")
        void entryNotFound() {
            // Arrange
            when(entryRepository.extractPlacementByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.deleteEntry(myUser, ENTRY_ID))
                    .as("Method should throw a ResourceNotFoundException.")
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(entryRepository, never().description("No entry should be deleted."))
                    .deleteById(anyLong());
            verify(entryRepository, never().description("No entries should be moved."))
                    .closeGapAfterRemoval(anyLong(), any(Category.class), anyInt());
        }

    }

    @Nested
    @DisplayName("retrieveEntry")
    class RetrieveEntry {

        // TODO: Test handling of deleted references?

        @Test
        @DisplayName("Given an existing food entry ID owned by the current user, returns a FoodEntryResponse.")
        void foodEntryRetrieved() {
            // Arrange
            FoodEntry found = defaultFoodEntry().id(ENTRY_ID).build();

            when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(found));

            // Act
            EntryResponse response = entryService.retrieveEntry(myUser, ENTRY_ID);

            // Assert
            assertThat(response).as("Method should return a FoodEntryResponse.")
                    .isInstanceOf(FoodEntryResponse.class);

            assertThat(response).as("Method output should match mapper output for the given entry.")
                    .isEqualTo(entryMapper.toResponse(found));
        }

        @Test
        @DisplayName("Given an existing exercise entry ID owned by the current user, returns an ExerciseEntryResponse.")
        void exerciseEntryRetrieved() {
            // Arrange
            ExerciseEntry found = defaultExerciseEntry().id(ENTRY_ID).build();

            when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(found));

            // Act
            EntryResponse response = entryService.retrieveEntry(myUser, ENTRY_ID);

            // Assert
            assertThat(response).as("Method should return an ExerciseEntryResponse.")
                    .isInstanceOf(ExerciseEntryResponse.class);

            assertThat(response).as("Method output should match mapper output for the given entry.")
                    .isEqualTo(entryMapper.toResponse(found));
        }

        @Test
        @DisplayName("Given a non-existent or non-owned entryId, throws a ResourceNotFoundException.")
        void entryNotFound() {
            // Arrange
            when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.retrieveEntry(myUser, ENTRY_ID))
                    .as("Method should throw a ResourceNotFoundException.")
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

}
