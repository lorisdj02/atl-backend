package com.atl.backend.model;

import lombok.Data;
import java.io.Serializable;

@Data
public class TeamRequestId implements Serializable {
    private Integer teamId;
    private Integer playerId;
}
