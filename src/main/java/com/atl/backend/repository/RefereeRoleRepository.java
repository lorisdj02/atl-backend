package com.atl.backend.repository;

import com.atl.backend.model.RefereeRole;
import com.atl.backend.model.RefereeRoleId;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefereeRoleRepository extends JpaRepository<RefereeRole, RefereeRoleId> {
    
    List<RefereeRole> findBySport_Id(Long sportId);

    List<RefereeRole> findBySport_IdAndLevel(Long sportId, Short level);

    List<RefereeRole> findBySport_IdAndName(Long sportId, String name);
}
