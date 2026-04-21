package com.atl.backend.repository;

import java.util.List;

import com.atl.backend.model.Field;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FieldRepository extends JpaRepository<Field, Long> {
        void deleteAll();

        List<Field> findByOrganization_OrganizationId(Long organizationId);

        List<Field> findByCity_Id(Long cityId);
}