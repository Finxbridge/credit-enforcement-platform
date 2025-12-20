package com.finx.management.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Read-only entity for agencies table.
 * Used only to fetch approved agencies for user creation dropdown.
 */
@Entity
@Table(name = "agencies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Agency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agency_code", unique = true, nullable = false, length = 50)
    private String agencyCode;

    @Column(name = "agency_name", nullable = false, length = 200)
    private String agencyName;

    @Column(name = "status", length = 30)
    private String status;
}
