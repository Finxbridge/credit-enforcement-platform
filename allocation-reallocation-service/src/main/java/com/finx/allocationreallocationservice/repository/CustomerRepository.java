package com.finx.allocationreallocationservice.repository;

import com.finx.allocationreallocationservice.domain.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByCustomerId(String customerId);
    Optional<Customer> findByMobileNumber(String mobileNumber);
    Optional<Customer> findByEmailAddress(String emailAddress);
}
