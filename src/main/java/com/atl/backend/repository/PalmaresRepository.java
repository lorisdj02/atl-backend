package com.atl.backend.repository;

import com.atl.backend.model.Palmares;
import com.atl.backend.model.PalmaresId;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PalmaresRepository extends JpaRepository<Palmares, PalmaresId> {

    List<Palmares> findByTeam_Id(Integer teamId);
}