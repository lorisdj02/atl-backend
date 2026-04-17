package com.atl.backend.repository;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.atl.backend.model.WalletBalance;

@Repository
public interface WalletBalanceRepository extends JpaRepository<WalletBalance, Integer> {
    Optional<WalletBalance> findByAccount_Id(Integer accountId);

    @Query("SELECT w.prizeBalance FROM WalletBalance w WHERE w.account.id = :accountId")
    BigDecimal findPrizeBalanceByAccount_Id(Integer accountId);

    @Query("SELECT w.personalBalance FROM WalletBalance w WHERE w.account.id = :accountId")
    BigDecimal findPersonalBalanceByAccount_Id(Integer accountId);

    @Query("SELECT w.inviteBalance FROM WalletBalance w WHERE w.account.id = :accountId")
    BigDecimal findInviteBalanceByAccount_Id(Integer accountId);
}
