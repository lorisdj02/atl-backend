package com.atl.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "\"order\"")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "object_id", nullable = false)
    private Integer objectId;

    @Column(nullable = false, length = 31)
    private String type;

    @Column(columnDefinition = "jsonb")
    private String detail;

    @Column(nullable = false, length = 15)
    private String status = "CONFIRMED";

    @Column(name = "transaction_group_id", nullable = false)
    private Integer transactionGroupId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
