package com.atl.backend.repository;

import com.atl.backend.model.Organization;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Integer> {

    Optional<Organization> findByOrganizationId(Integer organizationId);

    Optional<Organization> findByAccount_Id(Integer accountId);

    void deleteByAccount_Id(Integer accountId);

    boolean existsByAccount_Id(Integer accountId);

    boolean existsByOrganizationId(Integer organizationId);

}