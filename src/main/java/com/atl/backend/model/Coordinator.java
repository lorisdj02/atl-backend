package com.atl.backend.model;

import com.atl.backend.model.enums.*;

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

    @Column(name = "account_id", unique = true, nullable = false)
    private Integer accountId;

    @Column(nullable = false)
    private Short level = 1;
}
