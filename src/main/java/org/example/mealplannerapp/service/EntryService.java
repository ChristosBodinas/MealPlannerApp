package org.example.mealplannerapp.service;

import lombok.AllArgsConstructor;

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
import org.example.mealplannerapp.repository.DayRepository;
import org.example.mealplannerapp.repository.EntryRepository;
import org.example.mealplannerapp.repository.FoodRepository;
import org.springframework.stereotype.Service;

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
     * @param user the requesting user
     * @param dayId the requested day
     * @param request the submitted entry data
     * @return the full data of the new {@link Entry} along with all the underlying Food/Meal data
     * @throws ResourceNotFoundException if the requested day or food is not found
     */
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

    // duplicateEntry

    // editEntry
    public EntryResponse editEntry(User user, Long entryId, EntryEditRequest request) {
        Entry entry = entryRepository.findByIdVerified(user.getId(), entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Requested entry (id:" + entryId + ") not found."));

        entryMapper.updateFromRequest(entry, request);
        return entryMapper.generateResponse(entry);
    }

    // reorderEntry

    public void deleteEntry(User user, Long entryId) {
        if (entryRepository.deleteByIdVerified(user.getId(), entryId) == 0) {
            throw new ResourceNotFoundException("Requested entry (id: " + entryId + ") not found.");
        }
    }

    // retrieveEntry
    public EntryResponse retrieveEntry(User user, Long entryId) {
        Entry entry = entryRepository.findByIdVerified(user.getId(), entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Requested entry (id: " + entryId + ") not found."));
        return entryMapper.generateResponse(entry);
    }

    /*
    // TO DO: Category should default to Breakfast?
    public List<EntryResponse> duplicateEntries(User user, Long dayId, Category category, EntryBulkRequest request) {
        Set<Long> entryIds = request.entryIds();

        // Fetch the requested day.
        Day day = dayRepository.findByIdVerified(user.getId(), dayId)
                .orElseThrow(() -> new ResourceNotFoundException("Requested day (id: " + dayId + ") not found."));

        // Fetch the requested entries.
        List<Entry> entries = entryRepository.findMultipleByIdVerified(user.getId(), entryIds);
        if (entryIds.size() > entries.size()) {
            throw new ResourceNotFoundException("One or more of the requested entries was not found.");
        }

        // Count the number of entries already in the requested Day and Category.
        long count = entryRepository.countInDayAndCategory(dayId, category);
        int nextPosition = (int) count + 1;

        List<Entry> copies = new ArrayList<>();
        for (Entry entry : entries) {
            Entry copy = entry.createDuplicate();
            copy.setDay(day);
            copy.setCategory(category);
            copy.setPosition(nextPosition++);
            copies.add(copy);
        }

        entryRepository.saveAll(copies);
        return copies.stream().map(entryMapper::generateResponse).toList();
    }

    @Transactional
    public EntryResponse editEntry(User user, Long entryId, EntryEditRequest request) {
        Entry entry = entryRepository.findByIdVerified(user.getId(), entryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Requested entry (id: " + entryId + ") not found."));

        entryMapper.updateFromRequest(entry, request);
        return entryMapper.generateResponse(entry);
    }

    @Transactional
    public List<EntryResponse> reorderEntries(User user, EntriesReorderRequest request) {
        
        // Transform incoming reorder requests into a form that is searchable by entryId.
        Map<Long, EntryReorderRequest> requestsById = request.requests().stream().collect(Collectors.toMap(
                EntryReorderRequest::entryId,
                r -> r,
                (a, b) -> {throw new IllegalDuplicateValueException("Received multiple positions for the same entry.");}
        ));

        // Fetch the requested entries.
        List<Entry> entries = entryRepository.findMultipleByIdVerified(user.getId(), requestsById.keySet());
        if (requestsById.size() > entries.size()) {
                throw new ResourceNotFoundException("One or more of the requested entries was not found.");
        }

        // Reorder the requested entries.
        entries.forEach(entry ->
                entryMapper.repositionEntry(entry, requestsById.get(entry.getId())));

        return entries.stream().map(entryMapper::generateResponse).toList();
    }

    @Transactional
    public void deleteEntries(User user, EntryBulkRequest request) {
        Set<Long> entryIds = request.entryIds();
        Long count = entryRepository.multipleIdsExistVerified(user.getId(), entryIds);
        if (entryIds.size() > count) {
                throw new ResourceNotFoundException("One or more of the requested entries were not found.");
        }
        entryRepository.deleteMultipleByIdVerified(user.getId(), entryIds);
    }

    public EntryResponse retrieveEntry(User user, Long entryId) {
        Entry entry = entryRepository.findByIdVerified(user.getId(), entryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Requested entry (id: " + entryId + ") not found.")
                );
        return entryMapper.generateResponse(entry);
    }
     */
}