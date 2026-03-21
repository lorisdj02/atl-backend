package com.atl.backend.repository;

import com.atl.backend.model.PlaysAt;
import com.atl.backend.model.PlaysAtId;
import com.atl.backend.model.enums.SportCategory;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaysAtRepository extends JpaRepository<PlaysAt, PlaysAtId> {

    Optional<PlaysAt> findByPlayer_PlayerIdAndSportCategory(Integer playerId, SportCategory sportCategory);

    Optional<PlaysAt> findByPlayer_PlayerIdAndTeam_Id(Integer playerId, Integer teamId);

    List<PlaysAt> findByTeam_Id(Integer teamId);
}
