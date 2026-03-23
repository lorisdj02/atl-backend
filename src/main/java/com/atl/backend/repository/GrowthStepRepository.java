package com.atl.backend.repository;

import com.atl.backend.model.GrowthStep;
import com.atl.backend.model.GrowthStepId;
import com.atl.backend.model.enums.GrowthStepType;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GrowthStepRepository extends JpaRepository<GrowthStep, GrowthStepId> {

    List<GrowthStep> findByType(GrowthStepType type);
}
