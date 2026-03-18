package com.atl.backend.repository;

import com.atl.backend.model.PrivateBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrivateBookingRepository extends JpaRepository<PrivateBooking, Integer> {
}