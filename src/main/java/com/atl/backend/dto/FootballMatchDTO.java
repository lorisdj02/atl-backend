package com.atl.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

import com.atl.backend.model.FootballMatch;
import com.atl.backend.model.Match;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FootballMatchDTO extends MatchDTO {

    @JsonProperty("homeTeamId")
    private Integer homeTeamId;

    @JsonProperty("awayTeamId")
    private Integer awayTeamId;

    @JsonProperty("scoreHome")
    private Short scoreHome;

    @JsonProperty("penaltyScoreHome")
    private Short penaltyScoreHome;

    @JsonProperty("missedPenaltyHome")
    private Short missedPenaltyHome;

    @JsonProperty("scoreAway")
    private Short scoreAway;

    @JsonProperty("penaltyScoreAway")
    private Short penaltyScoreAway;

    @JsonProperty("missedPenaltyAway")
    private Short missedPenaltyAway;

    @NotNull
    @JsonProperty("status")
    private String status;

    @JsonProperty("halftime")
    private LocalDateTime halftime;

    @JsonProperty("startHalf")
    private LocalDateTime startHalf;

    public static FootballMatchDTO fromFootballMatch(FootballMatch footballMatch) {
        if (footballMatch == null) {
            return null;
        }

        Match match = footballMatch.getMatch();

        FootballMatchDTO dto = new FootballMatchDTO();
        MatchDTO baseDto = MatchDTO.fromMatch(match);

        dto.setId(baseDto.getId() != null ? baseDto.getId() : null);
        dto.setScheduledStart(baseDto.getScheduledStart() != null ? baseDto.getScheduledStart() : null);
        dto.setTournamentId(baseDto.getTournamentId() != null ? baseDto.getTournamentId() : null);
        dto.setMotmPlayerId(baseDto.getMotmPlayerId() != null ? baseDto.getMotmPlayerId() : null);
        dto.setGroupName(baseDto.getGroupName() != null ? baseDto.getGroupName() : null);
        dto.setSportCategory(baseDto.getSportCategory() != null ? baseDto.getSportCategory() : null);
        dto.setLegOrder(baseDto.getLegOrder() != null ? baseDto.getLegOrder() : null);
        dto.setCoordinatorStatus(baseDto.getCoordinatorStatus() != null ? baseDto.getCoordinatorStatus() : null);
        dto.setFinalStageBracketId(baseDto.getFinalStageBracketId() != null ? baseDto.getFinalStageBracketId() : null);
        
        dto.setHomeTeamId(footballMatch.getHomeTeam() != null ? footballMatch.getHomeTeam().getId() : null);
        dto.setScoreHome(footballMatch.getScoreHome() != null ? footballMatch.getScoreHome() : null);
        dto.setPenaltyScoreHome(footballMatch.getPenaltyScoreHome() != null ? footballMatch.getPenaltyScoreHome() : null);
        dto.setMissedPenaltyHome(footballMatch.getMissedPenaltyHome() != null ? footballMatch.getMissedPenaltyHome() : null);
        
        dto.setAwayTeamId(footballMatch.getAwayTeam() != null ? footballMatch.getAwayTeam().getId() : null);
        dto.setScoreAway(footballMatch.getScoreAway() != null ? footballMatch.getScoreAway() : null);
        dto.setPenaltyScoreAway(footballMatch.getPenaltyScoreAway() != null ? footballMatch.getPenaltyScoreAway() : null);
        dto.setMissedPenaltyAway(footballMatch.getMissedPenaltyAway() != null ? footballMatch.getMissedPenaltyAway() : null);

        dto.setHalftime(footballMatch.getHalftime() != null ? footballMatch.getHalftime() : null);
        dto.setStartHalf(footballMatch.getStartHalf() != null ? footballMatch.getStartHalf() : null);
        dto.setStatus(footballMatch.getStatus().toString());
        return dto;
    }
}
