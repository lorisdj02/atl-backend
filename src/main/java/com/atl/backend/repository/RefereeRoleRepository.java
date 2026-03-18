package com.atl.backend.repository;

import com.atl.backend.model.RefereeRole;
import com.atl.backend.model.RefereeRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefereeRoleRepository extends JpaRepository<RefereeRole, RefereeRoleId> {
}
