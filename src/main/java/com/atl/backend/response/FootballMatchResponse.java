package com.atl.backend.response;

import java.util.List;
import java.math.BigDecimal;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import com.atl.backend.config.ApiJsonKeys;
import com.atl.backend.dto.FieldDTO;
import com.atl.backend.dto.FinalStageBracketDTO;
import com.atl.backend.dto.FootballMatchDTO;
import com.atl.backend.dto.FootballMatchEventDTO;
import com.atl.backend.dto.PlayerDTO;
import com.atl.backend.dto.TeamDTO;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)

public class FootballMatchResponse {

    @NotNull
    @JsonProperty(ApiJsonKeys.match)
    private FootballMatchDTO match;

    @JsonProperty(ApiJsonKeys.finalStageBracket)
    private FinalStageBracketDTO finalStageBracket;

    @JsonProperty(ApiJsonKeys.homeTeam)
    private TeamDTO homeTeam;

    @JsonProperty(ApiJsonKeys.awayTeam)
    private TeamDTO awayTeam;

    @JsonProperty(ApiJsonKeys.events)
    private List<FootballMatchEventDTO> events;

    @JsonProperty(ApiJsonKeys.minutesPerRound)
    private Short minutesPerRound;

    @JsonProperty(ApiJsonKeys.minutesPerPause)
    private Short minutesPerPause;

    @JsonProperty(ApiJsonKeys.availableRefereeRoles)
    private List<String> availableRefereeRoles;

    @JsonProperty(ApiJsonKeys.playersPerTeam)
    private Map<Long, List<PlayerDTO>> playersPerTeam;

    @JsonProperty(ApiJsonKeys.assignmentPerRefereeNameSurname)
    private Map<String, String> assignmentPerRefereeNameSurname;

    @JsonProperty(ApiJsonKeys.field)
    private FieldDTO field;

    @JsonProperty(ApiJsonKeys.cityName)
    private String cityName;

    @JsonProperty(ApiJsonKeys.coordinatorId)
    private Long coordinatorId;

    @JsonProperty(ApiJsonKeys.refereeCheck)
    private Boolean refereeCheck;

    @JsonProperty(ApiJsonKeys.canMatchFinish)
    private Boolean canMatchFinish;

    @JsonProperty(ApiJsonKeys.hasRefereeLicense)
    private Boolean hasRefereeLicense;

    @JsonProperty(ApiJsonKeys.payPerRole)
    private Map<String, BigDecimal> payPerRole;
}
