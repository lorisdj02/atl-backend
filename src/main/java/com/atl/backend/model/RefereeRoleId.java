package com.atl.backend.model;

import lombok.Data;
import java.io.Serializable;

@Data
public class RefereeRoleId implements Serializable {
    private Long sport;
    private String name;
    private Short level;
}
