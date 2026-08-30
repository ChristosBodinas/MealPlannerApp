package org.example.mealplannerapp.service;

import lombok.AllArgsConstructor;
import org.example.mealplannerapp.dto.food.request.FoodRequest;
import org.example.mealplannerapp.dto.food.request.UnitRequest;
import org.example.mealplannerapp.dto.food.request.VendorRequest;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * A service responsible for handling create, read, update, delete,
 * and text search operations on {@link Food} entities.
 */
@Service
@AllArgsConstructor
public class FoodService {

    private final FoodRepository foodRepository;
    private final FoodMapper foodMapper;

    private void throwIfDuplicateLevelOrVendorNames(FoodRequest request) {
        Set<UnitRequest> units = Optional.ofNullable(request.units())
                .orElse(Collections.emptySet());

        if (units.size() > units.stream().map(UnitRequest::name).distinct().count()) {
            throw new DuplicateValueException("Cannot have multiple reference units with the same name.");
        }

        Set<VendorRequest> vendors = Optional.ofNullable(request.vendors())
                .orElse(Collections.emptySet());

        if (vendors.size() > vendors.stream().map(VendorRequest::name).distinct().count()) {
            throw new DuplicateValueException("Cannot have multiple vendors with the same name.");
        }
    }

    /**
     * Creates a new {@link Food} using the submitted {@code request} data and owned by {@code user},
     * then saves it to the database.
     *
     * @param user    the user making the request
     * @param request the submitted food data
     * @return the full data of the new food, including its units and vendors
     * @throws DuplicateValueException if {@code request} contains multiple units or vendors with identical names
     */
    public FoodResponse createFood(
            User user, FoodRequest request
    ) {
        throwIfDuplicateLevelOrVendorNames(request);

        Food food = foodMapper.toFood(request);
        food.setUser(user);

        Food saved = foodRepository.save(food);
        return foodMapper.toResponse(saved);
    }

    /**
     * Updates the {@link Food} identified by {@code foodId} and owned by {@code user} with the
     * submitted {@code request} data.
     *
     * @param user    the user making the request
     * @param foodId  the identifier of the food to be updated
     * @param request the submitted food data
     * @return the full data of the updated food, including its units and vendors
     * @throws DuplicateValueException   if {@code request} contains multiple units or vendors with identical names
     * @throws ResourceNotFoundException if {@code foodId} does not correspond to an existing
     *                                   food owned by {@code user}
     */
    @Transactional
    public FoodResponse updateFood(
            User user, Long foodId, FoodRequest request
    ) {
        throwIfDuplicateLevelOrVendorNames(request);

        Long userId = user.getId();
        Food food = foodRepository.fetchByIdVerified(userId, foodId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Requested food (id: " + foodId + ") not found."));

        foodMapper.update(food, request);
        return foodMapper.toResponse(food);
    }

    /**
     * Deletes the {@link Food} identified by {@code foodId} and owned by {@code user}, along
     * with its associated units and vendors.
     *
     * @param user   the user making the request
     * @param foodId the identifier of the food to be deleted
     * @throws ResourceNotFoundException if {@code foodId} does not correspond to an existing
     *                                   food owned by {@code user}
     */
    public void deleteFood(
            User user, Long foodId
    ) {
        Long userId = user.getId();
        if (foodRepository.deleteByIdVerified(userId, foodId) == 0) {
            throw new ResourceNotFoundException(
                    "Requested food (id: " + foodId + ") not found.");
        }
    }

    /**
     * Retrieves the {@link Food} identified by {@code foodId} and owned by {@code user}, along
     * with its associated units and vendors.
     *
     * @param user   the user making the request
     * @param foodId the identifier of the food to be retrieved
     * @return the full data of the requested food, including its units and vendors
     * @throws ResourceNotFoundException if {@code foodId} does not correspond to an existing
     *                                   food owned by {@code user}
     */
    public FoodResponse retrieveFood(
            User user, Long foodId
    ) {
        Long userId = user.getId();
        Food food = foodRepository.fetchByIdVerified(userId, foodId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Requested food (id: " + foodId + ") not found."));

        return foodMapper.toResponse(food);
    }

    /**
     * Retrieves a page of {@link Food} entities owned by {@code user} whose names or brands contain
     * {@code searchText}.
     * If {@code searchText} is empty or {@code null}, foods are retrieved regardless of their names
     * and brands.
     * Text matching is case-insensitive.
     * <p>Results are paginated and sorted according to {@code pageable}.</p>
     *
     * @param user       the user making the request
     * @param searchText the text to match against food names/brands
     * @param pageable   the pagination and sorting parameters to apply to the results
     * @return a page containing the top-level data of the retrieved foods,
     * excluding their associated units and vendors
     */
    public Page<ListedFoodResponse> searchFoods(
            User user, String searchText, Pageable pageable
    ) {
        Long userId = user.getId();

        return foodRepository.fetchShallowByUserAndText(userId, searchText, pageable)
                .map(foodMapper::toListedResponse);
    }

}


