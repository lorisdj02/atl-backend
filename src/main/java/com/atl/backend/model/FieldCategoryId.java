package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import lombok.Data;
import java.io.Serializable;

@Data
public class FieldCategoryId implements Serializable {
    private Long field;
    private SportCategory sportCategory;
}
