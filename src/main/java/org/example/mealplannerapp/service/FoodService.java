package org.example.mealplannerapp.service;

import lombok.AllArgsConstructor;
import org.example.mealplannerapp.dto.food.request.FoodPriceRequest;
import org.example.mealplannerapp.dto.food.request.FoodRequest;
import org.example.mealplannerapp.dto.food.request.FoodUnitRequest;
import org.example.mealplannerapp.dto.food.response.FoodResponse;
import org.example.mealplannerapp.dto.food.response.ListedFoodResponse;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.exception.IllegalDuplicateValueException;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.mapper.FoodMapper;
import org.example.mealplannerapp.repository.FoodRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * <p>A service that handles the creation, modification, deletion, and retrieval of
 * {@code Food} entities.
 * </p>
 */
@Service
@AllArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;
    private final FoodMapper foodMapper;

    /**
     * <p>Verifies that the submitted {@code units} and {@code prices} have unique unit names and merchants, respectively.
     * </p>
     * @param units - A set of submitted reference unit data.
     * @param prices - A set of submitted pricing data.
     * @throws {@code IllegalDuplicateValueException} if there are multiple {@code units} with the same name or multiple
     * {@code prices} with the same merchant.
     */
    private void verifyUniqueUnitsAndPrices(Set<FoodUnitRequest> units, Set<FoodPriceRequest> prices) {
        if (units != null
                && units.size() > units.stream().map(FoodUnitRequest::name).distinct().count()) {
            throw new IllegalDuplicateValueException("A food can't have duplicates of the same unit.");
        }

        if (prices != null
                && prices.size() > prices.stream().map(FoodPriceRequest::merchant).distinct().count()) {
            throw new IllegalDuplicateValueException("A food can't have duplicates of the same merchant.");
        }
    }

    /**
     * <p>Fetches the {@code Food} entity identified by {@code foodId}, but only if it is actually owned
     * by the requesting user. The associated {@code FoodUnit} and {@code FoodPrice} collections are also
     * fetched.
     * </p>
     * @param userId - the identifier of the requesting user
     * @param foodId - the identifier of the requested food
     * @return the requested {@code Food} entity
     * @throws {@code ResourceNotFoundException}, if the requested food isn't found.
     */
    public Food retrieveFoodEntity(Long userId, Long foodId) {
        return foodRepository.findByIdVerified(userId, foodId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Requested food (id: " + foodId + ") was not found.")
            );
    }

    /**
     * <p>Creates and saves a new {@code Food} entity owned by {@code user} using the submitted
     * {@code request} data.
     * </p>
     * @param user - the requesting user
     * @param request - the submitted food data
     * @return a {@code FoodResponse} containing the new {@code Food}'s full data
     */
    public FoodResponse createFood(User user, FoodRequest request) {
        verifyUniqueUnitsAndPrices(request.units(), request.prices());

        Food food = foodMapper.createFromRequest(request);
        food.setUser(user);

        Food saved = foodRepository.save(food);
        return foodMapper.generateResponse(saved);
    }

    /**
     * <p> Overwrites the data of the {@code Food} entity identified by {@code foodId} with the submitted
     * {@code request} data. Changes are only accepted if the requested food is actually owned by {@code user}.
     * </p>
     * @param user - the requesting user
     * @param foodId - the identifier of the requested food
     * @param request - the submitted food data
     * @return a {@code FoodResponse} containing the updated {@code Food}'s full data
     */
    @Transactional
    public FoodResponse updateFood(User user, Long foodId, FoodRequest request) {
        verifyUniqueUnitsAndPrices(request.units(), request.prices());

        Food food = retrieveFoodEntity(user.getId(), foodId);
        foodMapper.updateFromRequest(food, request);

        return foodMapper.generateResponse(food);
    }

    /**
     * <p>Deletes the {@code Food} entity identified by {@code foodId}, but only if it is actually
     * owned by {@code user}.
     * </p>
     * @param user - the requesting user
     * @param foodId - the identifier of the requested food
     */
    @Transactional
    public void deleteFood(User user, Long foodId) {
        // TO DO: Make this throw an exception when the requested food is not found!
        // NOTE: The associated FoodPrice and FoodUnit collections are deleted automatically.
        foodRepository.deleteByIdVerified(user.getId(), foodId);
    }

    /**
     * <p>Retrieves the {@code Food} entity identified by {@code foodId}, but only if it is actually
     * owned by {@code user}.
     * </p>
     * @param user - the requesting user
     * @param foodId - the identifier of the requested food
     * @return a {@code FoodResponse} containing the retrieved {@code Food}'s full data
     */
    public FoodResponse retrieveFood(User user, Long foodId) {
        Food food = retrieveFoodEntity(user.getId(), foodId);
        return foodMapper.generateResponse(food);
    }

    /**
     * <p>Retrieves all {@code Food} entities that are owned by {@code user} and whose name or brand
     * contain {@code search}. If {@code search} is an empty String, the method simply retrieves
     * all the {@code Food} entities owned by {@code user}.
     * @param user - the requesting user
     * @param search - the submitted search text
     * @return a {@code List} of {@code ListedFoodResponse} items containing the retrieved foods'
     * identifiers, names, brands, and nutritional data.
     */
    public List<ListedFoodResponse> searchFoods(User user, String search) {
        return foodRepository.searchByText(user.getId(), search)
            .stream()
            .map(foodMapper::generateListedResponse)
            .toList();
    }
}