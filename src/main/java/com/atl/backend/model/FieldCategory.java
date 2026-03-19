package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@IdClass(FieldCategoryId.class)
@Table(name = "field_category")
public class FieldCategory {
    @Id
    @Column(name = "field_id", nullable = false)
    private Integer fieldId;

    @MapsId("fieldId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "field_id", nullable = false, insertable = false, updatable = false)
    private Field field;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "sport_category", nullable = false, length = 15)
    private SportCategory sportCategory;

    @Column(name = "max_players", nullable = false)
    private Short maxPlayers;
}
