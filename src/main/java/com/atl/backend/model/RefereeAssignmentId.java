package com.atl.backend.model;

import lombok.Data;
import java.io.Serializable;

@Data
public class RefereeAssignmentId implements Serializable {
    private Integer matchId;
    private Integer refereeId;
}
