package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "referee")
public class Referee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "referee_id")
    private Integer refereeId;

    @Column(name = "account_id", unique = true, nullable = false)
    private Integer accountId;

    @Column(nullable = false)
    private Short level = 1;
}
