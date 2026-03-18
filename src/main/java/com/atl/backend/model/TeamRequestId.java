package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import lombok.Data;
import java.io.Serializable;

@Data
public class TeamRequestId implements Serializable {
    private Integer teamId;
    private Integer playerId;
}
