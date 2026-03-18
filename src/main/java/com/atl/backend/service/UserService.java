package com.atl.backend.service;

import com.atl.backend.model.Account;
import com.atl.backend.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final AccountRepository accountRepository;

    public UserService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<Account> getAllUsers() {
        return accountRepository.findAll();
    }
    
    public Account getUserById(Integer id) {
        return accountRepository.findById(id).orElse(null);
    }
}
