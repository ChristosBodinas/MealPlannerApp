package org.example.mealplannerapp.service;

import org.example.mealplannerapp.common.Category;
import org.example.mealplannerapp.dto.entry.request.EntryDuplicateRequest;
import org.example.mealplannerapp.dto.entry.request.EntryMoveRequest;
import org.example.mealplannerapp.dto.entry.request.create.ExerciseEntryCreateRequest;
import org.example.mealplannerapp.dto.entry.request.create.FoodEntryCreateRequest;
import org.example.mealplannerapp.dto.entry.request.edit.ExerciseEntryEditRequest;
import org.example.mealplannerapp.dto.entry.request.edit.FoodEntryEditRequest;
import org.example.mealplannerapp.dto.entry.response.EntryResponse;
import org.example.mealplannerapp.dto.entry.response.ExerciseEntryResponse;
import org.example.mealplannerapp.dto.entry.response.FoodEntryResponse;
import org.example.mealplannerapp.embeddable.ExerciseLevel;
import org.example.mealplannerapp.embeddable.FoodPrice;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Exercise;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.Plan;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.entity.entry.ExerciseEntry;
import org.example.mealplannerapp.entity.entry.FoodEntry;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.exception.ServiceValidationException;
import org.example.mealplannerapp.mapper.EntryMapper;
import org.example.mealplannerapp.mapper.EntryMapperImpl;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.example.mealplannerapp.fixture.DayTestFixtures.defaultDayBuilder;
import static org.example.mealplannerapp.fixture.EntryTestFixtures.*;
import static org.example.mealplannerapp.fixture.FoodTestFixtures.defaultFoodBuilder;
import static org.example.mealplannerapp.fixture.ExerciseTestFixtures.defaultExerciseBuilder;
import static org.example.mealplannerapp.fixture.PlanTestFixtures.defaultPlanBuilder;
import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUserBuilder;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EntryServiceUnitTests {

    // MOCKS, SPIES, CAPTORS
    @Mock
    private EntryRepository entryRepository;
    @Mock
    private DayRepository dayRepository;
    @Mock
    private FoodRepository foodRepository;
    @Mock
    private ExerciseRepository exerciseRepository;
    @Captor
    ArgumentCaptor<Entry> entryCaptor;

    // VARIABLES
    private EntryMapper entryMapper;
    private EntryService entryService;
    private User myUser;
    private Plan myPlan;
    private Day myDay;

    // CONSTANTS - TEST ENTITY IDS
    private static final long USER_ID = 1L;
    private static final long FOOD_ID = 99L;
    private static final long EXERCISE_ID = 44L;
    private static final long ENTRY_ID = 88L;
    private static final long DAY_ID = 77L;
    private static final long OTHER_DAY_ID = 78L;
    private static final long PLAN_ID = 66L;

    // CONSTANTS - TEST FOOD VALUES
    private static final double TEST_CALORIES_PER_100G = 123.0;
    private static final double TEST_PROTEIN_PER_100G = 24.0;
    private static final double TEST_CARBS_PER_100G = 56.0;
    private static final double TEST_FAT_PER_100G = 8.5;
    private static final double TEST_FIBER_PER_100G = 4.5;
    private static final double TEST_EDIBLE_RATIO = 0.9;
    private static final String TEST_VENDOR = "Masoutis";
    private static final double TEST_PURCHASE_PRICE = 6.00;
    private static final double TEST_PURCHASE_GRAMS = 500;

    // CONSTANTS - TEST EXERCISE VALUES
    private static final String TEST_LEVEL = "Moderate";
    private static final double TEST_CALORIES_PER_MINUTE = 12.0;

    // CONSTANTS - TEST ENTRY VALUES
    private static final Category TEST_CATEGORY = Category.LUNCH;
    private static final Category OTHER_CATEGORY = Category.DINNER;
    private static final int TEST_POSITION = 5;
    private static final int HIGHER_POSITION = 7;
    private static final int LOWER_POSITION = 3;
    private static final int TEST_COUNT = 10;
    private static final int OTHER_COUNT = 12;

    private static final double TEST_GRAMS = 110.0;
    private static final double OTHER_GRAMS = 85.0;
    private static final String OTHER_VENDOR = "Sklavenitis";
    private static final String TEST_UNIT = "tbsp";
    private static final String OTHER_UNIT = "cup";

    private static final double TEST_DURATION = 30.0;
    private static final double OTHER_DURATION = 45.0;
    private static final String OTHER_LEVEL = "Olympic";

    // HELPER METHODS

    /**
     * Method for creating a simple FoodEntry and its referenced Food, using the default food and entry IDs,
     * and owned by myDay and myUser respectively.
     */
    private FoodEntry prepareMyFoodEntry() {
        Food food = defaultFoodBuilder().id(FOOD_ID).user(myUser).build();
        return defaultFoodEntryBuilder().id(ENTRY_ID).day(myDay).food(food).build();
    }

    private ExerciseEntry prepareMyExerciseEntry() {
        Exercise exercise = defaultExerciseBuilder().id(EXERCISE_ID).user(myUser).build();
        return defaultExerciseEntryBuilder().id(ENTRY_ID).day(myDay).exercise(exercise).build();
    }

    /**
     * Method for creating a Food to be used in testing FoodEntry snapshot calculation.
     */
    private Food prepareFoodWithTestValues() {
        return defaultFoodBuilder()
                .id(FOOD_ID)
                .user(myUser)
                .caloriesPer100g(TEST_CALORIES_PER_100G)
                .proteinPer100g(TEST_PROTEIN_PER_100G)
                .carbsPer100g(TEST_CARBS_PER_100G)
                .fatPer100g(TEST_FAT_PER_100G)
                .fiberPer100g(TEST_FIBER_PER_100G)
                .edibleRatio(TEST_EDIBLE_RATIO)
                .prices(new HashSet<>(Set.of(new FoodPrice(TEST_VENDOR, TEST_PURCHASE_PRICE, TEST_PURCHASE_GRAMS))))
                .build();
    }

    /**
     * Method for creating a FoodEntry to be used in testing FoodEntry snapshot calculation.
     */
    private FoodEntry prepareFoodEntryWithExpectedValues(Food food) {
        return defaultFoodEntryBuilder()
                .id(ENTRY_ID)
                .day(myDay)
                .category(TEST_CATEGORY)
                .position(TEST_COUNT + 1)
                .calories(TEST_CALORIES_PER_100G * TEST_GRAMS / 100.0)
                .protein(TEST_PROTEIN_PER_100G * TEST_GRAMS / 100.0)
                .carbs(TEST_CARBS_PER_100G * TEST_GRAMS / 100.0)
                .fat(TEST_FAT_PER_100G * TEST_GRAMS / 100.0)
                .fiber(TEST_FIBER_PER_100G * TEST_GRAMS / 100.0)
                .price((TEST_PURCHASE_PRICE / (TEST_PURCHASE_GRAMS * TEST_EDIBLE_RATIO)) * TEST_GRAMS)
                .food(food)
                .grams(TEST_GRAMS)
                .unit(TEST_UNIT)
                .vendor(TEST_VENDOR)
                .build();
    }

    /**
     * Method for creating an Exercise to be used in testing ExerciseEntry snapshot calculation.
     */
    private Exercise prepareExerciseWithTestValues() {
        return defaultExerciseBuilder()
            .id(EXERCISE_ID)
            .user(myUser)
            .levels(new HashSet<>(Set.of(new ExerciseLevel(TEST_LEVEL, TEST_CALORIES_PER_MINUTE))))
            .build();
    }

    /**
     * Method for creating an ExerciseEntry to be used in testing ExerciseEntry snapshot calculation.
     */
    private ExerciseEntry prepareExerciseEntryWithExpectedValues(Exercise exercise) {
            return defaultExerciseEntryBuilder()
                .id(ENTRY_ID)
                .day(myDay)
                .category(TEST_CATEGORY)
                .position(TEST_COUNT + 1)
                .calories(-1 * TEST_CALORIES_PER_MINUTE * TEST_DURATION)
                .protein(0.0)
                .carbs(0.0)
                .fat(0.0)
                .fiber(0.0)
                .price(0.0)
                .exercise(exercise)
                .duration(TEST_DURATION)
                .level(TEST_LEVEL)
                .build();
    }

    // BEFORE EACH
    @BeforeEach
    void prepareAllTests() {
        myUser = defaultUserBuilder().id(USER_ID).build();
        myPlan = defaultPlanBuilder().id(PLAN_ID).user(myUser).build();
        myDay = defaultDayBuilder().id(DAY_ID).plan(myPlan).build();
        myPlan.getDays().add(myDay);

        entryMapper = new EntryMapperImpl();
        entryService = new EntryService(
                entryRepository,
                dayRepository,
                foodRepository,
                exerciseRepository,
                entryMapper);
    }

    // TESTS PROPER
    @Nested
    @DisplayName("createEntry")
    class CreateEntry {

        @Test
        @DisplayName("Throws a ResourceNotFoundException when the given day does not exist or belongs to a different user.")
        void dayNotFound() {
            // Arrange
            FoodEntryCreateRequest request = defaultFoodEntryCreateRequestBuilder().build();    // Request type is irrelevant here.

            when(dayRepository.fetchByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.createEntry(myUser, DAY_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(entryRepository, never()).save(any(Entry.class));
        }

        @Nested
        @DisplayName("with a FoodEntryCreateRequest")
        class CreateFoodEntry {

            FoodEntryCreateRequest request;

            @BeforeEach
            void prepareTests() {
                request = defaultFoodEntryCreateRequestBuilder()
                        .foodId(FOOD_ID)
                        .category(TEST_CATEGORY)
                        .grams(TEST_GRAMS)
                        .vendor(TEST_VENDOR)
                        .build();

                when(dayRepository.fetchByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.of(myDay));
            }

            @Test
            @DisplayName("Creates a FoodEntry when the given day and food exist and belong to the given user.")
            void foodEntryCreated() {
                // Arrange
                Food food = prepareFoodWithTestValues();
                FoodEntry saved = prepareMyFoodEntry(); 

                when(foodRepository.fetchByIdVerified(USER_ID, FOOD_ID)).thenReturn(Optional.of(food));
                when(entryRepository.countByDayAndCategory(DAY_ID, TEST_CATEGORY)).thenReturn(TEST_COUNT);
                when(entryRepository.save(any(FoodEntry.class))).thenReturn(saved);

                // Act
                EntryResponse result = entryService.createEntry(myUser, DAY_ID, request);

                // Assert
                assertThat(result).isInstanceOf(FoodEntryResponse.class);
                assertThat(result).isEqualTo(entryMapper.toResponse(saved));

                verify(entryRepository).save(entryCaptor.capture());
                FoodEntry created = (FoodEntry) entryCaptor.getValue();

                assertThat(created)
                        .usingRecursiveComparison()
                        .ignoringFields("id")
                        .isEqualTo(prepareFoodEntryWithExpectedValues(food));
            }

            @Test
            @DisplayName("Throws a ResourceNotFoundException when the given food does not exist or belongs to a different user.")
            void foodNotFound() {
                // Arrange
                when(foodRepository.fetchByIdVerified(USER_ID, FOOD_ID)).thenReturn(Optional.empty());

                // Act + Assert
                assertThatThrownBy(() -> entryService.createEntry(myUser, DAY_ID, request))
                        .isInstanceOf(ResourceNotFoundException.class);
                verify(entryRepository, never()).save(any(Entry.class));

            }

        }

        @Nested
        @DisplayName("with an ExerciseEntryCreateRequest")
        class CreateExerciseEntry {

            ExerciseEntryCreateRequest request;

            @BeforeEach
            void prepareTests() {
                request = defaultExerciseEntryCreateRequestBuilder()
                    .exerciseId(EXERCISE_ID)
                    .category(TEST_CATEGORY)
                    .duration(TEST_DURATION)
                    .level(TEST_LEVEL)
                    .build();

                when(dayRepository.fetchByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.of(myDay));
            }

            @Test
            @DisplayName("Creates an ExerciseEntry when the given day and exercise exist and belong to the given user.")
            void exerciseEntryCreated() {
                // Arrange
                Exercise exercise = prepareExerciseWithTestValues();
                ExerciseEntry saved = prepareMyExerciseEntry();

                when(exerciseRepository.fetchByIdVerified(USER_ID, EXERCISE_ID)).thenReturn(Optional.of(exercise));
                when(entryRepository.countByDayAndCategory(DAY_ID, TEST_CATEGORY)).thenReturn(TEST_COUNT);
                when(entryRepository.save(any(ExerciseEntry.class))).thenReturn(saved);

                // Act
                EntryResponse result = entryService.createEntry(myUser, DAY_ID, request);

                // Assert
                assertThat(result).isInstanceOf(ExerciseEntryResponse.class);
                assertThat(result).isEqualTo(entryMapper.toResponse(saved));

                verify(entryRepository).save(entryCaptor.capture());
                ExerciseEntry created = (ExerciseEntry) entryCaptor.getValue();

                assertThat(created)
                    .usingRecursiveComparison()
                    .ignoringFields("id")
                    .isEqualTo(prepareExerciseEntryWithExpectedValues(exercise));
            }

            @Test
            @DisplayName("Throws a ResourceNotFoundException when the given exercise does not exist or belongs to a different user.")
            void exerciseNotFound() {
                // Arrange
                when(exerciseRepository.fetchByIdVerified(USER_ID, EXERCISE_ID)).thenReturn(Optional.empty());

                // Act + Assert
                assertThatThrownBy(() -> entryService.createEntry(myUser, DAY_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class);
                verify(entryRepository, never()).save(any(Entry.class));
            }

        }

    }

    @Nested
    @DisplayName("duplicateEntry")
    class DuplicateEntry {

        EntryDuplicateRequest request;

        Day prepareMyOtherDay() {
            Day myOtherDay = defaultDayBuilder().id(OTHER_DAY_ID).plan(myPlan).position(2).build();
            myPlan.getDays().add(myOtherDay);
            return myOtherDay;
        }

        @BeforeEach
        void prepareTests() {
            request = defaultEntryDuplicateRequestBuilder()
                    .entryId(ENTRY_ID)
                    .category(OTHER_CATEGORY)
                    .build();
        }

        @Test
        @DisplayName("Throws a ResourceNotFoundException when the requested entry does not exist or belongs to a different user.")
        void entryNotFound() {
            // Arrange
            FoodEntry entry = prepareMyFoodEntry();     // Subtype is irrelevant here.

            when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(entry));
            when(dayRepository.fetchByIdVerified(USER_ID, OTHER_DAY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.duplicateEntry(myUser, OTHER_DAY_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(entryRepository, never()).save(any(Entry.class));
        }

        @Test
        @DisplayName("Throws a ResourceNotFoundException when the given day does not exist or belongs to a different user.")
        void dayNotFound() {
            // Arrange
            when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.duplicateEntry(myUser, OTHER_DAY_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(entryRepository, never()).save(any(Entry.class));
        }

        @Nested
        @DisplayName("with entryId that points to a FoodEntry")
        class DuplicateFoodEntry {

            @Test
            @DisplayName("Creates and saves a duplicate of a FoodEntry when given a valid dayId, entryId, and target category.")
            void foodEntryDuplicated() {
                // Arrange
                Food food = prepareFoodWithTestValues();
                FoodEntry original = prepareFoodEntryWithExpectedValues(food);
                Day myOtherDay = prepareMyOtherDay();
                FoodEntry saved = prepareMyFoodEntry();

                when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(original));
                when(dayRepository.fetchByIdVerified(USER_ID, OTHER_DAY_ID)).thenReturn(Optional.of(myOtherDay));
                when(entryRepository.countByDayAndCategory(OTHER_DAY_ID, OTHER_CATEGORY)).thenReturn(OTHER_COUNT);
                when(entryRepository.save(any(FoodEntry.class))).thenReturn(saved);

                // Act
                EntryResponse result = entryService.duplicateEntry(myUser, OTHER_DAY_ID, request);

                // Assert
                assertThat(result).isEqualTo(entryMapper.toResponse(saved));

                verify(entryRepository).save(entryCaptor.capture());
                FoodEntry copied = (FoodEntry) entryCaptor.getValue();

                assertThat(copied)
                        .usingRecursiveComparison()
                        .ignoringFields("id", "day", "category", "position")
                        .isEqualTo(original);

                assertThat(copied.getDay()).isEqualTo(myOtherDay);
                assertThat(copied.getCategory()).isEqualTo(OTHER_CATEGORY);
                assertThat(copied.getPosition()).isEqualTo(OTHER_COUNT + 1);
            }

            @Test
            @DisplayName("Duplicate FoodEntry calculates its own snapshots values from scratch.")
            void outdatedFoodEntryDuplicated() {
                // Arrange
                Food food = prepareFoodWithTestValues();
                FoodEntry outdatedOriginal = prepareFoodEntryWithExpectedValues(food);
                outdatedOriginal.setCalories(1.0);

                Day myOtherDay = prepareMyOtherDay();
                FoodEntry saved = prepareMyFoodEntry();

                when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(outdatedOriginal));
                when(dayRepository.fetchByIdVerified(USER_ID, OTHER_DAY_ID)).thenReturn(Optional.of(myOtherDay));
                when(entryRepository.countByDayAndCategory(OTHER_DAY_ID, OTHER_CATEGORY)).thenReturn(OTHER_COUNT);
                when(entryRepository.save(any(FoodEntry.class))).thenReturn(saved);

                // Act
                entryService.duplicateEntry(myUser, OTHER_DAY_ID, request);

                // Assert

                verify(entryRepository).save(entryCaptor.capture());
                FoodEntry copied = (FoodEntry) entryCaptor.getValue();

                assertThat(copied.getCalories()).isNotCloseTo(outdatedOriginal.getCalories(), within(0.01));
                assertThat(copied.getCalories()).isCloseTo(TEST_CALORIES_PER_100G * TEST_GRAMS / 100.0, within(0.01));
            }

        }

        @Nested
        @DisplayName("with entryId that points to an ExerciseEntry")
        class DuplicateExerciseEntry {

            @Test
            @DisplayName("Creates and saves a duplicate of an ExerciseEntry when given a valid dayId, entryId, and target category.")
            void exerciseEntryDuplicated() {
                // Arrange
                Exercise exercise = prepareExerciseWithTestValues();
                ExerciseEntry original = prepareExerciseEntryWithExpectedValues(exercise);
                Day myOtherDay = prepareMyOtherDay();
                ExerciseEntry saved = prepareMyExerciseEntry();

                when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(original));
                when(dayRepository.fetchByIdVerified(USER_ID, OTHER_DAY_ID)).thenReturn(Optional.of(myOtherDay));
                when(entryRepository.countByDayAndCategory(OTHER_DAY_ID, OTHER_CATEGORY)).thenReturn(OTHER_COUNT);
                when(entryRepository.save(any(ExerciseEntry.class))).thenReturn(saved);

                // Act
                EntryResponse result = entryService.duplicateEntry(myUser, OTHER_DAY_ID, request);

                // Assert
                assertThat(result).isEqualTo(entryMapper.toResponse(saved));

                verify(entryRepository).save(entryCaptor.capture());
                ExerciseEntry copied = (ExerciseEntry) entryCaptor.getValue();

                assertThat(copied)
                    .usingRecursiveComparison()
                    .ignoringFields("id", "day", "category", "position")
                    .isEqualTo(original);

                assertThat(copied.getDay()).isEqualTo(myOtherDay);
                assertThat(copied.getCategory()).isEqualTo(OTHER_CATEGORY);
                assertThat(copied.getPosition()).isEqualTo(OTHER_COUNT + 1);                
            }

            @Test
            @DisplayName("Duplicate ExerciseEntry calculates its own snapshots values from scratch.")
            void outdatedExerciseEntryDuplicated() {
                // Arrange
                Exercise exercise = prepareExerciseWithTestValues();
                ExerciseEntry outdatedOriginal = prepareExerciseEntryWithExpectedValues(exercise);
                outdatedOriginal.setCalories(1.0);

                Day myOtherDay = prepareMyOtherDay();
                ExerciseEntry saved = prepareMyExerciseEntry();

                when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(outdatedOriginal));
                when(dayRepository.fetchByIdVerified(USER_ID, OTHER_DAY_ID)).thenReturn(Optional.of(myOtherDay));
                when(entryRepository.countByDayAndCategory(OTHER_DAY_ID, OTHER_CATEGORY)).thenReturn(OTHER_COUNT);
                when(entryRepository.save(any(ExerciseEntry.class))).thenReturn(saved);               

                // Act
                entryService.duplicateEntry(myUser, OTHER_DAY_ID, request);

                // Assert

                verify(entryRepository).save(entryCaptor.capture());
                ExerciseEntry copied = (ExerciseEntry) entryCaptor.getValue();

                assertThat(copied.getCalories()).isNotCloseTo(outdatedOriginal.getCalories(), within(0.01));
                assertThat(copied.getCalories()).isCloseTo(-1 * TEST_CALORIES_PER_MINUTE * TEST_DURATION, within(0.01));
            }
        }

    }

    @Nested
    @DisplayName("editEntry")
    class EditEntry {

        FoodEntry prepareFoodEntryToUpdate(Food food) {
            return defaultFoodEntryBuilder()
                    .id(ENTRY_ID)
                    .day(myDay)
                    .category(TEST_CATEGORY)
                    .position(TEST_COUNT + 1)
                    .food(food)
                    .grams(OTHER_GRAMS)
                    .unit(OTHER_UNIT)
                    .vendor(OTHER_VENDOR)
                    .build();
        }

        ExerciseEntry prepareExerciseEntryToUpdate(Exercise exercise) {
            return defaultExerciseEntryBuilder()
                .id(ENTRY_ID)
                .day(myDay)
                .category(TEST_CATEGORY)
                .position(TEST_COUNT + 1)
                .exercise(exercise)
                .duration(OTHER_DURATION)
                .level(OTHER_LEVEL)
                .build();
        }

        @Test
        @DisplayName("Edits the requested FoodEntry when it exists and belongs to the given user.")
        void foodEntryEdited() {
            // Arrange
            FoodEntryEditRequest request = defaultFoodEntryEditRequestBuilder()
                    .grams(TEST_GRAMS)
                    .unit(TEST_UNIT)
                    .vendor(TEST_VENDOR)
                    .build();

            Food food = prepareFoodWithTestValues();
            FoodEntry entry = prepareFoodEntryToUpdate(food);

            when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(entry));

            // Act
            EntryResponse result = entryService.editEntry(myUser, ENTRY_ID, request);

            // Assert
            assertThat(result).isEqualTo(entryMapper.toResponse(entry));

            assertThat(entry)
                    .usingRecursiveComparison()
                    .isEqualTo(prepareFoodEntryWithExpectedValues(food));
        }

        @Test
        @DisplayName("Edits the requested ExerciseEntry when it exists and belongs to the given user.")
        void exerciseEntryEdited() {
            // Arrange
            ExerciseEntryEditRequest request = defaultExerciseEntryEditRequestBuilder()
                .duration(TEST_DURATION)
                .level(TEST_LEVEL)
                .build();
            
            Exercise exercise = prepareExerciseWithTestValues();
            ExerciseEntry entry = prepareExerciseEntryToUpdate(exercise);

            when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(entry));

            // Act
            EntryResponse result = entryService.editEntry(myUser, ENTRY_ID, request);

            // Assert
            assertThat(result).isEqualTo(entryMapper.toResponse(entry));

            assertThat(entry)
                .usingRecursiveComparison()
                .isEqualTo(prepareExerciseEntryWithExpectedValues(exercise));
        }

        @Test
        @DisplayName("Throws a ResourceNotFoundException when the requested entry does not exist or belongs to a different user.")
        void entryNotFound() {
            // Arrange
            FoodEntryEditRequest request = defaultFoodEntryEditRequestBuilder().build();    // Subtype is irrelevant here.

            when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.editEntry(myUser, ENTRY_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

    }

    @Nested
    @DisplayName("moveEntry")
    class MoveEntry {

        FoodEntry prepareFoodEntryForMove() {
            FoodEntry entry = prepareMyFoodEntry();
            entry.setCategory(TEST_CATEGORY);
            entry.setPosition(TEST_POSITION);
            return entry;
        }

        EntryMoveRequest prepareSpecificRequest(Category category, int desiredPosition) {
            return EntryMoveRequest.builder()
                .category(category)
                .desiredPosition(desiredPosition)
                .build();
        }

        static Stream<Arguments> provideInvalidInputs() {
            return Stream.of(
                argumentSet("Same category", TEST_CATEGORY, TEST_COUNT + 1),
                argumentSet("Different category", OTHER_CATEGORY, TEST_COUNT + 2)
            );
        }

        @Test
        @DisplayName("Moves entry up in the same category when the requested position is higher than the current position.")
        void entryMovedUpInSameCategory() {
            // Arrange
            EntryMoveRequest request = prepareSpecificRequest(TEST_CATEGORY, HIGHER_POSITION);
            FoodEntry entry = prepareFoodEntryForMove();

            when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(entry));
            when(entryRepository.countByDayAndCategory(DAY_ID, TEST_CATEGORY)).thenReturn(TEST_COUNT);

            // Act
            assertThatCode(() -> entryService.moveEntry(myUser, ENTRY_ID, request))
                .doesNotThrowAnyException();

            // Assert
            verify(entryRepository).shiftDownByDayAndCategory(DAY_ID, TEST_CATEGORY, TEST_POSITION, HIGHER_POSITION);
            verifyNoMoreInteractions(entryRepository);

            assertThat(entry.getCategory()).isEqualTo(TEST_CATEGORY);
            assertThat(entry.getPosition()).isEqualTo(HIGHER_POSITION);
        }

        @Test
        @DisplayName("Moves entry down in the same category when the requested position is lower than the current position.")
        void entryMovedDownInSameCategory() {
            // Arrange
            EntryMoveRequest request = prepareSpecificRequest(TEST_CATEGORY, LOWER_POSITION);
            FoodEntry entry = prepareFoodEntryForMove();

            when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(entry));
            when(entryRepository.countByDayAndCategory(DAY_ID, TEST_CATEGORY)).thenReturn(TEST_COUNT);

            // Act
            assertThatCode(() -> entryService.moveEntry(myUser, ENTRY_ID, request))
                .doesNotThrowAnyException();

            // Assert
            verify(entryRepository).shiftUpByDayAndCategory(DAY_ID, TEST_CATEGORY, LOWER_POSITION, TEST_POSITION);
            verifyNoMoreInteractions(entryRepository);

            assertThat(entry.getCategory()).isEqualTo(TEST_CATEGORY);
            assertThat(entry.getPosition()).isEqualTo(LOWER_POSITION);
        }

        @Test
        @DisplayName("Moves the requested entry to another category.")
        void entryMovedToAnotherCategory() {
            // Arrange
            EntryMoveRequest request = prepareSpecificRequest(OTHER_CATEGORY, HIGHER_POSITION);
            FoodEntry entry = prepareFoodEntryForMove();

            when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(entry));
            when(entryRepository.countByDayAndCategory(DAY_ID, OTHER_CATEGORY)).thenReturn(TEST_COUNT);

            // Act
            assertThatCode(() -> entryService.moveEntry(myUser, ENTRY_ID, request))
                .doesNotThrowAnyException();
            
            // Assert
            verify(entryRepository).shiftUpByDayAndCategory(DAY_ID, OTHER_CATEGORY, HIGHER_POSITION, null);
            verify(entryRepository).shiftDownByDayAndCategory(DAY_ID, TEST_CATEGORY, TEST_POSITION, null);

            assertThat(entry.getCategory()).isEqualTo(OTHER_CATEGORY);
            assertThat(entry.getPosition()).isEqualTo(HIGHER_POSITION);
        }

        @Test
        @DisplayName("Throws a ResourceNotFoundException when the requested entry does not exist or belongs to a different user.")
        void entryNotFound() {
            // Arrange
            EntryMoveRequest request = defaultEntryMoveRequestBuilder().build();
            when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.moveEntry(myUser, ENTRY_ID, request))
                .isInstanceOf(ResourceNotFoundException.class);
            verifyNoMoreInteractions(entryRepository);
        }

        @Test
        @DisplayName("Throws a ServiceValidationException when the request gives the entry's current category and position.")
        void noMoveNecessary() {
            // Arrange
            EntryMoveRequest request = prepareSpecificRequest(TEST_CATEGORY, TEST_POSITION);
            FoodEntry entry = prepareFoodEntryForMove();

            when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(entry));

            // Act
            assertThatThrownBy(() -> entryService.moveEntry(myUser, ENTRY_ID, request))
                .isInstanceOf(ServiceValidationException.class);
            verifyNoMoreInteractions(entryRepository);
        }

        @ParameterizedTest
        @DisplayName("Throws a ServiceValidationException when the desired position is out of the desired ")
        @MethodSource("provideInvalidInputs")
        void desiredPositionInvalid(Category targetCategory, int targetPosition) {
            // Arrange
            EntryMoveRequest request = prepareSpecificRequest(targetCategory, targetPosition);
            FoodEntry entry = prepareFoodEntryForMove();

            when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(entry));
            when(entryRepository.countByDayAndCategory(DAY_ID, targetCategory)).thenReturn(TEST_COUNT);

            // Act + Assert
            assertThatThrownBy(() -> entryService.moveEntry(myUser, ENTRY_ID, request))
                .isInstanceOf(ServiceValidationException.class);

            verifyNoMoreInteractions(entryRepository);
            assertThat(entry.getCategory()).isEqualTo(TEST_CATEGORY);
            assertThat(entry.getPosition()).isEqualTo(TEST_POSITION);
        }

    }

    @Nested
    @DisplayName("deleteEntry")
    class DeleteEntry {

        @Test
        @DisplayName("Deletes the requested entry and closes the gap when the entry exists and belongs to the given user.")
        void entryDeleted() {
            // Arrange
            Placement placement = mock(Placement.class);

            when(entryRepository.extractPlacementByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(placement));
            when(placement.getDayId()).thenReturn(DAY_ID);
            when(placement.getCategory()).thenReturn(TEST_CATEGORY);
            when(placement.getPosition()).thenReturn(TEST_POSITION);

            // Act
            assertThatCode(() -> entryService.deleteEntry(myUser, ENTRY_ID))
                    .doesNotThrowAnyException();

            // Assert
            verify(entryRepository).deleteByIdVerified(USER_ID, ENTRY_ID);
            verify(entryRepository).shiftDownByDayAndCategory(DAY_ID, TEST_CATEGORY, TEST_POSITION, null);
        }

        @Test
        @DisplayName("Throws a ResourceNotFoundException when the requested entry does not exist or belongs to a different user.")
        void entryNotFound() {
            // Arrange
            when(entryRepository.extractPlacementByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.deleteEntry(myUser, ENTRY_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(entryRepository, never()).deleteByIdVerified(anyLong(), anyLong());
        }

    }

    @Nested
    @DisplayName("retrieveEntry")
    class RetrieveEntry {

        @Test
        @DisplayName("Returns a FoodEntryResponse when given an entryId that points to a FoodEntry owned by the given user.")
        void foodEntryRetrieved() {
            // Arrange
            FoodEntry entry = prepareMyFoodEntry();

            when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(entry));

            // Act
            EntryResponse result = entryService.retrieveEntry(myUser, ENTRY_ID);

            // Assert
            assertThat(result).isInstanceOf(FoodEntryResponse.class);
            assertThat(result).isEqualTo(entryMapper.toResponse(entry));
        }

        @Test
        @DisplayName("Returns an ExerciseEntryResponse when given an entryId that points to an ExerciseEntry owned by the given user.")
        void exerciseEntryRetrieved() {
            // Arrange
            ExerciseEntry entry = prepareMyExerciseEntry();

            when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(entry));

            // Act
            EntryResponse result = entryService.retrieveEntry(myUser, ENTRY_ID);

            // Assert
            assertThat(result).isInstanceOf(ExerciseEntryResponse.class);
            assertThat(result).isEqualTo(entryMapper.toResponse(entry));
        }

        @Test
        @DisplayName("Throws a ResourceNotFoundException when the requested entry does not exist or belongs to a different user.")
        void entryNotFound() {
            // Arrange
            when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.retrieveEntry(myUser, ENTRY_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

    }

}