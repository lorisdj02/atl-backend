package com.atl.backend.repository;

import com.atl.backend.model.Team;
import com.atl.backend.model.enums.SportCategory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findBySportCategoryAndVisibleTrue(SportCategory sportCategory);

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Team t WHERE t.name = :teamName AND t.sportCategory = :sportCategory")
    Boolean existsByTeamNameAndSportCategory(String teamName, SportCategory sportCategory);
}