package com.atl.backend.repository;
import com.atl.backend.model.FinalStageBracket;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinalStageBracketRepository extends JpaRepository<FinalStageBracket, Integer>{
    List<FinalStageBracket> findByTournament_Id(Integer tournamentId);
}
