package com.atl.backend.model;

import com.atl.backend.dto.PurchaseDetail;
import com.atl.backend.model.enums.*;
import com.atl.backend.util.PurchaseDetailConverter;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Data
@Entity
@Table(name = "purchase")
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * @Column(name = "account_id")
     * private Long accountId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "object_id", nullable = false)
    private Long objectId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 31)
    private CheckoutType type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Convert(converter = PurchaseDetailConverter.class)
    private PurchaseDetail detail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private PurchaseStatus status = PurchaseStatus.CONFIRMED;

    /*
     * @Column(name = "transaction_group_id", nullable = false)
     * private Long transactionGroupId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_group_id", nullable = false)
    private TransactionGroup transactionGroup;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "redeemed_at")
    private LocalDateTime redeemedAt;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
