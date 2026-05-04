package com.atl.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Data
@Entity
@Table(name = "wallet")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * @Column(name = "account_id", unique = true, nullable = false)
     * private Long accountId;
     */

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", unique = true, nullable = false)
    private Account account;

    @Column(name = "prize_last_withdraw")
    private Instant prizeLastWithdraw;

    @Column(name = "personal_last_withdraw")
    private Instant personalLastWithdraw;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;
}
