package com.atl.backend.repository;

import com.atl.backend.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByField_Id(Long fieldId);

    List<Booking> findByField_City_IdAndStartTimeGreaterThanEqualAndFinishTimeLessThanEqual(Long cityId, Instant startTime, Instant finishTime);

    List<Booking> findByField_City_Id(Long cityId);

    List<Booking> findByField_IdAndStartTimeGreaterThanEqual(Long fieldId, Instant startTime);
}