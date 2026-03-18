package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "cities")
public class City {
    @Id
    private Integer id;

    @Column(unique = true, nullable = false)
    private String name;
}
