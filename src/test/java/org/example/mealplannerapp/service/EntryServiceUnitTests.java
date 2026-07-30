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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.example.mealplannerapp.fixture.DayTestFixtures.defaultDayBuilder;
import static org.example.mealplannerapp.fixture.EntryTestFixtures.*;
import static org.example.mealplannerapp.fixture.FoodTestFixtures.defaultFoodBuilder;
import static org.example.mealplannerapp.fixture.PlanTestFixtures.defaultPlanBuilder;
import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUserBuilder;
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
    private final Category TEST_CATEGORY = Category.LUNCH;
    private final Category OTHER_CATEGORY = Category.DINNER;
    private final int TEST_POSITION = 5;
    private final int TEST_COUNT = 10;
    private final int OTHER_COUNT = 12;
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
                    .category(TEST_CATEGORY)
                    .position(TEST_COUNT + 1)
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

        void entryMovedUpInSameCategory() {

        }

        void entryMovedDownInSameCategory() {

        }

        void entryMovedToAnotherCategory() {

        }

        void noMoveNecessary() {

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
            verifyNoMoreInteractions(entryRepository);
        }

        @Test
        @DisplayName("Throws a ServiceValidationException when the requested entry does not belong to the given day.")
        void entryNotInGivenDay() {
            // Arrange
            EntryMoveRequest request = defaultEntryMoveRequestBuilder().build();
            FoodEntry entry = prepareMyFoodEntry();

            when(entryRepository.fetchByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(entry));

            // Act + Assert
            assertThatThrownBy(() -> entryService.moveEntry(myUser, OTHER_DAY_ID, ENTRY_ID, request))
                    .isInstanceOf(ServiceValidationException.class);
            verifyNoMoreInteractions(entryRepository);
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