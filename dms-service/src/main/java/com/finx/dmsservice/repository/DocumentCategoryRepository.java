package com.finx.dmsservice.repository;

import com.finx.dmsservice.domain.entity.DocumentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentCategoryRepository extends JpaRepository<DocumentCategory, Long> {

    Optional<DocumentCategory> findByCategoryCode(String categoryCode);

    List<DocumentCategory> findByIsActiveTrue();

    List<DocumentCategory> findByParentCategoryId(Long parentCategoryId);

    @Query("SELECT c FROM DocumentCategory c WHERE c.parentCategoryId IS NULL AND c.isActive = true ORDER BY c.displayOrder")
    List<DocumentCategory> findRootCategories();

    @Query("SELECT c FROM DocumentCategory c WHERE c.isActive = true ORDER BY c.displayOrder")
    List<DocumentCategory> findAllActiveOrdered();

    boolean existsByCategoryCode(String categoryCode);
}
