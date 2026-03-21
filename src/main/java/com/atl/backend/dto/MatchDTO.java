package com.atl.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MatchDTO {

    @NotNull
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("scheduledStart")
    private LocalDateTime scheduledStart;

    @JsonProperty("startMatch")
    private LocalDateTime startMatch;

    @JsonProperty("finishMatch")
    private LocalDateTime finishMatch;

    @NotNull
    @JsonProperty("tournamentId")
    private Integer tournamentId;

    @JsonProperty("motmPlayerId")
    private Integer motmPlayerId;

    @JsonProperty("groupName")
    private String groupName;

    @NotNull
    @JsonProperty("sportCategory")
    private String sportCategory;

    @JsonProperty("legType")
    private String legType;

    @JsonProperty("coordinatorStatus")
    @NotNull
    private String coordinatorStatus;

    @JsonProperty("finalStageBracketId")
    private Integer finalStageBracketId;
}
