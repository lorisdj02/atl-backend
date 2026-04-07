package com.atl.backend.repository;

import com.atl.backend.model.Transaction;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    List<Transaction> findByAccount_Id(Integer accountId);

    List<Transaction> findByTransactionGroup_Id(Integer transactionGroupId);
}