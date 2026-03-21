package com.atl.backend.model;

import lombok.Data;
import java.io.Serializable;

@Data
public class PlaysAtId implements Serializable {
    private Integer team;
    private Integer player;
}
