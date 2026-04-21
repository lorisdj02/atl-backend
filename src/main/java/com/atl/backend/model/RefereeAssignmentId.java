package com.atl.backend.model;

import lombok.Data;
import java.io.Serializable;

@Data
public class RefereeAssignmentId implements Serializable {
    private Long match;
    private Long referee;
}
