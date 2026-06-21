package org.example.mealplannerapp.service;

import lombok.AllArgsConstructor;
import org.example.mealplannerapp.dto.FoodRequest;
import org.example.mealplannerapp.dto.FoodResponse;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.exception.ResourceNotOwnedException;
import org.example.mealplannerapp.mapper.FoodMapper;
import org.example.mealplannerapp.repository.FoodRepository;
import org.springframework.stereotype.Service;

import java.util.List;

// TO-DO: implement PATCH methods for more specific updates?
@Service
@AllArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;
    private final FoodMapper foodMapper;

    private Food tryFindById(Long id) {
        return foodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food with id " + id + " not found."));
    }

    private void verifyOwnership(Long userId, Food food) {
        if (!food.getUser().getId().equals(userId)) {
            throw new ResourceNotOwnedException("Food with id " + food.getId() + " belongs to another user.");
        }
    }

    private double calculateNewPrice(FoodRequest request) {
        // purchaseWeight and edibleRatio cannot be 0 currently 
        return request.purchasePrice() / (request.purchaseWeight() * request.edibleRatio());
    }

    public FoodResponse createNewFood(User user, FoodRequest request) {
        Food food = foodMapper.createFromRequest(request);
        food.setUser(user);
        food.setPrice100g(calculateNewPrice(request));
        return foodMapper.generateResponse(foodRepository.save(food));
    }

    public FoodResponse updateExistingFood(User user, Long id, FoodRequest request) {
        Food food = tryFindById(id);
        verifyOwnership(user.getId(), food);

        foodMapper.updateFromRequest(food, request);
        food.setPrice100g(calculateNewPrice(request));
        return foodMapper.generateResponse(foodRepository.save(food));
    }

    public FoodResponse retrieveFoodById(User user, Long id) {
        Food food = tryFindById(id);
        verifyOwnership(user.getId(), food);
        return foodMapper.generateResponse(food);
    }

    // TO-DO: pagination?
    public List<FoodResponse> retrieveFoodsByTextSearch(User user, String text) {
        return foodRepository.findAllByUserAndTextSearch(user.getId(), text)
                .stream()
                .map(foodMapper::generateResponse)
                .toList();
    }

    // TO-DO: pagination?
    public List<FoodResponse> retrieveAllFoods(User user) {
        return foodRepository.findAllByUserId(user.getId())
                .stream()
                .map(foodMapper::generateResponse)
                .toList();
    }

    public void deleteFoodById(User user, Long id) {
        Food food = tryFindById(id);
        verifyOwnership(user.getId(), food);
        foodRepository.delete(food);
    }
}
