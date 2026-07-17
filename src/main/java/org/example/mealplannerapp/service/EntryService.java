package org.example.mealplannerapp.service;

import lombok.AllArgsConstructor;
import org.example.mealplannerapp.dto.entry.request.EntryDuplicateRequest;
import org.example.mealplannerapp.dto.entry.request.EntryMoveRequest;
import org.example.mealplannerapp.dto.entry.request.create.EntryCreateRequest;
import org.example.mealplannerapp.dto.entry.request.create.FoodEntryCreateRequest;
import org.example.mealplannerapp.dto.entry.request.edit.EntryEditRequest;
import org.example.mealplannerapp.dto.entry.response.EntryResponse;
import org.example.mealplannerapp.entity.Day;
import org.example.mealplannerapp.entity.Food;
import org.example.mealplannerapp.entity.User;
import org.example.mealplannerapp.entity.entry.Entry;
import org.example.mealplannerapp.entity.entry.FoodEntry;
import org.example.mealplannerapp.exception.ResourceNotFoundException;
import org.example.mealplannerapp.mapper.EntryMapper;
import org.example.mealplannerapp.projection.PositionData;
import org.example.mealplannerapp.repository.DayRepository;
import org.example.mealplannerapp.repository.EntryRepository;
import org.example.mealplannerapp.repository.FoodRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>A service that handles the creation, duplication, modification, deletion,
 * and retrieval of individual {@link Entry} entities.
 * </p>
 */
@Service
@AllArgsConstructor
public class EntryService {

    private final EntryRepository entryRepository;
    private final EntryMapper entryMapper;

    private final DayRepository dayRepository;
    private final FoodRepository foodRepository;

