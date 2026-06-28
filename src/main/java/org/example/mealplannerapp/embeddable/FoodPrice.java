package org.example.mealplannerapp.embeddable;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter @Setter @NoArgsConstructor
public class FoodPrice {

    private String seller;
    private double purchasePrice;
    private double purchaseGrams;
}
