package org.example.mealplannerapp.service;

import static org.example.mealplannerapp.fixture.UserTestFixtures.defaultUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.example.mealplannerapp.fixture.FoodTestFixtures.*;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.example.mealplannerapp.dto.food.request.FoodRequest;
import org.example.mealplannerapp.dto.food.request.UnitRequest;
import org.example.mealplannerapp.dto.food.request.VendorRequest;
import org.example.mealplannerapp.dto.food.response.FoodResponse;
import org.example.mealplannerapp.dto.food.response.ListedFoodResponse;
import org.example.mealplannerapp.embeddable.ReferenceUnit;
import org.example.mealplannerapp.embeddable.VendorData;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.exception.DuplicateValueException;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.entity.Food;
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

@ExtendWith(MockitoExtension.class)
public class FoodServiceUnitTests {

        // CONSTANT
        private static final long USER_ID = 1L;
        private static final long FOOD_ID = 99L;

        // BEANS
        private FoodService foodService;
        private FoodMapper foodMapper;
        @Mock
        private FoodRepository foodRepository;

        // VARIABLES
        private User myUser;

        // HELPER METHODS
        private static Stream<Arguments> provideInvalidRequests() {
                Set<UnitRequest> duplicateUnits = new HashSet<>(Set.of(
                                new UnitRequest("dupe", BigDecimal.valueOf(5)),
                                new UnitRequest("dupe", BigDecimal.valueOf(10))));

                Set<VendorRequest> duplicateVendors = new HashSet<>(Set.of(
                                new VendorRequest("dupe", BigDecimal.valueOf(5), BigDecimal.valueOf(200)),
                                new VendorRequest("dupe", BigDecimal.valueOf(8), BigDecimal.valueOf(150))));

                return Stream.of(
                                argumentSet("units", defaultFoodRequest().units(duplicateUnits).build()),
                                argumentSet("vendors", defaultFoodRequest().vendors(duplicateVendors).build()));
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
                @DisplayName("Given a valid request, creates a new food owned by the current user, " +
                                "and returns a response.")
                void foodCreated() {
                        // Arrange
                        FoodRequest request = defaultFoodRequest().build();
                        Food saved = defaultFood().id(FOOD_ID).user(myUser).build();

                        when(foodRepository.save(any(Food.class))).thenReturn(saved);

                        // Act
                        FoodResponse response = foodService.createFood(myUser, request);

                        // Assert
                        assertThat(response).as("Method output should match mapper output.")
                                        .isEqualTo(foodMapper.toResponse(saved));

                        ArgumentCaptor<Food> captor = ArgumentCaptor.forClass(Food.class);
                        verify(foodRepository, description("New food should be saved to the database."))
                                        .save(captor.capture());
                        Food created = captor.getValue();

                        assertThat(created.getUser()).as("New food should belong to the current user.")
                                        .isEqualTo(myUser);

                        assertThat(created).as("New food fields should match request fields.")
                                        .usingRecursiveComparison()
                                        .ignoringFields("id", "user")
                                        .isEqualTo(request);
                }

                @ParameterizedTest(name = "Given a request with duplicate {0} names, throws a DuplicateValueException.")
                @MethodSource("org.example.mealplannerapp.service.FoodServiceUnitTests#prepareInvalidRequests")
                @DisplayName("Given a request with duplicate unit or vendor names, throws a DuplicateValueException.")
                void duplicateUnitOrVendorNames(FoodRequest request) {
                        // Act + Assert
                        assertThatThrownBy(() -> foodService.createFood(myUser, request))
                                        .as("Method should throw a DuplicateValueException.")
                                        .isInstanceOf(DuplicateValueException.class);

                        verify(foodRepository, never().description("Nothing should be saved to the database."))
                                        .save(any(Food.class));
                }

        }

        @Nested
        @DisplayName("updateFood")
        class UpdateFood {

