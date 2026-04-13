package com.atl.backend.repository;

import java.util.List;

import com.atl.backend.model.Field;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FieldRepository extends JpaRepository<Field, Integer> {
        void deleteAll();

        List<Field> findByOrganization_OrganizationId(Integer organizationId);

        List<Field> findByCity_Id(Integer cityId);
}