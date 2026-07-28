package org.example.mealplannerapp.service;

import org.example.mealplannerapp.dto.food.request.FoodRequest;
import org.example.mealplannerapp.dto.food.request.PriceRequest;
import org.example.mealplannerapp.dto.food.request.UnitRequest;
import org.example.mealplannerapp.dto.food.response.FoodResponse;
import org.example.mealplannerapp.dto.food.response.ListedFoodResponse;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.exception.ServiceValidationErrorException;
import org.example.mealplannerapp.mapper.FoodMapper;
import org.example.mealplannerapp.mapper.FoodMapperImpl;
import org.example.mealplannerapp.repository.FoodRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.mealplannerapp.fixture.FoodTestFixtures.*;
import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUserBuilder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FoodServiceUnitTests {

    // MOCKS
    @Mock
    private FoodRepository foodRepository;

    // VARIABLES
    private FoodMapper foodMapper;
    private FoodService foodService;
    private User user;

    // CONSTANTS
    private final Long USER_ID = 1L;
    private final Long FOOD_ID = 99L;

    @BeforeEach
    void prepareALlTests() {
        user = defaultUserBuilder().id(USER_ID).build();

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
            Food saved = defaultFoodBuilder().build();

            // Act

            // Assert

        }

        @Test
        @DisplayName("Throws ServiceValidationErrorException when the input data contains duplicate unit or vendor names.")
        void duplicateUnitsOrPrices() {

        }

    }
    /*


    // TESTS PROPER
    @Nested
    class createFood {

        @BeforeEach
        void prepareTests() {
            user = new User();
        }

        @Test
        @DisplayName("Given a valid input, creates and saves the new Food.")
        void happyFlow() {
            // Arrange
            FoodRequest request = defaultFoodRequestBuilder().build();
            Food created = new Food();
            Food saved = new Food();
            FoodResponse expected = defaultFoodResponseBuilder().build();

            when(foodMapper.createFromRequest(request)).thenReturn(created);
            when(foodRepository.save(created)).thenReturn(saved);
            when(foodMapper.generateResponse(saved)).thenReturn(expected);

            // Act
            FoodResponse response = foodService.createFood(user, request);

            // Assert
            assertThat(response).isEqualTo(expected);
        }

        @ParameterizedTest
        @DisplayName("Given duplicate units or merchants, throws an IllegalDuplicateValueException.")
        @EnumSource(DupeType.class)
        void duplicateUnitsOrPrices(DupeType dupeType) {
            // Arrange
            FoodRequest request;
            if (dupeType == DupeType.UNITS) {
                request = defaultFoodRequestBuilder().units(
                        Set.of(new UnitRequest("tbsp", 15.0), new UnitRequest("tbsp", 27.0))
                ).build();
            } else {
                request = defaultFoodRequestBuilder().prices(
                        Set.of(new PriceRequest("Masoutis", 6.80, 200), new PriceRequest("Masoutis", 5.30, 500))
                ).build();
            }

            // Act + Assert
            assertThatThrownBy(() -> foodService.createFood(user, request))
                    .isInstanceOf(ServiceValidationErrorException.class);
            verify(foodMapper, never()).updateFromRequest(any(), any());
        }

    }

    @Nested
    class updateFood {

        private FoodRequest request;

        @BeforeEach
        void prepareTests() {
            user = mock(User.class);
        }

        @Test
        @DisplayName("Given a valid input, finds and updates the requested Food.")
        void happyFlow() {
            // Arrange
            request = defaultFoodRequestBuilder().build();
            Food found = new Food();
            FoodResponse expected = defaultFoodResponseBuilder().build();

            when(user.getId()).thenReturn(USER_ID);
            when(foodRepository.fetchByIdVerified(USER_ID, FOOD_ID)).thenReturn(Optional.of(found));
            when(foodMapper.generateResponse(found)).thenReturn(expected);

            // Act
            FoodResponse response = foodService.updateFood(user, FOOD_ID, request);

            // Assert
            assertThat(response).isEqualTo(expected);
            verify(foodMapper).updateFromRequest(found, request);
        }

        @ParameterizedTest
        @DisplayName("Given duplicate units or merchants, throws an IllegalDuplicateValueException.")
        @EnumSource(DupeType.class)
        void duplicateUnitsOrPrices(DupeType dupeType) {
            // Arrange
            if (dupeType == DupeType.UNITS) {
                request = defaultFoodRequestBuilder().units(
                        Set.of(new UnitRequest("tbsp", 15.0), new UnitRequest("tbsp", 27.0))
                ).build();
            } else {
                request = defaultFoodRequestBuilder().prices(
                        Set.of(new PriceRequest("Masoutis", 6.80, 200), new PriceRequest("Masoutis", 5.30, 500))
                ).build();
            }

            // Act + Assert
            assertThatThrownBy(() -> foodService.updateFood(user, FOOD_ID, request))
                    .isInstanceOf(ServiceValidationErrorException.class);
            verify(foodRepository, never()).fetchByIdVerified(anyLong(), anyLong());
            verify(foodMapper, never()).updateFromRequest(any(), any());
        }

        @Test
        @DisplayName("Given an invalid user or foodId, throws a ResourceNotFoundException.")
        void foodNotFound() {
            // Arrange
            request = defaultFoodRequestBuilder().build();

            when(user.getId()).thenReturn(USER_ID);
            when(foodRepository.fetchByIdVerified(USER_ID, FOOD_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> foodService.updateFood(user, FOOD_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(foodMapper, never()).updateFromRequest(any(), any());
        }

    }

    @Nested
    class deleteFood {

        @BeforeEach
        void prepareTests() {
            user = mock(User.class);
            when(user.getId()).thenReturn(USER_ID);
        }

        @Test
        @DisplayName("Given a valid input, finds and deletes the requested Food.")
        void happyFlow() {
            // Arrange
            when(foodRepository.deleteByIdVerified(USER_ID, FOOD_ID)).thenReturn(1);

            // Act
            foodService.deleteFood(user, FOOD_ID);

            // Assert
            verify(foodRepository).deleteByIdVerified(USER_ID, FOOD_ID);
        }

        @Test
        @DisplayName("Given an invalid user or foodId, throws a ResourceNotFoundException.")
        void foodNotFound() {
            // Arrange
            when(foodRepository.deleteByIdVerified(USER_ID, FOOD_ID)).thenReturn(0);

            // Act + Assert
            assertThatThrownBy(() -> foodService.deleteFood(user, FOOD_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(foodRepository).deleteByIdVerified(USER_ID, FOOD_ID);
        }

    }

    @Nested
    class retrieveFood {

        @BeforeEach
        void prepareTests() {
            user = mock(User.class);
            when(user.getId()).thenReturn(USER_ID);
        }

        @Test
        @DisplayName("Given a valid input, finds and returns the requested Food.")
        void happyFlow() {
            // Arrange
            Food found = new Food();
            FoodResponse expected = defaultFoodResponseBuilder().build();

            when(foodRepository.fetchByIdVerified(USER_ID, FOOD_ID)).thenReturn(Optional.of(found));
            when(foodMapper.generateResponse(found)).thenReturn(expected);

            // Act
            FoodResponse response = foodService.retrieveFood(user, FOOD_ID);

            // Assert
            assertThat(response).isEqualTo(expected);
        }

        @Test
        @DisplayName("Given an invalid user or foodId, throws a ResourceNotFoundException.")
        void foodNotFound() {
            // Arrange
            when(foodRepository.fetchByIdVerified(USER_ID, FOOD_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> foodService.retrieveFood(user, FOOD_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(foodRepository).fetchByIdVerified(USER_ID, FOOD_ID);
            verify(foodMapper, never()).generateResponse(any());
        }

    }

    @Nested
    class searchFoods {

        List<Food> listedFoods() {
            Food food1 = defaultFoodBuilder().name("Listed Food #1").build();
            Food food2 = defaultFoodBuilder().name("Listed Food #2").build();
            return List.of(food1, food2);
        }

        List<ListedFoodResponse> listedResponses() {
            ListedFoodResponse expected1 = defaultListedFoodResponseBuilder().name("Listed Food #1").build();
            ListedFoodResponse expected2 = defaultListedFoodResponseBuilder().name("Listed Food #2").build();
            return List.of(expected1, expected2);
        }

        @Test
        @DisplayName("Given a user and a search text, returns the core data of the repository results.")
        void happyFlow() {
            // Arrange
            user = mock(User.class);
            when(user.getId()).thenReturn(USER_ID);
            String search = "text";
            List<Food> listedFoods = listedFoods();
            List<ListedFoodResponse> expected = listedResponses();

            when(foodRepository.fetchShallowByUserAndText(USER_ID, search)).thenReturn(listedFoods);
            when(foodMapper.generateListedResponse(listedFoods.get(0))).thenReturn(expected.get(0));
            when(foodMapper.generateListedResponse(listedFoods.get(1))).thenReturn(expected.get(1));

            // Act
            List<ListedFoodResponse> response = foodService.searchFoods(user, search);

            // Assert
            assertThat(response).isEqualTo(expected);
        }

    }
     */

}