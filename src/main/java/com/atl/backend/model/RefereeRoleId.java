package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import lombok.Data;
import java.io.Serializable;

@Data
public class RefereeRoleId implements Serializable {
    private Integer sportId;
    private String name;
    private Short level;
}
