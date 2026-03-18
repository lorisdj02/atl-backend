package com.atl.backend.repository;

import com.atl.backend.model.FootballMatchEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FootballMatchEventRepository extends JpaRepository<FootballMatchEvent, Integer> {
}