package com.finx.dmsservice.service;

import com.finx.dmsservice.domain.dto.CreateCategoryRequest;
import com.finx.dmsservice.domain.dto.DocumentCategoryDTO;

import java.util.List;

public interface DocumentCategoryService {

    DocumentCategoryDTO createCategory(CreateCategoryRequest request);

    DocumentCategoryDTO getCategoryById(Long id);

    DocumentCategoryDTO getCategoryByCode(String categoryCode);

    List<DocumentCategoryDTO> getActiveCategories();

    List<DocumentCategoryDTO> getRootCategories();

    List<DocumentCategoryDTO> getChildCategories(Long parentCategoryId);

    DocumentCategoryDTO updateCategory(Long id, CreateCategoryRequest request);

    DocumentCategoryDTO activateCategory(Long id);

    DocumentCategoryDTO deactivateCategory(Long id);

    void deleteCategory(Long id);
}
