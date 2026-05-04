package com.atl.backend.model;

import java.time.Instant;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "city")
public class City {
    @Id
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;
}
