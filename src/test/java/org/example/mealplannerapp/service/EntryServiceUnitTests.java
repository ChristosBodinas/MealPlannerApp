package org.example.mealplannerapp.service;


import org.example.mealplannerapp.constants.Category;
import org.example.mealplannerapp.dto.entry.request.EntryDuplicateRequest;
import org.example.mealplannerapp.dto.entry.request.EntryMoveRequest;
import org.example.mealplannerapp.dto.entry.request.create.FoodEntryCreateRequest;
import org.example.mealplannerapp.dto.entry.request.edit.FoodEntryEditRequest;
import org.example.mealplannerapp.dto.entry.response.EntryResponse;
import org.example.mealplannerapp.dto.entry.response.FoodEntryResponse;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.entity.entry.FoodEntry;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.mapper.EntryMapper;
import org.example.mealplannerapp.projection.PositionData;
import org.example.mealplannerapp.repository.DayRepository;
import org.example.mealplannerapp.repository.EntryRepository;
import org.example.mealplannerapp.repository.FoodRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.example.mealplannerapp.fixtures.EntryTestFixtures.*;
import static org.example.mealplannerapp.fixtures.FoodTestFixtures.defaultFoodBuilder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EntryServiceUnitTests {

    // MOCKS AND INJECTION
    @Mock private EntryRepository entryRepository;
    @Mock private EntryMapper entryMapper;
    @Mock private DayRepository dayRepository;
    @Mock private FoodRepository foodRepository;

    @InjectMocks private EntryService entryService;

    // UNIVERSAL VARIABLES
    private User user;
    private static final Long USER_ID = 1L;
    private static final Long ENTRY_ID = 99L;
    private static final Long FOOD_ID = 88L;
    private static final Long DAY_ID = 77L;

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
        private Food food;

        private static final Category COPY_CATEGORY = Category.DINNER;
        private static final long COUNT = 3L;
        private static final int COPY_POSITION = 4;

        @BeforeEach
        void prepareTests() {
            user = mock(User.class);
            request = new EntryDuplicateRequest(ENTRY_ID, COPY_CATEGORY);
            found = spy(defaultFoodEntryBuilder().build());
            food = defaultFoodBuilder().build();
            found.setFood(food);

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

            when(entryRepository.findByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(found);
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
            assertThat(copy.getId()).isNotSameAs(found);
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
            ArgumentCaptor<Integer> destCaptor = ArgumentCaptor.forClass(Integer.class);
            
            when(entryRepository.findShallowByIdAndDayVerified(USER_ID, DAY_ID, ENTRY_ID)).thenReturn(Optional.of(found));
            when(entryRepository.countInDayAndCategory(DAY_ID, SAME_CATEGORY)).thenReturn(COUNT);

            // Act
            entryService.moveEntry(user, DAY_ID, ENTRY_ID, request);

            // Assert
            verify(entryRepository).shiftDownInDayAndCategory(DAY_ID, SAME_CATEGORY, SOURCE_POSITION, destCaptor.capture());
            verify(entryRepository, never()).shiftUpInDayAndCategory(any(), any(), any(), any());

            int destination = destCaptor.getValue();
            assertThat(destination).isLessThanOrEqualTo((int) COUNT);   // Verifies that the targetPosition was properly clamped.
            assertThat(found.getCategory()).isEqualTo(SAME_CATEGORY);
            assertThat(found.getPosition()).isEqualTo(destination);
        }

        @Test
        @DisplayName("Given a lower position in the same category, moves the requested Entry down in its current category.")
        void sameCategoryDown() {
            // Arrange
            request = new EntryMoveRequest(SAME_CATEGORY, SOURCE_POSITION - 2);
            ArgumentCaptor<Integer> destCaptor = ArgumentCaptor.forClass(Integer.class);

            when(entryRepository.findShallowByIdAndDayVerified(USER_ID, DAY_ID, ENTRY_ID)).thenReturn(Optional.of(found));
            when(entryRepository.countInDayAndCategory(DAY_ID, SAME_CATEGORY)).thenReturn(COUNT);

            // Act
            entryService.moveEntry(user, DAY_ID, ENTRY_ID, request);

            // Assert
            verify(entryRepository).shiftUpInDayAndCategory(DAY_ID, SAME_CATEGORY, destCaptor.capture(), SOURCE_POSITION);
            verify(entryRepository, never()).shiftDownInDayAndCategory(any(), any(), any(), any());

            int destination = destCaptor.getValue();
            assertThat(found.getCategory()).isEqualTo(SAME_CATEGORY);
            assertThat(found.getPosition()).isEqualTo(destination);            
        }

        @Test
        @DisplayName("Given the same position in the same category, performs no changes.")
        void unchangedPosition() {

        }

        @Test
        @DisplayName("Given a different category, moves the requested Entry and closes the gap left behind.")
        void differentCategory() {

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

    @Nested
    class deleteEntry {

        private static final Category ENTRY_CATEGORY = Category.BREAKFAST;
        private static final int ENTRY_POSITION = 3;

        @BeforeEach
        void prepareTests() {
            user = mock(User.class);
            when(user.getId()).thenReturn(USER_ID);
        }

        @Test
        @DisplayName("Given a valid input, finds and deletes the requested Entry, then closes the gap left behind.")
        void happyFlow() {
            // Arrange
            PositionData data = mock(PositionData.class);

            when(entryRepository.findPositionDataByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(data));
            when(data.getDayId()).thenReturn(DAY_ID);
            when(data.getCategory()).thenReturn(ENTRY_CATEGORY);
            when(data.getPosition()).thenReturn(ENTRY_POSITION);

            // Act
            entryService.deleteEntry(user, ENTRY_ID);

            // Assert
            verify(entryRepository).findPositionDataByIdVerified(USER_ID, ENTRY_ID);
            verify(entryRepository).deleteByIdVerified(USER_ID, ENTRY_ID);
            verify(entryRepository).shiftDownInDayAndCategory(DAY_ID, ENTRY_CATEGORY, ENTRY_POSITION, null);
        }

        @Test
        @DisplayName("Given an invalid user or entryId, throws a ResourceNotFoundException.")
        void entryNotFound() {
            // Arrange
            when(entryRepository.findPositionDataByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.deleteEntry(user, ENTRY_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(entryRepository).findPositionDataByIdVerified(USER_ID, ENTRY_ID);
            verify(entryRepository, never()).deleteByIdVerified(any(), any());
            verify(entryRepository, never()).shiftDownInDayAndCategory(any(), any(), any(), any());
        }
    }

    @Nested
    class retrieveEntry {

        @BeforeEach
        void prepareTests() {
            user = mock(User.class);
            when(user.getId()).thenReturn(USER_ID);
        }

        @Test
        @DisplayName("Given a valid input, finds and returns the requested Entry.")
        void happyFlow() {
            // Arrange
            FoodEntry entry = new FoodEntry();
            FoodEntryResponse expected = defaultFoodEntryResponseBuilder().build();

            when(entryRepository.findByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.of(entry));
            when(entryMapper.generateResponse(entry)).thenReturn(expected);

            // Act
            EntryResponse response = entryService.retrieveEntry(user, ENTRY_ID);

            // Assert
            assertThat(response).isEqualTo(expected);
        }

        @Test
        @DisplayName("Given an invalid user or entryId, throws a ResourceNotFoundException.")
        void entryNotFound() {
            // Arrange
            when(entryRepository.findByIdVerified(USER_ID, ENTRY_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> entryService.retrieveEntry(user, ENTRY_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(entryRepository).findByIdVerified(USER_ID, ENTRY_ID);
            verify(entryMapper, never()).generateResponse(any());
        }

    }

}

/*    //<editor-fold desc="createEntry tests">
    @Test
    @DisplayName("createEntry successfully creates FoodEntry and saves to database.")
    void createEntry_withFoodEntry_happyFlow() {
        // Arrange
        FoodEntryCreateRequest request = defaultFoodEntryCreateRequest();
        FoodEntry entry = defaultFoodEntry();
        when(entryMapper.createFromRequest(request)).thenReturn(entry);

        Day day = new Day();
        when(dayRepository.findByIdVerified(1L, 88L)).thenReturn(Optional.of(day));

        Food food = defaultFood();
        when(foodRepository.findByIdVerified(1L, 99L)).thenReturn(Optional.of(food));

        when(entryRepository.countInDayAndCategory(88L, Category.BREAKFAST)).thenReturn(2L);

        FoodEntry saved = new FoodEntry();
        when(entryRepository.save(entry)).thenReturn(saved);

        FoodEntryResponse expected = defaultFoodEntryResponse(defaultFoodResponse());
        when(entryMapper.generateResponse(saved)).thenReturn(expected);

        // Act
        EntryResponse response = entryService.createEntry(user, 88L, request);

        // Assert
        assertThat(response).isEqualTo(expected);
        assertThat(entry.getDay()).isEqualTo(day);
        assertThat(entry.getFood()).isEqualTo(food);
        assertThat(entry.getPosition()).isEqualTo(3);
        assertThat(entry.getCalories()).isCloseTo(97.0, within(0.01));
        assertThat(entry.getProtein()).isCloseTo(12.0, within(0.01));
        assertThat(entry.getCarbs()).isCloseTo(37.5, within(0.01));
        assertThat(entry.getFat()).isCloseTo(4.5, within(0.01));
        assertThat(entry.getFiber()).isCloseTo(6.0, within(0.01));
        assertThat(entry.getPrice()).isCloseTo(3.77, within(0.01));
    }

    @Test
    @DisplayName("createEntry fails to find the requested day.")
    void createEntry_throwsDayNotFound() {
        // Arrange
        FoodEntryCreateRequest request = defaultFoodEntryCreateRequest();
        FoodEntry entry = new FoodEntry();
        when(entryMapper.createFromRequest(request)).thenReturn(entry);

        when(dayRepository.findByIdVerified(1L, 88L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> entryService.createEntry(user, 88L, request))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(entry.getDay()).isNull();
    }

    @Test
    @DisplayName("createEntry fails to find the requested food.")
    void createEntry_withFoodEntry_throwsFoodNotFound() {
        // Arrange
        FoodEntryCreateRequest request = defaultFoodEntryCreateRequest();
        FoodEntry entry = new FoodEntry();
        when(entryMapper.createFromRequest(request)).thenReturn(entry);

        Day day = new Day();
        when(dayRepository.findByIdVerified(1L, 88L)).thenReturn(Optional.of(day));

        when(foodRepository.findByIdVerified(1L, 99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> entryService.createEntry(user, 88L, request))
                .isInstanceOf(ResourceNotFoundException.class);
        assertThat(entry.getDay()).isEqualTo(day);
        assertThat(entry.getFood()).isNull();
    }
    //</editor-fold>

    //<editor-fold desc="duplicateEntry tests">
    @Test
    @DisplayName("duplicateEntry successfully copies the requested Entry to the requested Day.")
    void duplicateEntry_happyFlow() {
        // Arrange
        EntryDuplicateRequest request = new EntryDuplicateRequest(88L, Category.BREAKFAST);
        FoodEntry entry = defaultFoodEntry();
        Food food = defaultFood();
        entry.setFood(food);

        when(entryRepository.findByIdVerified(1L, 88L)).thenReturn(Optional.of(entry));

        Day day = new Day();
        when(dayRepository.findByIdVerified(1L, 77L)).thenReturn(Optional.of(day));

        when(entryRepository.countInDayAndCategory(77L, Category.BREAKFAST)).thenReturn(2L);

        FoodEntry saved = new FoodEntry();
        when(entryRepository.save(any())).thenReturn(saved);

        FoodEntryResponse expected = defaultFoodEntryResponse(defaultFoodResponse());
        when(entryMapper.generateResponse(saved)).thenReturn(expected);

        ArgumentCaptor<FoodEntry> captor = ArgumentCaptor.forClass(FoodEntry.class);

        // Act
        EntryResponse response = entryService.duplicateEntry(user, 88L, request);

        // Assert
        assertThat(response).isEqualTo(expected);
        verify(entryRepository).save(captor.capture());
        FoodEntry copy = captor.getValue();
        assertThat(copy).isNotSameAs(entry);    // The entities don't have IDs, so we use object comparison.
        assertThat(copy.getDay()).isEqualTo(day);
        assertThat(copy.getCategory()).isEqualTo(Category.BREAKFAST);
        assertThat(copy.getFood()).isEqualTo(food);
        assertThat(copy.getGrams()).isEqualTo(entry.getGrams());
        assertThat(copy.getDisplayUnit()).isEqualTo(entry.getDisplayUnit());
        assertThat(copy.getDisplayMerchant()).isEqualTo(entry.getDisplayMerchant());
    }

    @Test
    @DisplayName("duplicateEntry fails to find the requested entry.")
    void duplicateEntry_throwsEntryNotFound() {
        // Arrange
        EntryDuplicateRequest request = new EntryDuplicateRequest(88L, Category.BREAKFAST);

        when(entryRepository.findByIdVerified(1L, 88L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> entryService.duplicateEntry(user, 77L, request))
            .isInstanceOf(ResourceNotFoundException.class);
        verify(dayRepository, never()).findByIdVerified(any(), any());
    }

    @Test
    @DisplayName("duplicateEntry fails to find the requested day.")
    void duplicateEntry_throwsDayNotFound() {
        // Arrange
        EntryDuplicateRequest request = new EntryDuplicateRequest(88L, Category.BREAKFAST);

        FoodEntry entry = new FoodEntry();
        when(entryRepository.findByIdVerified(1L, 88L)).thenReturn(Optional.of(entry));

        when(dayRepository.findByIdVerified(1L, 77L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> entryService.duplicateEntry(user, 77L, request))
            .isInstanceOf(ResourceNotFoundException.class);
    }
    //</editor-fold>

    //<editor-fold desc="editEntry tests">
    @Test
    @DisplayName("editEntry successfully edits the requested entry.")
    void editEntry_happyFlow() {
        // Arrange
        FoodEntry entry = new FoodEntry();
        FoodEntryEditRequest request = defaultFoodEntryEditRequest();

        when(entryRepository.findByIdVerified(1L, 88L)).thenReturn(Optional.of(entry));

        FoodEntryResponse expected = defaultFoodEntryResponse(defaultFoodResponse());
        when(entryMapper.generateResponse(entry)).thenReturn(expected);

        // Act
        EntryResponse response = entryService.editEntry(user, 88L, request);

        // Assert
        assertThat(response).isEqualTo(expected);
        verify(entryMapper).updateFromRequest(entry, request);
    }

    @Test
    @DisplayName("editEntry fails to find the requested entry.")
    void editEntry_throwsEntryNotFound() {
        // Arrange
        FoodEntryEditRequest request = defaultFoodEntryEditRequest();

        when(entryRepository.findByIdVerified(1L, 88L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> entryService.editEntry(user, 88L, request))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(entryMapper, never()).updateFromRequest(any(), any());
    }
    //</editor-fold>

    //<editor-fold desc="moveEntry tests">

    @Test
    @DisplayName("moveEntry successfully moves the requested entry up in the same category.")
    void moveEntry_withSameCategoryUp_happyFlow() {
        // TO DO
    }

    @Test
    @DisplayName("moveEntry successfully moves the requested entry down in the same category.")
    void moveEntry_withSameCategoryDown_happyFlow() {
        // TO DO
    }

    @ParameterizedTest
    @DisplayName("moveEntry successfully moves the requested entry in from another category.")
    @ValueSource(ints = {
        8,      // desiredPosition < categoryCount
        10,     // desiredPosition = categoryCount
        12      // desiredPosition > categoryCount
    })
    void moveEntry_withDifferentCategory_happyFlow(int desiredPosition) {
        // TO DO
    }

    @Test
    @DisplayName("moveEntry makes no changes if requested entry is already in desired position.")
    void moveEntry_noMovementNeeded_happyFlow() {
        // TO DO
    }

    @Test
    @DisplayName("moveEntry fails to find the requested entry.")
    void moveEntry_throwsEntryNotFound() {
        // Arrange
        EntryMoveRequest request = new EntryMoveRequest(Category.LUNCH, 4);
        when(entryRepository.findShallowByIdAndDayVerified(1L, 77L, 88L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> entryService.moveEntry(user, 77L, 88L, request))
            .isInstanceOf(ResourceNotFoundException.class);
        verify(entryRepository, never()).countInDayAndCategory(any(), any());
    }
    //</editor-fold>

    //<editor-fold desc="delete Entry tests"
    @Test
    @DisplayName("deleteEntry successfully deletes the requested entry.")
    void deleteEntry_happyFlow() {
        // Arrange
        Tuple positionData = mock(Tuple.class);
        when(positionData.get("dayId", Long.class)).thenReturn(77L);
        when(positionData.get("category", Category.class)).thenReturn(Category.BREAKFAST);
        when(positionData.get("position", Integer.class)).thenReturn(5);
        when(entryRepository.findPositionDataByIdVerified(1L, 88L)).thenReturn(Optional.of(positionData));

        when(entryRepository.deleteByIdVerified(1L, 88L)).thenReturn(1);

        // Act
        entryService.deleteEntry(user, 88L);

        // Assert
        verify(entryRepository).deleteByIdVerified(1L, 88L);
        verify(entryRepository).shiftDownInDayAndCategory(77L, Category.BREAKFAST, 5, null);
    }

    @Test
    @DisplayName("deleteEntry fails to find the requested entry.")
    void deleteEntry_throwsEntryNotFound() {
        // Arrange
        when(entryRepository.findPositionDataByIdVerified(1L, 88L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> entryService.deleteEntry(user, 88L))
            .isInstanceOf(ResourceNotFoundException.class);
    }
    //</editor-fold>

    //<editor-fold desc="retrieveEntry tests">
    @Test
    @DisplayName("retrieveEntry successfully returns the requested entry.")
    void retrieveEntry_happyFlow() {
        // Arrange
        FoodEntry entry = new FoodEntry();
        when(entryRepository.findByIdVerified(1L, 88L)).thenReturn(Optional.of(entry));

        FoodEntryResponse expected = defaultFoodEntryResponse(defaultFoodResponse());
        when(entryMapper.generateResponse(entry)).thenReturn(expected);

        // Act
        EntryResponse response = entryService.retrieveEntry(user, 88L);

        // Assert
        assertThat(response).isEqualTo(expected);
    }

    @Test
    @DisplayName("retrieveEntry fails to find the requested entry.")
    void retrieveEntry_throwsEntryNotFound() {
        // Arrange
        when(entryRepository.findByIdVerified(1L, 88L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> entryService.retrieveEntry(user, 88L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
    //</editor-fold>
}
*/