package com.atl.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FootballMatchEventModifiedValues {

    @JsonProperty("homeScoreDelta")
    Short homeScoreDelta;

    @JsonProperty("awayScoreDelta")
    Short awayScoreDelta;

    @JsonProperty("homePtsDelta")
    Short homePtsDelta;

    @JsonProperty("awayPtsDelta")
    Short awayPtsDelta;

    @JsonProperty("homeWonDelta")
    Short homeWonDelta;

    @JsonProperty("awayWonDelta")
    Short awayWonDelta;

    @JsonProperty("homeDrawnDelta")
    Short homeDrawnDelta;
    
    @JsonProperty("awayDrawnDelta")
    Short awayDrawnDelta;

    @JsonProperty("homeLostDelta")
    Short homeLostDelta;

    @JsonProperty("awayLostDelta")
    Short awayLostDelta;

    @JsonProperty("penaltyScoreHomeDelta")
    Short penaltyScoreHomeDelta;

    @JsonProperty("penaltyScoreAwayDelta")
    Short penaltyScoreAwayDelta;

    @JsonProperty("missedPenaltyHomeDelta")
    Short missedPenaltyHomeDelta;

    @JsonProperty("missedPenaltyAwayDelta")
    Short missedPenaltyAwayDelta;
}
