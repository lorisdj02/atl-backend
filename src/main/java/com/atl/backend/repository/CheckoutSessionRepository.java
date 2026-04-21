package com.atl.backend.repository;

import com.atl.backend.model.CheckoutSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckoutSessionRepository extends JpaRepository<CheckoutSession, Long> {
}