package com.atl.backend.model;

import com.atl.backend.model.enums.*;

import lombok.Data;
import java.io.Serializable;

@Data
public class CallupId implements Serializable {
    private Integer tournamentId;
    private Integer playerId;
}