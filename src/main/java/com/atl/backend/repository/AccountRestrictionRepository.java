package com.atl.backend.repository;

import com.atl.backend.model.AccountRestriction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRestrictionRepository extends JpaRepository<AccountRestriction, Long> {
}
