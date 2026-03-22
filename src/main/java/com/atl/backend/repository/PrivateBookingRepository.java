package com.atl.backend.repository;

import com.atl.backend.model.PrivateBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface PrivateBookingRepository extends JpaRepository<PrivateBooking, Integer> {

    List<PrivateBooking> findByPlayer_PlayerId(Integer playerId);
    
    List<PrivateBooking> findByBooking_Field_Id(Integer fieldId);
}