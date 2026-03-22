package com.atl.backend.repository;

import com.atl.backend.model.FootballMatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface FootballMatchRepository extends JpaRepository<FootballMatch, Integer> {

    List<FootballMatch> findByMatch_Tournament_Id(Integer tournamentId);

    List<FootballMatch> findByMatch_Tournament_IdAndMatch_GroupName(Integer tournamentId, String groupName);

    Optional<FootballMatch> findByMatch_Id(Integer id);
}