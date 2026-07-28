package org.example.mealplannerapp.service;

import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.entity.Plan;

import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUserBuilder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.example.mealplannerapp.constants.Category;
import org.example.mealplannerapp.dto.entry.request.EntryDuplicateRequest;
import org.example.mealplannerapp.dto.entry.request.EntryMoveRequest;
import org.example.mealplannerapp.dto.entry.request.create.FoodEntryCreateRequest;
import org.example.mealplannerapp.dto.entry.request.edit.FoodEntryEditRequest;
import org.example.mealplannerapp.dto.entry.response.EntryResponse;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.entity.entry.FoodEntry;
import org.example.mealplannerapp.entity.entry.FoodEntry.FoodEntryBuilder;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.mealplannerapp.fixture.UserTestFixtures.*;
import static org.example.mealplannerapp.fixture.FoodTestFixtures.*;
import static org.example.mealplannerapp.fixture.EntryTestFixtures.*;
import static org.example.mealplannerapp.fixture.PlanTestFixtures.*;
import static org.example.mealplannerapp.fixture.DayTestFixtures.*;

@ExtendWith(MockitoExtension.class)
public class EntryServiceUnitTests {

    // MOCKS, SPIES, CAPTORS
    @Mock private EntryRepository entryRepository;
    @Mock private DayRepository dayRepository;
    @Mock private FoodRepository foodRepository;
    @Captor ArgumentCaptor<Entry> entryCaptor;

    // VARIABLES
    private EntryMapper entryMapper;
    private EntryService entryService;
    private User myUser;
    private Plan myPlan;
    private Day myDay;

    // CONSTANTS
    private final long USER_ID = 1L;
    private final long FOOD_ID = 99L;
    private final long ENTRY_ID = 88L;
    private final long DAY_ID = 77L;
    private final long PLAN_ID = 66L;

    // HELPER METHODS
    private FoodEntry prepareMyEntry() {
        Food food = defaultFoodBuilder().id(FOOD_ID).user(myUser).build();
        FoodEntry entry = defaultFoodEntryBuilder().id(ENTRY_ID).day(myDay).food(food).build();
        return entry;
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

        FoodEntryCreateRequest request;

        @BeforeEach
        void prepareTests() {
            request = defaultFoodEntryCreateRequestBuilder().foodId(FOOD_ID).build();
        }

        @Test
        @DisplayName("Creates and saves a FoodEntry when given a valid dayId and valid input data.")
        void foodEntryCreated() {
            // TODO: Write test.
        }

        @Test
        @DisplayName("Throws a ResourceNotFoundException when the given day does not exist or belongs to a different user.")
        void dayNotFound() {
            // Arrange
            when(dayRepository.fetchByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.createEntry(myUser, DAY_ID, request))
                .isInstanceOf(ResourceNotFoundException.class);
            verifyNoInteractions(foodRepository, entryRepository);
        }

        @Test
        @DisplayName("Throws a ResourceNotFoundException when the referenced food does not exist or belongs to a different user.")
        void foodNotFound() {
            // Arrange
            when(dayRepository.fetchByIdVerified(USER_ID, DAY_ID)).thenReturn(Optional.of(myDay));
            when(foodRepository.fetchByIdVerified(USER_ID, FOOD_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.createEntry(myUser, DAY_ID, request))
                .isInstanceOf(ResourceNotFoundException.class);
            verifyNoInteractions(entryRepository);
        }

    }

    @Nested
    @DisplayName("duplicateEntry")
    class DuplicateEntry {

        EntryDuplicateRequest request;

        @BeforeEach
        void prepareTests() {
            request = defaultEntryDuplicateRequestBuilder().entryId(ENTRY_ID).build();
        }

        @Test
        @DisplayName("Creates and saves a duplicate of the requested entry when entryId and dayId are valid for the given user.")
        void entryDuplicated() {
            // TODO: Write test.
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
    @DisplayName("deleteEntry")
    class DeleteEntry {

        final Category TARGET_CATEGORY = Category.DINNER;
        final int TARGET_POSITION = 5;

        @Test
        @DisplayName("Deletes the requested entry and closes the gap in its day/category when it exists and belongs to the given user.")
        void entryDeleted() {
            // Arrange
            Placement placement = mock(Placement.class);

            when(entryRepository.extractPlacementByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(placement));
            when(placement.getDayId()).thenReturn(DAY_ID);
            when(placement.getCategory()).thenReturn(TARGET_CATEGORY);
            when(placement.getPosition()).thenReturn(TARGET_POSITION);

            // Act
            assertThatCode(() -> entryService.deleteEntry(myUser, ENTRY_ID))
                .doesNotThrowAnyException();
            
            // Assert
            verify(entryRepository).deleteByIdVerified(USER_ID, ENTRY_ID);
            verify(entryRepository).shiftDownByDayAndCategory(DAY_ID, TARGET_CATEGORY, TARGET_POSITION, null);
        }

        @Test
        @DisplayName("Throws a ResourceNotFoundException when the requested entry does not exist or belongs to a different user.")
        void entryNotFound() {
            // Arrange
            when(entryRepository.extractPlacementByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.deleteEntry(myUser, ENTRY_ID))
                .isInstanceOf(ResourceNotFoundException.class);
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
