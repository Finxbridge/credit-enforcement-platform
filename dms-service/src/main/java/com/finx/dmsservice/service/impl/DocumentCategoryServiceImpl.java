package com.finx.dmsservice.service.impl;

import com.finx.dmsservice.config.CacheConstants;
import com.finx.dmsservice.domain.dto.CreateCategoryRequest;
import com.finx.dmsservice.domain.dto.DocumentCategoryDTO;
import com.finx.dmsservice.domain.entity.DocumentCategory;
import com.finx.dmsservice.exception.BusinessException;
import com.finx.dmsservice.exception.ResourceNotFoundException;
import com.finx.dmsservice.mapper.DocumentMapper;
import com.finx.dmsservice.repository.DocumentCategoryRepository;
import com.finx.dmsservice.service.DocumentCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DocumentCategoryServiceImpl implements DocumentCategoryService {

    private final DocumentCategoryRepository categoryRepository;
    private final DocumentMapper documentMapper;

    @Override
    @CacheEvict(value = {CacheConstants.CATEGORY_CACHE, CacheConstants.CATEGORY_LIST_CACHE}, allEntries = true)
    public DocumentCategoryDTO createCategory(CreateCategoryRequest request) {
        log.info("Creating document category: {}", request.getCategoryCode());

        if (categoryRepository.existsByCategoryCode(request.getCategoryCode())) {
            throw new BusinessException("Category code already exists: " + request.getCategoryCode());
        }

        DocumentCategory category = documentMapper.toEntity(request);
        category.setIsActive(true);

        DocumentCategory savedCategory = categoryRepository.save(category);
        log.info("Category created with ID: {}", savedCategory.getId());

        return enrichWithParentName(documentMapper.toCategoryDto(savedCategory));
    }

    @Override
    @Cacheable(value = CacheConstants.CATEGORY_CACHE, key = "#id")
    @Transactional(readOnly = true)
    public DocumentCategoryDTO getCategoryById(Long id) {
        DocumentCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        return enrichWithParentName(documentMapper.toCategoryDto(category));
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentCategoryDTO getCategoryByCode(String categoryCode) {
        DocumentCategory category = categoryRepository.findByCategoryCode(categoryCode)
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryCode));
        return enrichWithParentName(documentMapper.toCategoryDto(category));
    }

    @Override
    @Cacheable(value = CacheConstants.CATEGORY_LIST_CACHE, key = "'active'")
    @Transactional(readOnly = true)
    public List<DocumentCategoryDTO> getActiveCategories() {
        return categoryRepository.findAllActiveOrdered().stream()
                .map(documentMapper::toCategoryDto)
                .map(this::enrichWithParentName)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentCategoryDTO> getRootCategories() {
        return categoryRepository.findRootCategories().stream()
                .map(documentMapper::toCategoryDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentCategoryDTO> getChildCategories(Long parentCategoryId) {
        return categoryRepository.findByParentCategoryId(parentCategoryId).stream()
                .map(documentMapper::toCategoryDto)
                .toList();
    }

    @Override
    @CacheEvict(value = {CacheConstants.CATEGORY_CACHE, CacheConstants.CATEGORY_LIST_CACHE}, allEntries = true)
    public DocumentCategoryDTO updateCategory(Long id, CreateCategoryRequest request) {
        log.info("Updating category ID: {}", id);

        DocumentCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));

        category.setCategoryName(request.getCategoryName());
        category.setParentCategoryId(request.getParentCategoryId());
        category.setDescription(request.getDescription());
        category.setAllowedFileTypes(request.getAllowedFileTypes());
        category.setMaxFileSizeMb(request.getMaxFileSizeMb());
        category.setRetentionDays(request.getRetentionDays());
        category.setDisplayOrder(request.getDisplayOrder());

        return enrichWithParentName(documentMapper.toCategoryDto(categoryRepository.save(category)));
    }

    @Override
    @CacheEvict(value = {CacheConstants.CATEGORY_CACHE, CacheConstants.CATEGORY_LIST_CACHE}, allEntries = true)
    public DocumentCategoryDTO activateCategory(Long id) {
        DocumentCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        category.setIsActive(true);
        return enrichWithParentName(documentMapper.toCategoryDto(categoryRepository.save(category)));
    }

    @Override
    @CacheEvict(value = {CacheConstants.CATEGORY_CACHE, CacheConstants.CATEGORY_LIST_CACHE}, allEntries = true)
    public DocumentCategoryDTO deactivateCategory(Long id) {
        DocumentCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
        category.setIsActive(false);
        return enrichWithParentName(documentMapper.toCategoryDto(categoryRepository.save(category)));
    }

    @Override
    @CacheEvict(value = {CacheConstants.CATEGORY_CACHE, CacheConstants.CATEGORY_LIST_CACHE}, allEntries = true)
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category", id);
        }
        categoryRepository.deleteById(id);
        log.info("Category deleted: {}", id);
    }

    private DocumentCategoryDTO enrichWithParentName(DocumentCategoryDTO dto) {
        if (dto.getParentCategoryId() != null) {
            categoryRepository.findById(dto.getParentCategoryId())
                    .ifPresent(parent -> dto.setParentCategoryName(parent.getCategoryName()));
        }
        return dto;
    }
}
