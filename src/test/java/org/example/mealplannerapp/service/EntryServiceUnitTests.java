package org.example.mealplannerapp.service;

import org.example.mealplannerapp.constants.Category;
import org.example.mealplannerapp.dto.entry.request.EntryDuplicateRequest;
import org.example.mealplannerapp.dto.entry.request.EntryMoveRequest;
import org.example.mealplannerapp.dto.entry.request.create.FoodEntryCreateRequest;
import org.example.mealplannerapp.dto.entry.request.edit.FoodEntryEditRequest;
import org.example.mealplannerapp.dto.entry.response.EntryResponse;
import org.example.mealplannerapp.dto.entry.response.FoodEntryResponse;
import org.example.mealplannerapp.embeddable.FoodPrice;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.Plan;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.entity.entry.FoodEntry;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.exception.ServiceValidationException;
import org.example.mealplannerapp.mapper.EntryMapper;
import org.example.mealplannerapp.mapper.EntryMapperImpl;
import org.example.mealplannerapp.projection.Placement;
import org.example.mealplannerapp.repository.DayRepository;
import org.example.mealplannerapp.repository.EntryRepository;
import org.example.mealplannerapp.repository.FoodRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
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
import static org.example.mealplannerapp.fixture.EntryTestFixtures.defaultEntryMoveRequestBuilder;
import static org.example.mealplannerapp.fixture.EntryTestFixtures.defaultEntryDuplicateRequestBuilder;
import static org.example.mealplannerapp.fixture.EntryTestFixtures.defaultFoodEntryBuilder;
import static org.example.mealplannerapp.fixture.EntryTestFixtures.defaultFoodEntryCreateRequestBuilder;
import static org.example.mealplannerapp.fixture.EntryTestFixtures.defaultFoodEntryEditRequestBuilder;
import static org.example.mealplannerapp.fixture.FoodTestFixtures.defaultFoodBuilder;
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
    @Captor
    ArgumentCaptor<Entry> entryCaptor;

    // VARIABLES
    private EntryMapper entryMapper;
    private EntryService entryService;
    private User myUser;
    private Plan myPlan;
    private Day myDay;
    // TODO: Make these static?
    // CONSTANTS - TEST ENTITY IDS
    private final long USER_ID = 1L;
    private final long FOOD_ID = 99L;
    private final long ENTRY_ID = 88L;
    private final long DAY_ID = 77L;
    private final long OTHER_DAY_ID = 78L;
    private final long PLAN_ID = 66L;

    // CONSTANTS - TEST FOOD VALUES
    private final double TEST_CALORIES_PER_100G = 123.0;
    private final double TEST_PROTEIN_PER_100G = 24.0;
    private final double TEST_CARBS_PER_100G = 56.0;
    private final double TEST_FAT_PER_100G = 8.5;
    private final double TEST_FIBER_PER_100G = 4.5;
    private final double TEST_EDIBLE_RATIO = 0.9;
    private final String TEST_VENDOR = "Masoutis";
    private final double TEST_PURCHASE_PRICE = 6.00;
    private final double TEST_PURCHASE_GRAMS = 500;

    // CONSTANTS - TEST ENTRY VALUES
    private static final Category TEST_CATEGORY = Category.LUNCH;
    private static final Category OTHER_CATEGORY = Category.DINNER;
    private final int TEST_POSITION = 5;
    private final int HIGHER_POSITION = 7;
    private final int LOWER_POSITION = 3;
    private static final int TEST_COUNT = 10;
    private static final int OTHER_COUNT = 12;
    private final double TEST_GRAMS = 110.0;
    private final double OTHER_GRAMS = 85.0;
    private final String OTHER_VENDOR = "Sklavenitis";
    private final String TEST_UNIT = "tbsp";
    private final String OTHER_UNIT = "cup";

    // HELPER METHODS
    /**
     * Method for creating a simple FoodEntry and its referenced Food, using the default food and entry IDs,
     * and owned by myDay and myUser respectively.
     */
    private FoodEntry prepareMyFoodEntry() {
        Food food = defaultFoodBuilder().id(FOOD_ID).user(myUser).build();
        return defaultFoodEntryBuilder().id(ENTRY_ID).day(myDay).food(food).build();
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
                .displayUnit(TEST_UNIT)
                .selectedVendor(TEST_VENDOR)
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
        entryService = new EntryService(entryRepository, dayRepository, foodRepository, entryMapper);
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
                        .selectedVendor(TEST_VENDOR)
                        .build();

                when(dayRepository.fetchByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.of(myDay));
            }

            @Test
            @DisplayName("Creates a FoodEntry when the given day and food exist and belong to the given user.")
            void foodEntryCreated() {
                // Arrange
                Food food = prepareFoodWithTestValues();
                FoodEntry saved = prepareFoodEntryWithExpectedValues(food);

                when(foodRepository.fetchByIdVerified(USER_ID, FOOD_ID)).thenReturn(Optional.of(food));
                when(entryRepository.countByDayAndCategory(DAY_ID, TEST_CATEGORY)).thenReturn(TEST_COUNT);
                when(entryRepository.save(any(FoodEntry.class))).thenReturn(saved);

                // Act
                EntryResponse result = entryService.createEntry(myUser, DAY_ID, request);

                // Assert
                assertThat(result).isInstanceOf(FoodEntryResponse.class);
                assertThat(result).isEqualTo(entryMapper.generateResponse(saved));

                verify(entryRepository).save(entryCaptor.capture());
                FoodEntry created = (FoodEntry) entryCaptor.getValue();

                assertThat(created)
                        .usingRecursiveComparison()
                        .ignoringFields("id")
                        .isEqualTo(saved);
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
                    assertThat(result).isEqualTo(entryMapper.generateResponse(saved));
                    
                    verify(entryRepository).save(entryCaptor.capture());
                    FoodEntry copied = (FoodEntry) entryCaptor.getValue();

                    assertThat(copied)
                        .usingRecursiveComparison()
                        .ignoringFields("id", "day", "category", "position")
                        .isEqualTo(original);   // TODO: Learn how (if?) this works for floats.

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
                    EntryResponse result = entryService.duplicateEntry(myUser, OTHER_DAY_ID, request);

                    // Assert
                    verify(entryRepository).save(entryCaptor.capture());
                    FoodEntry copied = (FoodEntry) entryCaptor.getValue();

                    assertThat(copied.getCalories()).isNotCloseTo(outdatedOriginal.getCalories(), within(0.01));
                    assertThat(copied.getCalories()).isCloseTo(TEST_CALORIES_PER_100G * TEST_GRAMS / 100.0, within(0.01));

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
                .food(food)
                .grams(OTHER_GRAMS)
                .displayUnit(OTHER_UNIT)
                .selectedVendor(OTHER_VENDOR)
                .build();
        }

        @Test
        @DisplayName("Edits the requested FoodEntry when it exists and belongs to the given user.")
        void foodEntryEdited() {
            // Arrange
            FoodEntryEditRequest request = defaultFoodEntryEditRequestBuilder()
                .grams(TEST_GRAMS)
                .displayUnit(TEST_UNIT)
                .selectedVendor(TEST_VENDOR)
                .build();

            Food food = prepareFoodWithTestValues();
            FoodEntry entry = prepareFoodEntryToUpdate(food);

            when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(entry));

            // Act
            EntryResponse result = entryService.editEntry(myUser, ENTRY_ID, request);

            // Assert
            assertThat(result).isEqualTo(entryMapper.generateResponse(entry));

            FoodEntry target = prepareFoodEntryWithExpectedValues(food);
            assertThat(entry)
                .usingRecursiveComparison()
                .isEqualTo(target);
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
            assertThat(result).isEqualTo(entryMapper.generateResponse(entry));
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



    /*

    }
    @Nested
    @DisplayName("duplicateEntry")
    class DuplicateEntry {

        EntryDuplicateRequest request;

        @BeforeEach
        void prepareTests() {
            request = defaultEntryDuplicateRequestBuilder()
                .entryId(ENTRY_ID)
                .category(TEST_CATEGORY)
                .build();
        }

        @Test
        @DisplayName("Creates and saves a duplicate of the requested entry when entryId and dayId are valid for the given user.")
        void entryDuplicated() {
            // Arrange
            Food food = prepareFoodWithTestValues();
            FoodEntry original = prepareFoodEntryWithExpectedValues(food);

            when(dayRepository.fetchByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.of(myDay));
            when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(original));
            when(entryRepository.countByDayAndCategory(DAY_ID, TEST_CATEGORY)).thenReturn(TEST_COUNT);

            // Act
            EntryResponse result = entryService.duplicateEntry(myUser, DAY_ID, request);
        }

        @Test
        @DisplayName("Throws a ResourceNotFoundException when the requested entry does not exist or belongs to a different user.")
        void entryNotFound() {
            // Arrange
            when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.duplicateEntry(myUser, DAY_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(entryRepository, never()).save(any(Entry.class));
        }

        @Test
        @DisplayName("Throws a ResourceNotFoundException when the requested day does not exist or belongs to a different user.")
        void dayNotFound() {
            // Arrange
            FoodEntry entry = prepareMyEntry();

            when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(entry));
            when(dayRepository.fetchByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.duplicateEntry(myUser, DAY_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(entryRepository, never()).save(any(Entry.class));
        }

    }

    @Nested
    @DisplayName("editEntry")
    class EditEntry {

        @Test
        @DisplayName("Updates the request entry when it exists and belongs to the given user.")
        void entryEdited() {
            // TODO: Write test.
        }

        @Test
        @DisplayName("Throws a ResourceNotFoundException when the requested entry does not exist or belongs to a different user.")
        void entryNotFound() {
            // Arrange
            FoodEntryEditRequest request = new FoodEntryEditRequest(100.0, "cup", "Sklavenitis");
            when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.editEntry(myUser, ENTRY_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

    }

    @Nested
    @DisplayName("moveEntry")
    class MoveEntry {

        void entryMovedUpInSameCategory() {

        }

        void entryMovedDownInSameCategory() {

        }

        void entryMovedToDifferentCategory() {

        }

        @Test
        @DisplayName("Throws a ResourceNotFoundException when the requested entry does not exist or belongs to a different user.")
        void entryNotFound() {
            // Arrange
            EntryMoveRequest request = defaultEntryMoveRequestBuilder().build();
            when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.moveEntry(myUser, DAY_ID, ENTRY_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class);
            // TODO: Verify no more interactions?
        }

        @Test
        @DisplayName("Throws a ServiceValidationException when the given entry does not belong to the given day.")
        void entryNotInGivenDay() {
            // Arrange
            EntryMoveRequest request = defaultEntryMoveRequestBuilder().build();
            FoodEntry entry = prepareMyEntry();
            Day otherDay = defaultDayBuilder().id(DAY_ID + 1).plan(myPlan).build();
            entry.setDay(otherDay);

            when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(entry));

            // Act + Assert
            assertThatThrownBy(() -> entryService.moveEntry(myUser, DAY_ID, ENTRY_ID, request))
                    .isInstanceOf(ServiceValidationException.class);
            // TODO: Verify no more interactions?
        }

    }



    @Nested
    @DisplayName("retrieveEntry")
    class retrieveEntry {

        @Test
        @DisplayName("Returns the requested entry's full data when it exists and belongs to the given user.")
        void entryRetrieved() {
            // Arrange
            FoodEntry entry = prepareMyEntry();

            when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(entry));

            // Act
            EntryResponse result = entryService.retrieveEntry(myUser, ENTRY_ID);

            // Assert
            assertThat(result).isEqualTo(entryMapper.generateResponse(entry));
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

/*
@ExtendWith(MockitoExtension.class)
public class EntryServiceUnitTests {

    @Nested
    class createEntry {

        private FoodEntryCreateRequest request;
        private FoodEntry created;

        private static final long COUNT = 3;
        private static final int POSITION = 4;
        private static final Category CATEGORY_LUNCH = Category.LUNCH;

        @BeforeEach
        void prepareTests() {
            user = mock(User.class);
            request = defaultFoodEntryCreateRequestBuilder()
                    .foodId(FOOD_ID)
                    .category(CATEGORY_LUNCH)
                    .build();
            created = spy(defaultFoodEntryBuilder().category(CATEGORY_LUNCH).build());

            when(user.getId()).thenReturn(USER_ID);
            when(entryMapper.createFromRequest(request)).thenReturn(created);
        }

        @Test
        @DisplayName("Given a valid input, creates and saves the new Entry.")
        void happyFlow() {
            // Arrange
            Day day = new Day();
            Food food = defaultFoodBuilder().build();
            FoodEntry saved = new FoodEntry();
            FoodEntryResponse expected = defaultFoodEntryResponseBuilder().build();

            when(dayRepository.findByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.of(day));
            when(foodRepository.findByIdVerified(USER_ID, FOOD_ID)).thenReturn(Optional.of(food));
            when(entryRepository.countInDayAndCategory(DAY_ID, CATEGORY_LUNCH)).thenReturn(COUNT);
            when(entryRepository.save(created)).thenReturn(saved);
            when(entryMapper.generateResponse(saved)).thenReturn(expected);

            // Act
            EntryResponse response = entryService.createEntry(user, DAY_ID, request);

            // Assert
            assertThat(response).isEqualTo(expected);
            assertThat(created.getDay()).isEqualTo(day);
            assertThat(created.getFood()).isEqualTo(food);
            assertThat(created.getPosition()).isEqualTo(POSITION);
            verify(created).snapshotNutritionAndPriceInfo();
        }

        @Test
        @DisplayName("Given an invalid dayId, throws a ResourceNotFoundException.")
        void dayNotFound() {
            // Arrange
            when(dayRepository.findByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.createEntry(user, DAY_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(dayRepository).findByIdVerified(USER_ID, DAY_ID);
            verify(foodRepository, never()).findByIdVerified(USER_ID, FOOD_ID);
            verify(created, never()).snapshotNutritionAndPriceInfo();
        }

        @Test
        @DisplayName("Given an invalid foodId, throws a ResourceNotFoundException.")
        void foodNotFound() {
            // Arrange
            Day day = new Day();

            when(dayRepository.findByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.of(day));
            when(foodRepository.findByIdVerified(USER_ID, FOOD_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.createEntry(user, DAY_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(dayRepository).findByIdVerified(USER_ID, DAY_ID);
            verify(foodRepository).findByIdVerified(USER_ID, FOOD_ID);
            verify(created, never()).snapshotNutritionAndPriceInfo();
        }

    }

    @Nested
    class duplicateEntry {

        private EntryDuplicateRequest request;
        private FoodEntry found;

        private static final Category COPY_CATEGORY = Category.DINNER;
        private static final long COUNT = 3L;
        private static final int COPY_POSITION = 4;

        @BeforeEach
        void prepareTests() {
            user = mock(User.class);
            request = new EntryDuplicateRequest(ENTRY_ID, COPY_CATEGORY);
            found = spy(defaultFoodEntryBuilder().build());
            Food food = defaultFoodBuilder().build();
            found.setFood(food);
            found.snapshotNutritionAndPriceInfo();

            when(user.getId()).thenReturn(USER_ID);
        }

        @Test
        @DisplayName("Given a valid input, creates and saves a duplicate of the requested Entry.")
        void happyFlow() {
            // Arrange
            Day day = new Day();
            ArgumentCaptor<FoodEntry> entryCaptor = ArgumentCaptor.forClass(FoodEntry.class);
            FoodEntry saved = new FoodEntry();
            FoodEntryResponse expected = defaultFoodEntryResponseBuilder().build();

            when(entryRepository.findByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(found));
            when(dayRepository.findByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.of(day));
            when(entryRepository.countInDayAndCategory(DAY_ID, COPY_CATEGORY)).thenReturn(COUNT);
            when(entryRepository.save(any())).thenReturn(saved);    // We don't have access to the new entry.
            when(entryMapper.generateResponse(saved)).thenReturn(expected);

            // Act
            EntryResponse response = entryService.duplicateEntry(user, DAY_ID, request);

            // Assert
            assertThat(response).isEqualTo(expected);
            verify(entryRepository).save(entryCaptor.capture());
            FoodEntry copy = entryCaptor.getValue();
            assertThat(copy).isNotSameAs(found);
            assertThat(copy.getDay()).isEqualTo(day);
            assertThat(copy.getCategory()).isEqualTo(COPY_CATEGORY);
            assertThat(copy.getPosition()).isEqualTo(COPY_POSITION);
            assertThat(copy.getCalories()).isCloseTo(found.getCalories(), within(0.01));
        }

        @Test
        @DisplayName("Given an invalid entryId, throws a ResourceNotFoundException.")
        void entryNotFound() {
            // Arrange
            when(entryRepository.findByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.duplicateEntry(user, DAY_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(entryRepository).findByIdVerified(USER_ID, ENTRY_ID);
            verify(dayRepository, never()).findByIdVerified(USER_ID, DAY_ID);
        }

        @Test
        @DisplayName("Given an invalid dayId, throws a ResourceNotFoundException.")
        void dayNotFound() {
            // Arrange
            when(entryRepository.findByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(found));
            when(dayRepository.findByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.duplicateEntry(user, DAY_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(entryRepository).findByIdVerified(USER_ID, ENTRY_ID);
            verify(dayRepository).findByIdVerified(USER_ID, DAY_ID);
            verify(found, never()).createDuplicate();
        }

    }

    @Nested
    class editEntry {

        private FoodEntryEditRequest request;
        private FoodEntry found;
        private static final double NEW_GRAMS = 200.0;
        private static final String NEW_UNIT = "cup";
        private static final String NEW_MERCHANT = "MyMarket";

        @BeforeEach
        void prepareTests() {
            user = mock(User.class);
            request = new FoodEntryEditRequest(NEW_GRAMS, NEW_UNIT, NEW_MERCHANT);

            when(user.getId()).thenReturn(USER_ID);
        }

        @Test
        @DisplayName("Given a valid input, finds and edits the requested Entry.")
        void happyFlow() {
            // Arrange
            FoodEntry found = spy(defaultFoodEntryBuilder().build());
            Food food = defaultFoodBuilder().build();
            found.setFood(food);
            FoodEntryResponse expected = defaultFoodEntryResponseBuilder().build();

            when(entryRepository.findByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(found));
            when(entryMapper.generateResponse(found)).thenReturn(expected);

            // Act
            EntryResponse response = entryService.editEntry(user, ENTRY_ID, request);

            // Assert
            assertThat(response).isEqualTo(expected);
            verify(entryMapper).updateFromRequest(found, request);
            verify(found).snapshotNutritionAndPriceInfo();
        }

        @Test
        @DisplayName("Given an invalid user or entryId, throws a ResourceNotFoundException.")
        void entryNotFound() {
            // Arrange
            when(entryRepository.findByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.editEntry(user, ENTRY_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(entryRepository).findByIdVerified(USER_ID, ENTRY_ID);
            verify(entryMapper, never()).updateFromRequest(any(), any());
        }

    }

    @Nested
    class moveEntry {

        private EntryMoveRequest request;
        private FoodEntry found;

        private static final Category SAME_CATEGORY = Category.LUNCH;
        private static final Category DIFFERENT_CATEGORY = Category.DINNER;
        private static final int SOURCE_POSITION = 5;
        private static final long COUNT = 10;

        @BeforeEach
        void prepareTests() {
            user = mock(User.class);
            found = defaultFoodEntryBuilder()
                    .category(SAME_CATEGORY)
                    .position(SOURCE_POSITION)
                    .build();

            when(user.getId()).thenReturn(USER_ID);
        }

        @ParameterizedTest
        @DisplayName("Given a higher position in the same category, moves the requested Entry up in its current category.")
        @ValueSource(ints = {(int) COUNT - 2, (int) COUNT, (int) COUNT + 2})
        void sameCategoryUp(int desiredPosition) {
            // Arrange
            request = new EntryMoveRequest(SAME_CATEGORY, desiredPosition);

            when(entryRepository.findShallowByIdAndDayVerified(USER_ID, DAY_ID, ENTRY_ID)).thenReturn(Optional.of(found));
            when(entryRepository.countInDayAndCategory(DAY_ID, SAME_CATEGORY)).thenReturn(COUNT);

            // Act
            entryService.moveEntry(user, DAY_ID, ENTRY_ID, request);

            // Assert
            verify(entryRepository).shiftDownInDayAndCategory(eq(DAY_ID), eq(SAME_CATEGORY), eq(SOURCE_POSITION), anyInt());
            verify(entryRepository, never()).shiftUpInDayAndCategory(any(), any(), any(), any());

            assertThat(found.getCategory()).isEqualTo(SAME_CATEGORY);
            assertThat(found.getPosition()).isGreaterThan(SOURCE_POSITION);
            assertThat(found.getPosition()).isLessThanOrEqualTo((int) COUNT);
        }

        @Test
        @DisplayName("Given a lower position in the same category, moves the requested Entry down in its current category.")
        void sameCategoryDown() {
            // Arrange
            request = new EntryMoveRequest(SAME_CATEGORY, SOURCE_POSITION - 2);

            when(entryRepository.findShallowByIdAndDayVerified(USER_ID, DAY_ID, ENTRY_ID)).thenReturn(Optional.of(found));
            when(entryRepository.countInDayAndCategory(DAY_ID, SAME_CATEGORY)).thenReturn(COUNT);

            // Act
            entryService.moveEntry(user, DAY_ID, ENTRY_ID, request);

            // Assert
            verify(entryRepository).shiftUpInDayAndCategory(eq(DAY_ID), eq(SAME_CATEGORY), anyInt(), eq(SOURCE_POSITION));
            verify(entryRepository, never()).shiftDownInDayAndCategory(any(), any(), any(), any());

            assertThat(found.getCategory()).isEqualTo(SAME_CATEGORY);
            assertThat(found.getPosition()).isLessThan(SOURCE_POSITION);
        }

        @Test
        @DisplayName("Given the same position in the same category, performs no changes.")
        void unchangedPosition() {
            // Arrange
            request = new EntryMoveRequest(SAME_CATEGORY, SOURCE_POSITION);

            when(entryRepository.findShallowByIdAndDayVerified(USER_ID, DAY_ID, ENTRY_ID)).thenReturn(Optional.of(found));
            when(entryRepository.countInDayAndCategory(DAY_ID, SAME_CATEGORY)).thenReturn(COUNT);

            // Act
            entryService.moveEntry(user, DAY_ID, ENTRY_ID, request);

            // Assert
            verify(entryRepository, never()).shiftDownInDayAndCategory(any(), any(), any(), any());
            verify(entryRepository, never()).shiftUpInDayAndCategory(any(), any(), any(), any());
            assertThat(found.getPosition()).isEqualTo(SOURCE_POSITION);
        }

        @ParameterizedTest
        @DisplayName("Given a different category, moves the requested Entry and closes the gap left behind.")
        @ValueSource(ints = {(int) COUNT - 2, (int) COUNT, (int) COUNT + 2})
        void differentCategory(int desiredPosition) {
            // Arrange
            request = new EntryMoveRequest(DIFFERENT_CATEGORY, desiredPosition);

            when(entryRepository.findShallowByIdAndDayVerified(USER_ID, DAY_ID, ENTRY_ID)).thenReturn(Optional.of(found));
            when(entryRepository.countInDayAndCategory(DAY_ID, DIFFERENT_CATEGORY)).thenReturn(COUNT);

            // Act
            entryService.moveEntry(user, DAY_ID, ENTRY_ID, request);

            // Assert
            verify(entryRepository).shiftUpInDayAndCategory(eq(DAY_ID), eq(DIFFERENT_CATEGORY), eq(null), anyInt());
            verify(entryRepository).shiftDownInDayAndCategory(DAY_ID, SAME_CATEGORY, SOURCE_POSITION, null);

            assertThat(found.getCategory()).isEqualTo(DIFFERENT_CATEGORY);
            assertThat(found.getPosition()).isLessThanOrEqualTo((int) COUNT + 1);
        }

        @Test
        @DisplayName("Given an invalid entryId, throws a ResourceNotFoundException.")
        void entryNotFound() {
            // Assert
            when(entryRepository.findShallowByIdAndDayVerified(USER_ID, DAY_ID, ENTRY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.moveEntry(user, DAY_ID, ENTRY_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(entryRepository, times(1)).findShallowByIdAndDayVerified(USER_ID, DAY_ID, ENTRY_ID);
            verifyNoMoreInteractions(entryRepository);
        }

    }
 */
