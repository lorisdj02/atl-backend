package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "account_restriction")
public class AccountRestriction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "account_id", nullable = false)
    private Integer accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 31)
    private RestrictionType type;

    @Column(name = "validFrom", nullable = false)
    private LocalDate validFrom;

    @Column(name = "validTo")
    private LocalDate validTo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
