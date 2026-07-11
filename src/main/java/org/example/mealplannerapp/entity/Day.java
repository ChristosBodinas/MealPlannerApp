package org.example.mealplannerapp.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.mealplannerapp.entity.entry.Entry;

import java.util.Map;
import java.util.Set;

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