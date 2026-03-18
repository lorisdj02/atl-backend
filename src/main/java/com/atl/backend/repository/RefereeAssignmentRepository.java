package com.atl.backend.repository;

import com.atl.backend.model.RefereeAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefereeAssignmentRepository extends JpaRepository<RefereeAssignment, Integer> {
}