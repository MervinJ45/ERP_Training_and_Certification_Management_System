package com.example.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "training_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainingCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID categoryId;

    private String categoryName;

    private Boolean isActive;
}