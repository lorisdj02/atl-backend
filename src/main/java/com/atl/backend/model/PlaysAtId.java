package com.atl.backend.model;

import lombok.Data;
import java.io.Serializable;

@Data
public class PlaysAtId implements Serializable {
    private Long team;
    private Long player;
}
