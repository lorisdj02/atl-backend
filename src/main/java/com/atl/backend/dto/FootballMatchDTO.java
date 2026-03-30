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

        dto.setId(baseDto.getId());
        dto.setScheduledStart(baseDto.getScheduledStart());
        dto.setTournamentId(baseDto.getTournamentId());
        dto.setMotmPlayerId(baseDto.getMotmPlayerId());
        dto.setGroupName(baseDto.getGroupName());
        dto.setSportCategory(baseDto.getSportCategory());
        dto.setLegOrder(baseDto.getLegOrder());
        dto.setCoordinatorStatus(baseDto.getCoordinatorStatus());
        dto.setFinalStageBracketId(baseDto.getFinalStageBracketId());
        
        dto.setHomeTeamId(footballMatch.getHomeTeam() != null ? footballMatch.getHomeTeam().getId() : null);
        dto.setScoreHome(footballMatch.getScoreHome());
        dto.setPenaltyScoreHome(footballMatch.getPenaltyScoreHome());
        dto.setMissedPenaltyHome(footballMatch.getMissedPenaltyHome());
        
        dto.setAwayTeamId(footballMatch.getAwayTeam() != null ? footballMatch.getAwayTeam().getId() : null);
        dto.setScoreAway(footballMatch.getScoreAway());
        dto.setPenaltyScoreAway(footballMatch.getPenaltyScoreAway());
        dto.setMissedPenaltyAway(footballMatch.getMissedPenaltyAway());

        dto.setHalftime(footballMatch.getHalftime());
        dto.setStartHalf(footballMatch.getStartHalf());
        dto.setStatus(footballMatch.getStatus().toString());
        return dto;
    }
}
