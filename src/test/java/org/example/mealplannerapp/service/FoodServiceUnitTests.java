package org.example.mealplannerapp.service;

import org.example.mealplannerapp.dto.food.request.PriceRequest;
import org.example.mealplannerapp.dto.food.request.FoodRequest;
import org.example.mealplannerapp.dto.food.request.UnitRequest;
import org.example.mealplannerapp.dto.food.response.PriceResponse;
import org.example.mealplannerapp.dto.food.response.FoodResponse;
import org.example.mealplannerapp.dto.food.response.UnitResponse;
import org.example.mealplannerapp.mapper.FoodMapper;
import org.example.mealplannerapp.repository.FoodRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class FoodServiceUnitTests {

    @Mock private FoodRepository foodRepository;
    @Mock private FoodMapper foodMapper;

    @InjectMocks private FoodService foodService;

    private FoodRequest defaultFoodRequest() {
        return new FoodRequest(
                "Fake Food", "Fake Brand",
                97.0, 12.0, 37.5, 4.5, 6.0,
                0.9,
                Set.of(new UnitRequest("tbsp", 15.0),
                        new UnitRequest("cup", 235.0)),
                Set.of(new PriceRequest("Masoutis", 6.80, 200),
                        new PriceRequest("MyMarket", 5.70, 175)));
    }

    private FoodRequest dupUnitFoodRequest() {
        return new FoodRequest(
                "Fake Food", "Fake Brand",
                97.0, 12.0, 37.5, 4.5, 6.0,
                0.9,
                Set.of(new UnitRequest("tbsp", 15.0),
                        new UnitRequest("tbsp", 235.0)),
                Set.of(new PriceRequest("Masoutis", 6.80, 200),
                        new PriceRequest("MyMarket", 5.70, 175)));
    }

    private FoodRequest dupPriceFoodRequest() {
        return new FoodRequest(
                "Fake Food", "Fake Brand",
                97.0, 12.0, 37.5, 4.5, 6.0,
                0.9,
                Set.of(new UnitRequest("tbsp", 15.0),
                        new UnitRequest("cup", 235.0)),
                Set.of(new PriceRequest("Masoutis", 6.80, 200),
                        new PriceRequest("Masoutis", 5.70, 175)));
    }

    private FoodResponse defaultFoodResponse() {
        return new FoodResponse(
                99L, "Fake Food", "Fake Brand",
                97.0, 12.0, 37.5, 4.5, 6.0,
                0.9,
                Set.of(new UnitResponse("tbsp", 15.0),
                        new UnitResponse("cup", 235.0)),
                Set.of(new PriceResponse("Masoutis", 6.80, 200),
                        new PriceResponse("MyMarket", 5.70, 175)));
    }

    private FoodResponse alternateFoodResponse() {
        return new FoodResponse(
                99L, "Mock Meal", "Fake Brand",
                97.0, 12.0, 37.5, 4.5, 6.0,
                0.9,
                Set.of(new UnitResponse("tbsp", 16.0),
                        new UnitResponse("cup", 235.0)),
                Set.of(new PriceResponse("Masoutis", 6.80, 200),
                        new PriceResponse("MyMarket", 5.80, 175)));
    }

    // retrieveFoodEntity tests: happy flow, exception?

    // createFood tests: happy flow, duplicate exception

    // updateFood tests: happy flow, duplication exception, not found exception?

    // deleteFood tests: happy flow, not found exception?

    // retrieveFood tests: happy flow, not exception exception?

    // searchFoods tests: happy flow

}