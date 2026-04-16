package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "purchase")
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /*
     * @Column(name = "account_id")
     * private Integer accountId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "object_id", nullable = false)
    private Integer objectId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 31)
    private CheckoutType type;

    @Column(columnDefinition = "jsonb")
    private String detail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private PurchaseStatus status = PurchaseStatus.CONFIRMED;

    /*
     * @Column(name = "transaction_group_id", nullable = false)
     * private Integer transactionGroupId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_group_id", nullable = false)
    private TransactionGroup transactionGroup;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
