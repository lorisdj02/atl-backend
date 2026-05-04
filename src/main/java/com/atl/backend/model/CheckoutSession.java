package com.atl.backend.model;

import com.atl.backend.dto.CheckoutPayload;
import com.atl.backend.model.enums.*;
import com.atl.backend.util.CheckoutPayloadConverter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Entity
@Table(name = "checkout_session")
public class CheckoutSession {
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 31)
    private CheckoutType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private CheckoutStatus status = CheckoutStatus.PENDING;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    @Convert(converter = CheckoutPayloadConverter.class)
    private CheckoutPayload payload;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt = Instant.now().plusSeconds(600);
}
