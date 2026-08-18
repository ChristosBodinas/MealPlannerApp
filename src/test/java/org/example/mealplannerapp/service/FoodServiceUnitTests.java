package org.example.mealplannerapp.service;

import org.example.mealplannerapp.dto.food.request.FoodRequest;
import org.example.mealplannerapp.dto.food.request.PriceRequest;
import org.example.mealplannerapp.dto.food.request.UnitRequest;
import org.example.mealplannerapp.dto.food.response.FoodResponse;
import org.example.mealplannerapp.dto.food.response.ListedFoodResponse;
import org.example.mealplannerapp.embeddable.FoodPrice;
import org.example.mealplannerapp.embeddable.FoodUnit;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.exception.DuplicateValueException;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.example.mealplannerapp.fixture.FoodTestFixtures.defaultFood;
import static org.example.mealplannerapp.fixture.FoodTestFixtures.defaultFoodRequest;
import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUser;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FoodServiceUnitTests {

    // CONSTANTS
    private static final long USER_ID = 1L;
    private static final long FOOD_ID = 99L;

    // VARIABLES
    private FoodService foodService;
    private FoodMapper foodMapper;
    @Mock private FoodRepository foodRepository;
    @Captor private ArgumentCaptor<Food> captor;

    private User myUser;

    // HELPER METHODS
    private static Stream<Arguments> provideInvalidRequests() {
        Set<UnitRequest> duplicateUnits = new HashSet<>(Set.of(
                new UnitRequest("unit", BigDecimal.valueOf(10)),
                new UnitRequest("unit", BigDecimal.valueOf(20))
        ));

        Set<PriceRequest> duplicatePrices = new HashSet<>(Set.of(
                new PriceRequest("vendor", BigDecimal.valueOf(10), BigDecimal.valueOf(5)),
                new PriceRequest("vendor", BigDecimal.valueOf(20), BigDecimal.valueOf(4))
        ));

        return Stream.of(
                argumentSet("Duplicate units", defaultFoodRequest().units(duplicateUnits).build()),
                argumentSet("Duplicate Prices", defaultFoodRequest().prices(duplicatePrices).build())
        );
    }

    // TESTS PROPER
    @BeforeEach
    void prepareServiceAndUser() {
        foodMapper = new FoodMapperImpl();
        foodService = new FoodService(foodRepository, foodMapper);

        myUser = defaultUser().id(USER_ID).build();
    }

    @Nested
    @DisplayName("createFood")
    class CreateFood {

        @Test
        @DisplayName("Given a valid FoodRequest, creates and saves a new Food owned by the current user, " +
                "then returns a FoodResponse")
        void validRequest_createsFoodAndReturnsResponse() {
            // Arrange
            FoodRequest request = defaultFoodRequest().build();
            Food saved = defaultFood().id(FOOD_ID).user(myUser).build();

            when(foodRepository.save(any(Food.class))).thenReturn(saved);

            // Act
            FoodResponse response = foodService.createFood(myUser, request);

            // Assert
            assertThat(response).as("Method output should match mapper output.")
                    .isEqualTo(foodMapper.toResponse(saved));

            verify(foodRepository).save(captor.capture());
            Food created = captor.getValue();

            assertThat(created.getUser()).as("New food should be owned by the current user.")
                    .isEqualTo(myUser);    // TODO: does this work? why?

            assertThat(created).as("New food fields should match request data.")
                    .usingRecursiveComparison()
                    .ignoringFields("id", "user")
                    .isEqualTo(request);
        }

        @ParameterizedTest
        @DisplayName("Given a FoodRequest with duplicate unit or vendor names, throws a DuplicateValueException.")
        @MethodSource("org.example.mealplannerapp.service.FoodServiceUnitTests#provideInvalidRequests")
        void duplicateUnitOrVendorNames_throwsDuplicateValue(FoodRequest request) {
            // Act + Assert
            assertThatThrownBy(() -> foodService.createFood(myUser, request))
                    .as("Method should throw a DuplicateValueException.")
                    .isInstanceOf(DuplicateValueException.class);
        }

    }

    @Nested
    @DisplayName("updateFood")
    class UpdateFood {

        private FoodRequest prepareValidRequest(Food food) {
            Set<UnitRequest> unitRequests = new HashSet<>(food.getUnits().size());
            for (FoodUnit unit : food.getUnits()) {
                unitRequests.add(new UnitRequest(
                        unit.getName() + "_edited",
                        unit.getGrams().add(BigDecimal.valueOf(5))));
            }

            Set<PriceRequest> priceRequests = new HashSet<>(food.getPrices().size());
            for (FoodPrice price : food.getPrices()) {
                priceRequests.add(new PriceRequest(
                        price.getVendorName() + "_edited",
                        price.getPurchasePrice().add(BigDecimal.valueOf(4)),
                        price.getPurchaseGrams().add(BigDecimal.valueOf(3))));
            }

            return FoodRequest.builder()
                    .name(food.getName() + "_edited")
                    .brand(food.getBrand() + "_edited")
                    .calories100g(food.getCalories100g().add(BigDecimal.valueOf(10)))
                    .protein100g(food.getProtein100g().add(BigDecimal.valueOf(9)))
                    .carbs100g(food.getCarbs100g().add(BigDecimal.valueOf(8)))
                    .fat100g(food.getFat100g().add(BigDecimal.valueOf(7)))
                    .fiber100g(food.getFiber100g().add(BigDecimal.valueOf(6)))
                    .edibleRatio(food.getEdibleRatio().subtract(BigDecimal.valueOf(5)))
                    .units(unitRequests)
                    .prices(priceRequests)
                    .build();
        }

        @Test
        @DisplayName("Given a valid foodId owned by the current user and a valid FoodRequest, " +
                "updates the requested Food and returns a FoodResponse.")
        void validIdAndRequest_updatesFoodAndReturnsResponse() {
            // Arrange
            Food fetched = defaultFood().id(FOOD_ID).user(myUser).build();
            FoodRequest request = prepareValidRequest(fetched);

            when(foodRepository.fetchByIdVerified(USER_ID, FOOD_ID)).thenReturn(Optional.of(fetched));

            // Act
            FoodResponse response = foodService.updateFood(myUser, FOOD_ID, request);

            // Assert
            assertThat(response).as("Method output should match mapper output.")
                    .isEqualTo(foodMapper.toResponse(fetched));

            assertThat(fetched).as("Updated food fields should match request data.")
                    .usingRecursiveComparison()
                    .ignoringFields("id", "user")
                    .isEqualTo(request);
        }

        @Test
        @DisplayName("Given an invalid or non-owned foodId, throws a ResourceNotFoundException.")
        void invalidId_throwsResourceNotFound() {
            // Arrange
            FoodRequest request = defaultFoodRequest().build();

            when(foodRepository.fetchByIdVerified(USER_ID, FOOD_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> foodService.updateFood(myUser, FOOD_ID, request))
                    .as("Method should throw a ResourceNotFoundException.")
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @ParameterizedTest
        @DisplayName("Given a FoodRequest with duplicate unit or vendor names, throws a DuplicateValueException.")
        @MethodSource("org.example.mealplannerapp.service.FoodServiceUnitTests#provideInvalidRequests")
        void duplicateUnitOrVendorNames_throwsDuplicateValue(FoodRequest request) {
            // Act + Assert
            assertThatThrownBy(() -> foodService.updateFood(myUser, FOOD_ID, request))
                    .as("Method should throw a DuplicateValueException.")
                    .isInstanceOf(DuplicateValueException.class);
        }
    }

    @Nested
    @DisplayName("deleteFood")
    class DeleteFood {

        @Test
        @DisplayName("Given a valid foodId owned by the current user, deletes the requested Food.")
        void validId_deletesFood() {
            // Arrange
            when(foodRepository.deleteByIdVerified(USER_ID, FOOD_ID)).thenReturn(1);

            // Act + Assert
            assertThatCode(() -> foodService.deleteFood(myUser, FOOD_ID))
                    .as("Method should complete without exceptions.")
                    .doesNotThrowAnyException();
            verify(foodRepository).deleteByIdVerified(USER_ID, FOOD_ID);    // TODO: add description
        }

        @Test
        @DisplayName("Given an invalid or non-owned foodId, throws a ResourceNotFoundException.")
        void invalidId_throwsResourceNotFound() {
            // Arrange
            when(foodRepository.deleteByIdVerified(USER_ID, FOOD_ID)).thenReturn(0);

            // Act + Assert
            assertThatThrownBy(() -> foodService.deleteFood(myUser, FOOD_ID))
                    .as("Method should throw a ResourceNotFoundException.")
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("retrieveFood")
    class RetrieveFood {

        @Test
        @DisplayName("Given a valid foodId owned by the current user, returns a FoodResponse.")
        void validId_returnsFoodResponse() {
            // Arrange
            Food fetched = defaultFood().id(FOOD_ID).user(myUser).build();

            when(foodRepository.fetchByIdVerified(USER_ID, FOOD_ID)).thenReturn(Optional.of(fetched));

            // Act
            FoodResponse response = foodService.retrieveFood(myUser, FOOD_ID);

            // Assert
            assertThat(response).as("Method output should match mapper output.")
                    .isEqualTo(foodMapper.toResponse(fetched));
        }

        @Test
        @DisplayName("Given an invalid or non-owned foodId, throws a ResourceNotFoundException.")
        void invalidId_throwsResourceNotFound() {
            // Arrange
            when(foodRepository.fetchByIdVerified(USER_ID, FOOD_ID)).thenReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> foodService.retrieveFood(myUser, FOOD_ID))
                    .as("Method should throw a ResourceNotFoundException.")
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("searchFoods")
    class SearchFoods {

        @Test
        @DisplayName("Returns a Page of ListedFoodResponse DTOs.")
        void returnsMatchingFoods() {
            // Arrange
            List<Food> foods = new ArrayList<>(List.of(
                    defaultFood().name("match1").user(myUser).build(),
                    defaultFood().name("match2").user(myUser).build(),
                    defaultFood().name("match3").user(myUser).build()
            ));

            Pageable pageable = PageRequest.of(0, 3);
            Page<Food> foodsPage = new PageImpl<>(foods, pageable, 3);

            when(foodRepository.fetchShallowByUserAndText(USER_ID, "match", pageable)).thenReturn(foodsPage);

            // Act
            Page<ListedFoodResponse> response = foodService.searchFoods(myUser, "match", pageable);

            // Assert
            assertThat(response).as("Method output should match mapper output.")
                    .isEqualTo(foodsPage.map(foodMapper::toListedResponse));
        }
    }
}
