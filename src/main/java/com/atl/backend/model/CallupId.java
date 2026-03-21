package com.atl.backend.model;

import lombok.Data;
import java.io.Serializable;

@Data
public class CallupId implements Serializable {
    private Integer tournament;
    private Integer player;
}