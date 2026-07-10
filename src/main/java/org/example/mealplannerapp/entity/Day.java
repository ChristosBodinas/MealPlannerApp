package org.example.mealplannerapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

import javax.annotation.processing.Generated;

@Entity
@Getter @Setter @NoArgsConstructor
public class Day {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @OneToMany(mappedBy="day")
    private Set<Entry> entries;
}