                private FoodRequest prepareValidRequest(Food food) {
                        Set<UnitRequest> unitRequests = new HashSet<>(food.getUnits().size());
                        for (ReferenceUnit unit : food.getUnits()) {
                                unitRequests.add(new UnitRequest(
                                                unit.getName() + "_edited",
                                                unit.getGrams().add(BigDecimal.valueOf(5))));
                        }

                        Set<VendorRequest> vendorRequests = new HashSet<>(food.getVendors().size());
                        for (VendorData price : food.getVendors()) {
                                vendorRequests.add(new VendorRequest(
                                                price.getName() + "_edited",
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
                                        .vendors(vendorRequests)
                                        .build();
                }

                @Test
                @DisplayName("Given an existing foodId owned by the current user and a valid request, " +
                                "updates the requested food and returns a response.")
                void foodUpdated() {
                        // Arrange
                        Food fetched = defaultFood().id(FOOD_ID).user(myUser).build();
                        FoodRequest request = prepareValidRequest(fetched);

                        when(foodRepository.fetchByIdVerified(USER_ID, FOOD_ID)).thenReturn(Optional.of(fetched));

                        // Act
                        FoodResponse response = foodService.updateFood(myUser, FOOD_ID, request);

                        // Assert
                        assertThat(response).as("Method output should match mapper output.")
                                        .isEqualTo(foodMapper.toResponse(fetched));

                        assertThat(fetched).as("Updated food fields should match request fields.")
                                        .usingRecursiveComparison()
                                        .ignoringFields("id", "user")
                                        .isEqualTo(request);
                }

                @ParameterizedTest(name = "Given a request with duplicate {0} names, throws a DuplicateValueException.")
                @MethodSource("org.example.mealplannerapp.service.FoodServiceUnitTests#prepareInvalidRequests")
                @DisplayName("Given a request with duplicate unit or vendor names, throws a DuplicateValueException.")
                void duplicateUnitOrVendorNames(FoodRequest request) {
                        // Act + Assert
                        assertThatThrownBy(() -> foodService.updateFood(myUser, FOOD_ID, request))
                                        .as("Method should throw a DuplicateValueException.")
                                        .isInstanceOf(DuplicateValueException.class);
                }

                @Test
                @DisplayName("Given a non-existent or non-owned foodId, throws a ResourceNotFoundException.")
                void foodNotFound() {
                        // Arrange
                        FoodRequest request = defaultFoodRequest().build();

                        when(foodRepository.fetchByIdVerified(USER_ID, FOOD_ID)).thenReturn(Optional.empty());

                        // Act + Assert
                        assertThatThrownBy(() -> foodService.updateFood(myUser, FOOD_ID, request))
                                        .as("Method should throw a ResourceNotFoundException.")
                                        .isInstanceOf(ResourceNotFoundException.class);
                }

        }

        @Nested
        @DisplayName("deleteFood")
        class DeleteFood {

                @Test
                @DisplayName("Given an existing foodId owned by the current user, deletes the requested food.")
                void foodDeleted() {
                        // Arrange
                        when(foodRepository.deleteByIdVerified(USER_ID, FOOD_ID)).thenReturn(1);

                        // Act + Assert
                        assertThatCode(() -> foodService.deleteFood(myUser, FOOD_ID))
                                        .as("Method should not throw any exceptions.")
                                        .doesNotThrowAnyException();

                        verify(foodRepository, description("deleteByIdVerified should be called."))
                                        .deleteByIdVerified(USER_ID, FOOD_ID);
                }

                @Test
                @DisplayName("Given a non-existent or non-owned foodId, throws a ResouceNotFoundException.")
                void foodNotFound() {
                        // Arrange
                        when(foodRepository.deleteByIdVerified(USER_ID, FOOD_ID)).thenReturn(0);

                        // Assert
                        assertThatThrownBy(() -> foodService.deleteFood(myUser, FOOD_ID))
                                        .as("Method should throw a ResourceNotFoundException.")
                                        .isInstanceOf(ResourceNotFoundException.class);
                }

        }

        @Nested
        @DisplayName("retrieveFood")
        class RetrieveFood {

                @Test
                @DisplayName("Given an existing foodId owned by the current user, returns a response.")
                void foodRetrieved() {
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
                @DisplayName("Given a non-existent or non-owned foodId, throws a ResourceNotFoundException.")
                void foodNotFound() {
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
                @DisplayName("Returns a page of listed responses.")
                void matchesReturned() {
                        // Arrange
                        List<Food> foods = new ArrayList<>(List.of(
                                defaultFood().name("match1").user(myUser).build(),
                                defaultFood().name("match2").user(myUser).build(),
                                defaultFood().name("match3").user(myUser).build()));

                        Pageable pageable = PageRequest.of(0, 3);
                        Page<Food> foodsPage = new PageImpl<>(foods, pageable, 3);

                        when(foodRepository.fetchShallowByUserAndText(USER_ID, "match", pageable))
                        .thenReturn(foodsPage);

                        // Act
                        Page<ListedFoodResponse> response = foodService.searchFoods(myUser, "match", pageable);

                        // Assert
                        assertThat(response).as("Method output should match mapper output.")
                                        .isEqualTo(foodsPage.map(foodMapper::toListedResponse));


                }
        }

}