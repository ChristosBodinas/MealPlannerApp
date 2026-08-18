package org.example.mealplannerapp.service;

import org.example.mealplannerapp.dto.food.request.FoodRequest;
import org.example.mealplannerapp.dto.food.request.PriceRequest;
import org.example.mealplannerapp.dto.food.request.UnitRequest;
import org.example.mealplannerapp.dto.food.response.FoodResponse;
import org.example.mealplannerapp.dto.food.response.ListedFoodResponse;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.exception.DuplicateValueException;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.mapper.FoodMapper;
import org.example.mealplannerapp.repository.FoodRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;
    private final FoodMapper foodMapper;

    // TODO: Can this be improved?
    private void verifyUniqueUnitsAndVendors(FoodRequest request) {
        if (request.units() != null && request.units().size() > 
        request.units().stream().map(UnitRequest::name).distinct().count()) {
            throw new DuplicateValueException("text");   // TODO: edit text
        }

        if (request.prices() != null && request.prices().size() >
        request.prices().stream().map(PriceRequest::vendorName).distinct().count()) {
            throw new DuplicateValueException("text");
        }
    }

    public FoodResponse createFood(
        User user, FoodRequest request
    ) {
        verifyUniqueUnitsAndVendors(request);

        Food created = foodMapper.toFood(request);
        created.setUser(user);

        Food saved = foodRepository.save(created);
        return foodMapper.toResponse(saved);
    }

    @Transactional
    public FoodResponse updateFood(
        User user, Long foodId, FoodRequest request
    ) {
        verifyUniqueUnitsAndVendors(request);

        Long userId = user.getId();
        Food fetched = foodRepository.fetchByIdVerified(userId, foodId)
            .orElseThrow(() -> new ResourceNotFoundException("Requested food (id: " + foodId + ") not found."));
        
        foodMapper.update(fetched, request);
        return foodMapper.toResponse(fetched);
    }

    @Transactional
    public void deleteFood(
        User user, Long foodId
    ) {
        Long userId = user.getId();
        if (foodRepository.deleteByIdVerified(userId, foodId) == 0) {
            throw new ResourceNotFoundException("Requested food (id: " + foodId + ") not found.");
        }
    }

    public FoodResponse retrieveFood(
        User user, Long foodId
    ) {
        Long userId = user.getId();
        Food fetched = foodRepository.fetchByIdVerified(userId, foodId)
            .orElseThrow(() -> new ResourceNotFoundException("Requested food (id: " + foodId + ") not found."));

        return foodMapper.toResponse(fetched);
    }

    public Page<ListedFoodResponse> searchFoods(
        User user, String searchText, Pageable pageable
    ) {
        Long userId = user.getId();

        return foodRepository.fetchShallowByUserAndText(userId, searchText, pageable)
            .map(foodMapper::toListedResponse);
    }
    
}
