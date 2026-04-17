package com.atl.backend.model;

import com.atl.backend.dto.CheckoutPayload;
import com.atl.backend.model.enums.*;
import com.atl.backend.util.CheckoutPayloadConverter;

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

    /*
     * @Column(name = "account_id")
     * private Integer accountId;
     */

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 31)
    private CheckoutType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private CheckoutStatus status = CheckoutStatus.PENDING;

    @Column(nullable = false, columnDefinition = "jsonb")
    @Convert(converter = CheckoutPayloadConverter.class)
    private CheckoutPayload payload;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(10);
}
