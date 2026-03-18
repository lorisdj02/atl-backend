package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "purchase")
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "account_id")
    private Integer accountId;

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

    @Column(name = "transaction_group_id", nullable = false)
    private Integer transactionGroupId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
