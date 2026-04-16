package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transaction")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /*
     * @Column(name = "account_id", nullable = false)
     * private Integer accountId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 31)
    private TransactionSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TransactionType type;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "net_amount", nullable = false)
    private BigDecimal netAmount;

    private BigDecimal fee;

    private BigDecimal tax;

    @Column(nullable = false, length = 3)
    private String currency = "EUR";

    @Column(name = "stripe_payment_intent_id", length = 255)
    private String stripePaymentIntentId;

    @Column(name = "idempotency_key", length = 255)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "pending_reason", length = 31)
    private TransactionPendingReason pendingReason;

    /*
     * @Column(name = "transaction_group_id", nullable = false)
     * private Integer transactionGroupId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_group_id", nullable = false)
    private TransactionGroup transactionGroup;

    /*
     * @Column(name = "related_transaction_id")
     * private Integer relatedTransactionId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_transaction_id")
    private Transaction relatedTransaction;

    @Column(nullable = false)
    private Boolean refundable = true;

    @Column(nullable = false)
    private Boolean visible = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}
