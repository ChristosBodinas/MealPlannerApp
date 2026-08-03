package org.example.mealplannerapp.service;

import org.example.mealplannerapp.dto.food.request.FoodRequest;
import org.example.mealplannerapp.dto.food.request.PriceRequest;
import org.example.mealplannerapp.dto.food.request.UnitRequest;
import org.example.mealplannerapp.dto.food.response.FoodResponse;
import org.example.mealplannerapp.dto.food.response.ListedFoodResponse;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.exception.ServiceValidationException;
import org.example.mealplannerapp.mapper.FoodMapper;
import org.example.mealplannerapp.mapper.FoodMapperImpl;
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

import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.example.mealplannerapp.fixture.FoodTestFixtures.defaultFoodBuilder;
import static org.example.mealplannerapp.fixture.FoodTestFixtures.defaultFoodRequestBuilder;
import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUserBuilder;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FoodServiceUnitTests {

    // MOCKS, SPIES, CAPTORS
    @Mock
    private FoodRepository foodRepository;
    @Captor
    private ArgumentCaptor<Food> foodCaptor;

    // VARIABLES
    private FoodMapper foodMapper;
    private FoodService foodService;
    private User myUser;

    // CONSTANTS
    private static final long USER_ID = 1L;
    private static final long FOOD_ID = 99L;

    // HELPER METHODS
    private static Stream<Arguments> provideDuplicateInputs() {

        FoodRequest duplicateUnits = defaultFoodRequestBuilder().units(new HashSet<>(Set.of(
                        new UnitRequest("tbsp", 15.0),
                        new UnitRequest("tbsp", 17.0))))
                .build();

        FoodRequest duplicateVendors = defaultFoodRequestBuilder().prices(new HashSet<>(Set.of(
                        new PriceRequest("Masoutis", 6.80, 200),
                        new PriceRequest("Masoutis", 8.60, 240))))
                .build();

        return Stream.of(
                argumentSet("Duplicate Unit Names", duplicateUnits),
                argumentSet("Duplicate Vendor Names", duplicateVendors)
        );
    }

    // BEFORE EACH
    @BeforeEach
    void prepareALlTests() {
        myUser = defaultUserBuilder().id(USER_ID).build();

        foodMapper = new FoodMapperImpl();
        foodService = new FoodService(foodRepository, foodMapper);
    }

    // TESTS PROPER
    @Nested
    @DisplayName("createFood")
    class CreateFood {

        @Test
        @DisplayName("Creates a new food and saves it to the database when the input data is valid.")
        void foodCreated() {
            // Arrange
            FoodRequest request = defaultFoodRequestBuilder().build();
            Food saved = defaultFoodBuilder().id(FOOD_ID).build();

            when(foodRepository.save(any(Food.class))).thenReturn(saved);

            // Act
            FoodResponse result = foodService.createFood(myUser, request);

            // Assert
            assertThat(result).isEqualTo(foodMapper.generateResponse(saved));

            verify(foodRepository).save(foodCaptor.capture());
            assertThat(foodCaptor.getValue().getUser()).isEqualTo(myUser);
            assertThat(foodCaptor.getValue())
                    .usingRecursiveComparison()
                    .ignoringFields("id", "user")
                    .isEqualTo(foodMapper.createFromRequest(request));
        }

        @ParameterizedTest
        @DisplayName("Throws ServiceValidationErrorException when the input data contains duplicate unit or vendor names.")
        @MethodSource("org.example.mealplannerapp.service.FoodServiceUnitTests#provideDuplicateInputs")
        void duplicateUnitsOrPrices(FoodRequest request) {
            // Act + Assert
            assertThatThrownBy(() -> foodService.createFood(myUser, request))
                    .isInstanceOf(ServiceValidationException.class);
            verify(foodRepository, never()).save(any(Food.class));
        }

    }

    @Nested
    @DisplayName("updateFood")
    class UpdateFood {

        private static final double CALORIES_BEFORE = 120.0;
        private static final double CALORIES_AFTER = 153.0;

        @Test
        @DisplayName("Updates the requested food when it exists and belongs to the given user and the input data is valid.")
        void foodUpdated() {
            // Arrange
            FoodRequest request = defaultFoodRequestBuilder().caloriesPer100g(CALORIES_AFTER).build();
            Food found = defaultFoodBuilder().id(FOOD_ID).user(myUser).caloriesPer100g(CALORIES_BEFORE).build();

            when(foodRepository.fetchByIdVerified(USER_ID, FOOD_ID)).thenReturn(Optional.of(found));

            // Act
            FoodResponse result = foodService.updateFood(myUser, FOOD_ID, request);

            // Assert
            assertThat(result).isEqualTo(foodMapper.generateResponse(found));
            assertThat(found.getCaloriesPer100g()).isEqualTo(CALORIES_AFTER);
        }

        @ParameterizedTest
        @DisplayName("Throws a ServiceValidationException when the input data contains duplicate unit or vendor names.")
        @MethodSource("org.example.mealplannerapp.service.FoodServiceUnitTests#provideDuplicateInputs")
        void duplicateUnitsOrPrices(FoodRequest request) {
            // Arrange
            FoodMapper spyMapper = spy(foodMapper);

            // Act + Assert
            assertThatThrownBy(() -> foodService.updateFood(myUser, FOOD_ID, request))
                    .isInstanceOf(ServiceValidationException.class);
            verifyNoInteractions(spyMapper);
        }

        @Test
        @DisplayName("Throws a ResourceNotFoundException when the requested entry does not exist or belongs to a different user.")
        void foodNotFound() {
            // Arrange
            FoodRequest request = defaultFoodRequestBuilder().build();

            when(foodRepository.fetchByIdVerified(USER_ID, FOOD_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> foodService.updateFood(myUser, FOOD_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

    }

    @Nested
    @DisplayName("deleteFood")
    class DeleteFood {

        @Test
        @DisplayName("Deletes the requested food when it exists and belongs to the given user.")
        void foodDeleted() {
            // Arrange
            when(foodRepository.deleteByIdVerified(USER_ID, FOOD_ID)).thenReturn(1);

            // Act
            assertThatCode(() -> foodService.deleteFood(myUser, FOOD_ID))
                    .doesNotThrowAnyException();

            // Assert
            verify(foodRepository).deleteByIdVerified(USER_ID, FOOD_ID);
        }

        @Test
        @DisplayName("Throws a ResourceNotFoundException if the requested food does not exist or belongs to a different user.")
        void foodNotFound() {
            // Arrange
            when(foodRepository.deleteByIdVerified(USER_ID, FOOD_ID)).thenReturn(0);

            // Act + Assert
            assertThatThrownBy(() -> foodService.deleteFood(myUser, FOOD_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(foodRepository).deleteByIdVerified(USER_ID, FOOD_ID);
        }

    }

    @Nested
    @DisplayName("retrieveFood")
    class RetrieveFood {

        @Test
        @DisplayName("Returns the requested food's full data when it exists and belongs to the given user.")
        void foodRetrieved() {
            // Arrange
            Food found = defaultFoodBuilder().id(FOOD_ID).user(myUser).build();

            when(foodRepository.fetchByIdVerified(USER_ID, FOOD_ID)).thenReturn(Optional.of(found));

            // Act
            FoodResponse result = foodService.retrieveFood(myUser, FOOD_ID);

            // Assert
            assertThat(result).isEqualTo(foodMapper.generateResponse(found));
        }

        @Test
        @DisplayName("Throws a ResourceNotFoundException when the requested food does not exist or belongs to a different user.")
        void foodNotFound() {
            // Arrange
            when(foodRepository.fetchByIdVerified(USER_ID, FOOD_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> foodService.retrieveFood(myUser, FOOD_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("searchFoods")
    class SearchFoods {

        List<Food> listedFoods(int number) {
            List<Food> foods = new ArrayList<>(number);

            for (int i = 1; i <= number; i++) {
                Food food = defaultFoodBuilder()
                        .id((long) i)
                        .user(myUser)
                        .name("Listed Food #" + i)
                        .build();
                foods.add(food);
            }

            return foods;
        }

        @Test
        @DisplayName("Returns a list of matching foods when given a user and a search string.")
        void foodsRetrieved() {
            // Arrange
            List<Food> foods = listedFoods(5);

            when(foodRepository.fetchShallowByUserAndText(USER_ID, "text")).thenReturn(foods);

            // Act
            List<ListedFoodResponse> results = foodService.searchFoods(myUser, "text");

            // Assert
            assertThat(results).isEqualTo(foods.stream().map(foodMapper::generateListedResponse).toList());
        }

    }

}