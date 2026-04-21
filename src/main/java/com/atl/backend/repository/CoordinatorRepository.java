package com.atl.backend.repository;

import com.atl.backend.model.Coordinator;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoordinatorRepository extends JpaRepository<Coordinator, Long> {

    Optional<Coordinator> findByAccount_Id(Long accountId);

    Optional<Coordinator> findByCoordinatorId(Long coordinatorId);

    void deleteByAccount_Id(Long accountId);

    boolean existsByAccount_Id(Long accountId);

    boolean existsByCoordinatorId(Long coordinatorId);
}