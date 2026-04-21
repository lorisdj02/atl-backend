package com.atl.backend.repository;

import com.atl.backend.model.RefereeLicense;
import com.atl.backend.model.RefereeLicenseId;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefereeLicenseRepository extends JpaRepository<RefereeLicense, RefereeLicenseId> {
    List<RefereeLicense> findByReferee_RefereeId(Long refereeId);

    Optional<RefereeLicense> findByReferee_RefereeIdAndSport_Id(Long refereeId, Long sportId);
}
