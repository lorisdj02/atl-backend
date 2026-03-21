package com.atl.backend.repository;

import com.atl.backend.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    java.util.Optional<Account> findByFirebaseUid(String firebaseUid);
}