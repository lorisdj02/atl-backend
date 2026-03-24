package com.atl.backend.repository;

import com.atl.backend.model.Coordinator;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoordinatorRepository extends JpaRepository<Coordinator, Integer> {

    Optional<Coordinator> findByAccount_Id(Integer accountId);

    Optional<Coordinator> findByCoordinatorId(Integer coordinatorId);

    void deleteByAccount_Id(Integer accountId);

    boolean existsByAccount_Id(Integer accountId);

    boolean existsByCoordinatorId(Integer coordinatorId);
}