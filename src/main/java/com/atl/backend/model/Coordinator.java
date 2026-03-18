package com.atl.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "coordinator")
public class Coordinator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coordinator_id")
    private Integer coordinatorId;

    @Column(name = "user_id", unique = true, nullable = false)
    private Integer userId;

    @Column(nullable = false)
    private Short level = 1;
}
