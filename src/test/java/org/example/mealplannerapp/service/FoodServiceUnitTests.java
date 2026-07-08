package org.example.mealplannerapp.service;

import org.example.mealplannerapp.dto.food.request.FoodPriceRequest;
import org.example.mealplannerapp.dto.food.request.FoodRequest;
import org.example.mealplannerapp.dto.food.request.FoodUnitRequest;
import org.example.mealplannerapp.dto.food.response.FoodPriceResponse;
import org.example.mealplannerapp.dto.food.response.FoodResponse;
import org.example.mealplannerapp.dto.food.response.FoodUnitResponse;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.exception.IllegalDuplicateValueException;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.mapper.FoodMapper;
import org.example.mealplannerapp.repository.FoodRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

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
                Set.of(new FoodUnitRequest("tbsp", 15.0),
                        new FoodUnitRequest("cup", 235.0)),
                Set.of(new FoodPriceRequest("Masoutis", 6.80, 200),
                        new FoodPriceRequest("MyMarket", 5.70, 175)));
    }

    private FoodRequest dupUnitFoodRequest() {
        return new FoodRequest(
                "Fake Food", "Fake Brand",
                97.0, 12.0, 37.5, 4.5, 6.0,
                0.9,
                Set.of(new FoodUnitRequest("tbsp", 15.0),
                        new FoodUnitRequest("tbsp", 235.0)),
                Set.of(new FoodPriceRequest("Masoutis", 6.80, 200),
                        new FoodPriceRequest("MyMarket", 5.70, 175)));
    }

    private FoodRequest dupPriceFoodRequest() {
        return new FoodRequest(
                "Fake Food", "Fake Brand",
                97.0, 12.0, 37.5, 4.5, 6.0,
                0.9,
                Set.of(new FoodUnitRequest("tbsp", 15.0),
                        new FoodUnitRequest("cup", 235.0)),
                Set.of(new FoodPriceRequest("Masoutis", 6.80, 200),
                        new FoodPriceRequest("Masoutis", 5.70, 175)));
    }

    private FoodResponse defaultFoodResponse() {
        return new FoodResponse(
                99L, "Fake Food", "Fake Brand",
                97.0, 12.0, 37.5, 4.5, 6.0,
                0.9,
                Set.of(new FoodUnitResponse("tbsp", 15.0),
                        new FoodUnitResponse("cup", 235.0)),
                Set.of(new FoodPriceResponse("Masoutis", 6.80, 200),
                        new FoodPriceResponse("MyMarket", 5.70, 175)));
    }

    private FoodResponse alternateFoodResponse() {
        return new FoodResponse(
                99L, "Mock Meal", "Fake Brand",
                97.0, 12.0, 37.5, 4.5, 6.0,
                0.9,
                Set.of(new FoodUnitResponse("tbsp", 16.0),
                        new FoodUnitResponse("cup", 235.0)),
                Set.of(new FoodPriceResponse("Masoutis", 6.80, 200),
                        new FoodPriceResponse("MyMarket", 5.80, 175)));
    }


}