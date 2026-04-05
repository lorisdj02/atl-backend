package com.atl.backend.repository;

import com.atl.backend.model.Referee;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RefereeRepository extends JpaRepository<Referee, Integer> {

    Optional<Referee> findByAccount_Id(Integer accountId);

    Optional<Referee> findByRefereeId(Integer refereeId);

    void deleteByAccount_Id(Integer accountId);

    boolean existsByAccount_Id(Integer accountId);

    boolean existsByRefereeId(Integer refereeId);

    @Query("SELECT r.level FROM Referee r WHERE r.level IS NOT NULL ORDER BY r.level ASC")
    List<Short> findAllLevelsOrdered();
}