package org.example.mealplannerapp.entity;

import java.util.ArrayList;
import java.util.List;

import org.example.mealplannerapp.constants.Sex;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "plan", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @OrderColumn(name = "day_index")
    private List<Day> days;

    @Column(nullable = false, length = 45)
    private String name;

    @Column(nullable = false)
    private double startWeight;

    // ActivityLevel

    @Column(nullable = false)
    private double desiredChange;

    @Column(nullable = false)
    private double proteinRatio;

    @Column(nullable = false)
    private double carbsRatio;

    public double deriveTDEE() {
        // Basal Metabolic Rate
        double bmr = 10 * startWeight + 6.25 * user.getHeight() - 5 * user.deriveAgeInYears();
        bmr += (user.getSex() == Sex.MALE) ? 5 : -161;

        // TO DO: Implemented proper activity levels. Currently hardcoded to sedentary (1.2)
        double activityFactor = 1.2;

        return bmr * activityFactor;
    }

    public void distributeDailyGoals() {
        // NOTE: verification checks will occur in the service method
        int numberOfDays = days.size();

        // TO DO: Fix protein/carbs/fat calculations
        double dailyCalories = deriveTDEE() / numberOfDays;
        double dailyProtein = dailyCalories * proteinRatio;
        double dailyCarbs = dailyCalories * carbsRatio;
        double dailyFat = dailyCalories * (1.0 - proteinRatio - carbsRatio);
        double dailyFiber = (user.getSex() == Sex.MALE) ? 30.0 : 25.0;

        for (Day day : days) {
            day.setCaloriesGoal(dailyCalories);
            day.setProteinGoal(dailyProtein);
            day.setCarbsGoal(dailyCarbs);
            day.setFatGoal(dailyFat);
            day.setFiberGoal(dailyFiber);
        }
    }

    public void initializeDays(int numberOfDays) {
        days = new ArrayList<>();

        for (int i = 0; i < numberOfDays; i++) {
            Day day = new Day();
            day.setPlan(this);
            days.add(day);
        }

        distributeDailyGoals();
    }
}
