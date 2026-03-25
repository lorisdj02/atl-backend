package com.atl.backend.repository;

import com.atl.backend.model.Account;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    Optional<Account> findByFirebaseUid(String firebaseUid);

    Optional<Account> findByEmail(String email);

    Optional<Account> findByUsername(String username);

}