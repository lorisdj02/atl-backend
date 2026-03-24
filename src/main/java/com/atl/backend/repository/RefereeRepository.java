package com.atl.backend.repository;

import com.atl.backend.model.Referee;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefereeRepository extends JpaRepository<Referee, Integer> {

    Optional<Referee> findByAccount_Id(Integer accountId);

    Optional<Referee> findByRefereeId(Integer refereeId);

    void deleteByAccount_Id(Integer accountId);

    boolean existsByAccount_Id(Integer accountId);

    boolean existsByRefereeId(Integer refereeId);

}