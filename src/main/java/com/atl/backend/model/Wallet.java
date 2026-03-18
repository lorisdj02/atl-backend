package com.atl.backend.model;

import com.atl.backend.model.enums.*;

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

    @Column(name = "account_id", unique = true, nullable = false)
    private Integer accountId;

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
}
