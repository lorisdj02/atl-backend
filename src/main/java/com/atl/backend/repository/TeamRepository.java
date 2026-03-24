package com.atl.backend.repository;

import com.atl.backend.model.Team;
import com.atl.backend.model.enums.SportCategory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Integer> {

    List<Team> findBySportCategoryAndVisibleTrue(SportCategory sportCategory);
}