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
    private Long id;

    /*
     * @Column(name = "tournament_id", nullable = false)
     * private Long tournamentId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private Match match;

    /*
     * @Column(name = "reviewer_account_id", nullable = false)
     * private Long reviewerAccountId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_account_id", nullable = false)
    private Account reviewerAccount;

    /*
     * @Column(name = "reviewed_account_id", nullable = false)
     * private Long reviewedAccountId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_account_id", nullable = false)
    private Account reviewedAccount;

    @Column(length = 255)
    private String comment;

    @Column(precision = 3, scale = 2)
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

    /*
     * @Column(name = "linked_transaction_id")
     * private Long linkedTransactionId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_transaction_id")
    private Transaction linkedTransaction;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "committed_at")
    private LocalDateTime committedAt;
}
