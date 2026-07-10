package org.example.mealplannerapp.service;

@Service
@AllArgsConstructor
public class EntryService {

    private final EntryRepository entryRepository;
    private final EntryMapper entryMapper;

    private final DayRepository dayRepository;
    private final FoodRepository foodRepository;

    // (POST) createEntry(User user, Long dayId, EntryCreateRequest request)

    // (POST) duplicateEntries(User user, Long dayId, List<Long> entryIds)

    // (PATCH) editSelectedEntry

    // (PATCH) reorderEntries

    // (DELETE) deleteEntries

    // (GET) retrieveEntry

}