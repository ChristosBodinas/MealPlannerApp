package org.example.mealplannerapp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor
@AllArgsConstructor @Builder
public class Plan {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne @JoinColumn(name = "user_id")
    private User user;
}
