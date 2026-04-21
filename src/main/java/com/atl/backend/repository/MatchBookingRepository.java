package com.atl.backend.repository;

import java.util.List;
import java.util.Optional;

import com.atl.backend.model.MatchBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchBookingRepository extends JpaRepository<MatchBooking, Long> {

    List<MatchBooking> findByBooking_Field_Id(Long fieldId);

    Optional<MatchBooking> findByMatch_Id(Long matchId);
}