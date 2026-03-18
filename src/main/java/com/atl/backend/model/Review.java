package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "review")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tournament_id", nullable = false)
    private Integer tournamentId;

    @Column(name = "reviewer_account_id", nullable = false)
    private Integer reviewerAccountId;

    @Column(name = "reviewed_account_id", nullable = false)
    private Integer reviewedAccountId;

    @Column(length = 255)
    private String comment;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal rating;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal weight = new BigDecimal("0.5");

    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", nullable = false, length = 31)
    private ReviewType reviewType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ReviewStatus status = ReviewStatus.WAITING;

    @Enumerated(EnumType.STRING)
    @Column(name = "sport_category", nullable = false, length = 15)
    private SportCategory sportCategory;

    @Column(name = "linked_transaction_id")
    private Integer linkedTransactionId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "committed_at")
    private LocalDateTime committedAt;
}
