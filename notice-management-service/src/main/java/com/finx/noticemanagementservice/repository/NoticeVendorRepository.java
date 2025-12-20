package com.finx.noticemanagementservice.repository;

import com.finx.noticemanagementservice.domain.entity.NoticeVendor;
import com.finx.noticemanagementservice.domain.enums.VendorType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoticeVendorRepository extends JpaRepository<NoticeVendor, Long> {

    Optional<NoticeVendor> findByVendorCode(String vendorCode);

    List<NoticeVendor> findByIsActiveTrue();

    List<NoticeVendor> findByVendorType(VendorType vendorType);

    List<NoticeVendor> findByVendorTypeAndIsActiveTrue(VendorType vendorType);

    Page<NoticeVendor> findByIsActive(Boolean isActive, Pageable pageable);

    @Query("SELECT v FROM NoticeVendor v WHERE v.isActive = true ORDER BY v.priorityOrder ASC")
    List<NoticeVendor> findActiveVendorsByPriority();

    @Query("SELECT v FROM NoticeVendor v WHERE v.vendorType = :type AND v.isActive = true " +
           "ORDER BY v.priorityOrder ASC")
    List<NoticeVendor> findActiveVendorsByTypeAndPriority(@Param("type") VendorType type);

    @Query("SELECT v FROM NoticeVendor v WHERE v.serviceAreas LIKE %:pincode%")
    List<NoticeVendor> findVendorsServicingPincode(@Param("pincode") String pincode);

    boolean existsByVendorCode(String vendorCode);

    @Query("SELECT v FROM NoticeVendor v WHERE v.isActive = true AND v.vendorType = :type " +
           "ORDER BY v.costPerDispatch ASC")
    List<NoticeVendor> findCheapestActiveVendorsByType(@Param("type") VendorType type);
}
