package com.atl.backend.repository;

import com.atl.backend.model.FieldCategory;
import com.atl.backend.model.FieldCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FieldCategoryRepository extends JpaRepository<FieldCategory, FieldCategoryId> {
}