    /**
     * <p>Creates a new {@link Entry} using the {@code request} data and places it in {@code day}.
     * Only completes if {@code day} and the requested {@link Food} are owned by {@code user}.
     * </p>
     *
     * @param user    the requesting user
     * @param dayId   the requested day
     * @param request the submitted entry data
     * @return the full data of the new {@link Entry} along with all the underlying Food/Meal data
     * @throws ResourceNotFoundException if the requested day or food is not found
     */
    @Transactional
    public EntryResponse createEntry(User user, Long dayId, EntryCreateRequest request) {
        Entry entry = entryMapper.createFromRequest(request);

        Day day = dayRepository.findByIdVerified(user.getId(), dayId)
                .orElseThrow(() -> new ResourceNotFoundException("Requested day (id: " + dayId + ") not found."));
        entry.setDay(day);

        long count = entryRepository.countInDayAndCategory(dayId, entry.getCategory());
        entry.setPosition(((int) count) + 1);

        switch (request) {
            case FoodEntryCreateRequest fRequest:
                Food food = foodRepository.findByIdVerified(user.getId(), fRequest.foodId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Requested food (id: " + fRequest.foodId() + ") not found."));
                ((FoodEntry) entry).setFood(food);
                break;
        }

        entry.snapshotNutritionAndPriceInfo();

        Entry saved = entryRepository.save(entry);
        return entryMapper.generateResponse(saved);
    }

    /**
     * <p> Creates a new {@link Entry} using data from the existing {@link Entry} identified in {@code request},
     * then places it in the requested day and in the category contained in {@code request}. The new {@link Entry} is
     * always placed at the highest (bottom) position for the day and category.
     * </p>
     *
     * @param user    the requesting user
     * @param dayId   the requested day's identifier
     * @param request the submitted entry identifier and target category
     * @return the full data of the new {@link Entry} along with all the underlying Food/Meal data
     * @throws ResourceNotFoundException if either the requested day or entry is not found
     */
    @Transactional
    public EntryResponse duplicateEntry(User user, Long dayId, EntryDuplicateRequest request) {
        Entry entry = entryRepository.findByIdVerified(user.getId(), request.entryId())
                .orElseThrow(() -> new ResourceNotFoundException("Requested entry (id: " + request.entryId() + ") not found."));

        Day day = dayRepository.findByIdVerified(user.getId(), dayId)
                .orElseThrow(() -> new ResourceNotFoundException("Requested day (id: " + dayId + ") not found."));

        Entry copy = entry.createDuplicate();
        copy.setDay(day);
        copy.setCategory(request.category());

        long count = entryRepository.countInDayAndCategory(dayId, request.category());
        copy.setPosition(((int) count) + 1);

        Entry saved = entryRepository.save(copy);
        return entryMapper.generateResponse(saved);
    }

    /**
     * <p>Updates the quantity data for the {@link Entry} identified by {@code entryId} and owned
     * by {@code user}. In the case of a {@link FoodEntry}, can also update the selected reference
     * unit and merchant.
     * </p>
     *
     * @param user    the requesting user
     * @param entryId the requested entry's identifier
     * @param request the submitted entry update data
     * @return the full data of the updated {@link Entry} along with the underlying Food/Meal data
     * @throws ResourceNotFoundException if the requested entry is not found
     */
    @Transactional
    public EntryResponse editEntry(User user, Long entryId, EntryEditRequest request) {
        Entry entry = entryRepository.findByIdVerified(user.getId(), entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Requested entry (id:" + entryId + ") not found."));

        entryMapper.updateFromRequest(entry, request);
        entry.snapshotNutritionAndPriceInfo();  // Recalculate snapshot fields to account for the change.

        return entryMapper.generateResponse(entry);
    }

    /**
     * <p>Updates category and position of the requested {@link Entry} within the same day. Also updates the positions
     * of the entries in the new and former categories to account for the movement. If the submitted category and
     * position are the same as the current ones, no changes are made.
     * </p>
     *
     * @param user    the requesting user
     * @param dayId   the requested day's identifier
     * @param entryId the requested entry's identifier
     * @param request the submitted target category and target position data
     * @throws ResourceNotFoundException if the requested entry is not found
     */
    @Transactional
    public void moveEntry(User user, Long dayId, Long entryId, EntryMoveRequest request) {
        Entry entry = entryRepository.findShallowByIdAndDayVerified(user.getId(), dayId, entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Requested entry (id: " + entryId + ") not found."));

        int sourcePosition = entry.getPosition();
        int categoryCount = (int) entryRepository.countInDayAndCategory(dayId, request.category());
        int targetPosition;

        if (entry.getCategory() == request.category()) {
            targetPosition = Math.min(request.desiredPosition(), categoryCount);

            if (sourcePosition > targetPosition) {
                entryRepository.shiftUpInDayAndCategory(dayId, request.category(), targetPosition, sourcePosition);
            } else if (sourcePosition < targetPosition) {
                entryRepository.shiftDownInDayAndCategory(dayId, request.category(), sourcePosition, targetPosition);
            }
        } else {
            targetPosition = Math.min(request.desiredPosition(), categoryCount + 1);

            entryRepository.shiftUpInDayAndCategory(dayId, request.category(), null, targetPosition);
            entryRepository.shiftDownInDayAndCategory(dayId, entry.getCategory(), sourcePosition, null);

            entry.setCategory(request.category());
        }

        entry.setPosition(targetPosition);
    }

    /**
     * <p>Deletes the {@link Entry} identified by {@code entryId} and owned by {@code user}, then repositions the
     * remaining entries in the same day and category to close the ensuing gap.
     * </p>
     *
     * @param user    the requesting user
     * @param entryId the requested entry's identifier
     * @throws ResourceNotFoundException if the requested entry is not found
     */
    @Transactional
    public void deleteEntry(User user, Long entryId) {
        PositionData positionData = entryRepository.findPositionDataByIdVerified(user.getId(), entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Requested entry (id: " + entryId + ") not found."));

        // Check only exists to catch race conditions. Might remove later.
        entryRepository.deleteByIdVerified(user.getId(), entryId);

        entryRepository.shiftDownInDayAndCategory(
                positionData.getDayId(), positionData.getCategory(), positionData.getPosition(), null
        );
    }

    /**
     * <p></p>Retrieves the full data of the {@link Entry} identified by {@code entryId} and owned by {@code user}, along
     * with the full data of the underlying Food/Meal.
     *
     * @param user    the requesting user
     * @param entryId the requested entry's identifier
     * @return the full data of the requested {@link Entry} as well as the underlying Food/Meal
     * @throws ResourceNotFoundException if the requested entry is not found
     */
    public EntryResponse retrieveEntry(User user, Long entryId) {
        Entry entry = entryRepository.findByIdVerified(user.getId(), entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Requested entry (id: " + entryId + ") not found."));
        return entryMapper.generateResponse(entry);
    }
}