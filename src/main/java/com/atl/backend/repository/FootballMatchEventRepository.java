package com.atl.backend.repository;

import com.atl.backend.model.FootballMatchEvent;
import com.atl.backend.model.enums.FootballEventType;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FootballMatchEventRepository extends JpaRepository<FootballMatchEvent, Long> {
    @Query("SELECT e FROM FootballMatchEvent e WHERE e.match.match.id = :matchId ORDER BY e.minute, e.additionalMinute")
    List<FootballMatchEvent> findByMatch_Match_Id(Long matchId);

    Boolean existsByMatch_IdAndPlayer_PlayerIdAndType(Long matchId, Long playerId, FootballEventType type);
}