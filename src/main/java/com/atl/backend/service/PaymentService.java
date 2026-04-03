package com.atl.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atl.backend.model.Transaction;
import com.atl.backend.model.Wallet;
import com.atl.backend.repository.TransactionRepository;
import com.atl.backend.repository.WalletRepository;

@Service
public class PaymentService {
    @Autowired
    private AccountService accountService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public List<Transaction> getTransactionsByAccountId(Integer accountId) {
        return transactionRepository.findByAccount_Id(accountId);
    }

    public Optional<Wallet> getWalletByAccountId(Integer accountId) {
        return walletRepository.findByAccount_Id(accountId);
    }
}
