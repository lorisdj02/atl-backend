package com.atl.backend.model;

import java.math.BigDecimal;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "wallet_balance")
@Immutable
public class WalletBalance {
    @Id
    @Column(name = "account_id", nullable = false)
    private Integer accountId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", unique = true, nullable = false)
    private Account account;

    @Column(name = "prize_balance", nullable = false)
    private BigDecimal prizeBalance;

    @Column(name = "personal_balance", nullable = false)
    private BigDecimal personalBalance;

    @Column(name = "invite_balance", nullable = false)
    private BigDecimal inviteBalance;

    public boolean hasSufficientBalance(BigDecimal amount) {
        return prizeBalance.add(personalBalance).add(inviteBalance).add(inviteBalance).compareTo(amount) >= 0;
    }

    public boolean hasSomeBalance() {
        return prizeBalance.add(personalBalance).add(inviteBalance).add(inviteBalance).compareTo(BigDecimal.ZERO) > 0;
    }
}
