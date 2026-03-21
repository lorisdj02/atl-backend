package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import lombok.Data;
import java.io.Serializable;

@Data
public class FieldCategoryId implements Serializable {
    private Integer field;
    private SportCategory sportCategory;
}
