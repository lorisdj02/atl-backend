package com.atl.backend.model;

import lombok.Data;
import java.io.Serializable;

@Data
public class RefereeLicenseId implements Serializable {
    private Integer sport;
    private Integer referee;
}
