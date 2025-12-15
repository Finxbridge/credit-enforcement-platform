package com.finx.dmsservice.controller;

import com.finx.dmsservice.domain.dto.CommonResponse;
import com.finx.dmsservice.domain.dto.CreateCategoryRequest;
import com.finx.dmsservice.domain.dto.DocumentCategoryDTO;
import com.finx.dmsservice.service.DocumentCategoryService;
import com.finx.dmsservice.util.ResponseWrapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/documents/categories")
@RequiredArgsConstructor
public class DocumentCategoryController {

    private final DocumentCategoryService categoryService;

    @PostMapping
    public ResponseEntity<CommonResponse<DocumentCategoryDTO>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        DocumentCategoryDTO category = categoryService.createCategory(request);
        return ResponseWrapper.created("Category created successfully", category);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<DocumentCategoryDTO>> getCategoryById(@PathVariable Long id) {
        DocumentCategoryDTO category = categoryService.getCategoryById(id);
        return ResponseWrapper.ok("Category retrieved successfully", category);
    }

    @GetMapping("/code/{categoryCode}")
    public ResponseEntity<CommonResponse<DocumentCategoryDTO>> getCategoryByCode(@PathVariable String categoryCode) {
        DocumentCategoryDTO category = categoryService.getCategoryByCode(categoryCode);
        return ResponseWrapper.ok("Category retrieved successfully", category);
    }

    @GetMapping("/active")
    public ResponseEntity<CommonResponse<List<DocumentCategoryDTO>>> getActiveCategories() {
        List<DocumentCategoryDTO> categories = categoryService.getActiveCategories();
        return ResponseWrapper.ok("Active categories retrieved successfully", categories);
    }

    @GetMapping("/root")
    public ResponseEntity<CommonResponse<List<DocumentCategoryDTO>>> getRootCategories() {
        List<DocumentCategoryDTO> categories = categoryService.getRootCategories();
        return ResponseWrapper.ok("Root categories retrieved successfully", categories);
    }

    @GetMapping("/children/{parentId}")
    public ResponseEntity<CommonResponse<List<DocumentCategoryDTO>>> getChildCategories(@PathVariable Long parentId) {
        List<DocumentCategoryDTO> categories = categoryService.getChildCategories(parentId);
        return ResponseWrapper.ok("Child categories retrieved successfully", categories);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommonResponse<DocumentCategoryDTO>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CreateCategoryRequest request) {
        DocumentCategoryDTO category = categoryService.updateCategory(id, request);
        return ResponseWrapper.ok("Category updated successfully", category);
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<CommonResponse<DocumentCategoryDTO>> activateCategory(@PathVariable Long id) {
        DocumentCategoryDTO category = categoryService.activateCategory(id);
        return ResponseWrapper.ok("Category activated successfully", category);
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<CommonResponse<DocumentCategoryDTO>> deactivateCategory(@PathVariable Long id) {
        DocumentCategoryDTO category = categoryService.deactivateCategory(id);
        return ResponseWrapper.ok("Category deactivated successfully", category);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseWrapper.okMessage("Category deleted successfully");
    }
}
