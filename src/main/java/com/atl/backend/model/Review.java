package com.atl.backend.model;

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

    @Column(name = "reviewer_user_id", nullable = false)
    private Integer reviewerUserId;

    @Column(name = "reviewed_user_id", nullable = false)
    private Integer reviewedUserId;

    @Column(length = 255)
    private String comment;

    @Column(nullable = false)
    private BigDecimal rating;

    @Column(nullable = false)
    private BigDecimal weight = new BigDecimal("0.5");

    @Column(name = "review_type", nullable = false, length = 31)
    private String reviewType;

    @Column(nullable = false, length = 15)
    private String status = "WAITING";

    @Column(name = "sport_category", nullable = false, length = 15)
    private String sportCategory;

    @Column(name = "linked_transaction_id")
    private Integer linkedTransactionId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "committed_at")
    private LocalDateTime committedAt;
}
