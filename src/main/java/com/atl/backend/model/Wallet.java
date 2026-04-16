package com.atl.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wallet")
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /*
     * @Column(name = "account_id", unique = true, nullable = false)
     * private Integer accountId;
     */

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", unique = true, nullable = false)
    private Account account;

    @Column(name = "prize_balance", nullable = false)
    private BigDecimal prizeBalance = BigDecimal.ZERO;

    @Column(name = "personal_balance", nullable = false)
    private BigDecimal personalBalance = BigDecimal.ZERO;

    @Column(name = "invite_balance", nullable = false)
    private BigDecimal inviteBalance = BigDecimal.ZERO;

    @Column(name = "prize_last_withdraw")
    private LocalDateTime prizeLastWithdraw;

    @Column(name = "personal_last_withdraw")
    private LocalDateTime personalLastWithdraw;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
