package com.atl.backend.repository;

import java.util.List;

import com.atl.backend.model.MatchBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchBookingRepository extends JpaRepository<MatchBooking, Integer> {

    List<MatchBooking> findByBooking_Field_Id(Integer fieldId);
}