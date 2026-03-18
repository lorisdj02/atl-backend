package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "organization")
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "organization_id")
    private Integer organizationId;

    @Column(name = "account_id", unique = true, nullable = false)
    private Integer accountId;

    @Column(name = "org_name", nullable = false)
    private String orgName;
}
