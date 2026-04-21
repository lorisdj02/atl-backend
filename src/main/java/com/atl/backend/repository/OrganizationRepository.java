package com.atl.backend.repository;

import com.atl.backend.model.Organization;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    Optional<Organization> findByOrganizationId(Long organizationId);

    Optional<Organization> findByAccount_Id(Long accountId);

    void deleteByAccount_Id(Long accountId);

    boolean existsByAccount_Id(Long accountId);

    boolean existsByOrganizationId(Long organizationId);

}