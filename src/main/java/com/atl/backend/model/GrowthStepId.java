package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import lombok.Data;
import java.io.Serializable;

@Data
public class GrowthStepId implements Serializable {
    private GrowthStepType type;
    private Short level;
}
