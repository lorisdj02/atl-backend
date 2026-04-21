package com.atl.backend.repository;

import com.atl.backend.model.FootballMatch;
import com.atl.backend.model.enums.FootballMatchStatus;
import com.atl.backend.model.enums.LegOrderType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FootballMatchRepository extends JpaRepository<FootballMatch, Long> {

    List<FootballMatch> findByMatch_Tournament_Id(Long tournamentId);

    List<FootballMatch> findByMatch_Tournament_IdAndMatch_GroupName(Long tournamentId, String groupName);

    @Query("SELECT fm FROM FootballMatch fm WHERE fm.match.tournament.id = :tournamentId AND fm.match.groupName LIKE CONCAT(:groupNamePrefix, '%') AND fm.match.legOrder = :legOrder")
    List<FootballMatch> findByMatch_Tournament_IdAndGroupPrefixAndLegOrder(Long tournamentId, String groupNamePrefix,
            LegOrderType legOrder);

    List<FootballMatch> findByMatch_FinalStageBracket_Id(Long finalStageBracketId);

    Optional<FootballMatch> findByMatch_Id(Long id);

    List<FootballMatch> findByStatus(FootballMatchStatus status);
}