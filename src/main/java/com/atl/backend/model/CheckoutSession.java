package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "checkout_session")
public class CheckoutSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "account_id")
    private Integer accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 31)
    private CheckoutType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private CheckoutStatus status;

    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
