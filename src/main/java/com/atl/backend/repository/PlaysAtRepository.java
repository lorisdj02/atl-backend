package com.atl.backend.repository;

import com.atl.backend.model.PlaysAt;
import com.atl.backend.model.PlaysAtId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaysAtRepository extends JpaRepository<PlaysAt, PlaysAtId> {
}
