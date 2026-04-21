package com.atl.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;

import com.atl.backend.model.Tournament;
import com.atl.backend.model.enums.TournamentStatus;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TournamentDTO {

    @NotNull
    @JsonProperty("id")
    private Long id;

    @NotNull
    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    @Size(max = 511)
    private String description;

    @NotNull
    @JsonProperty("coordinatorId")
    private Long coordinatorId;

    @NotNull
    @JsonProperty("fieldId")
    private Long fieldId;

    @NotNull
    @JsonProperty("cityId")
    private Long cityId;

    @NotNull
    @JsonProperty("sportId")
    private Long sportId;

    @NotNull
    @JsonProperty("isFavorite")
    private Boolean isFavorite = false;

    @NotNull
    @JsonProperty("isParticipating")
    private Boolean isParticipating = false;

    @NotNull
    @JsonProperty("numGroups")
    private Short numGroups;

    @NotNull
    @JsonProperty("numTeams")
    private Short numTeams;

    @NotNull
    @JsonProperty("paidTeams")
    private Short paidTeams;

    @NotNull
    @JsonProperty("status")
    private String status;

    @NotNull
    @JsonProperty("atlEvent")
    private Boolean atlEvent = false;

    @NotNull
    @JsonProperty("prize1")
    private BigDecimal prize1;

    @NotNull
    @JsonProperty("prize2")
    private BigDecimal prize2 = BigDecimal.ZERO;

    @NotNull
    @JsonProperty("startDate")
    private LocalDate startDate;

    @NotNull
    @JsonProperty("fee")
    private BigDecimal fee;

    private String photoUrl;

    @JsonProperty("round2GroupStage")
    @NotNull
    private Boolean round2GroupStage = false;

    @JsonProperty("round2FinalStage")
    @NotNull
    private Boolean round2FinalStage = false;

    @JsonProperty("numTeamsQualified")
    @NotNull
    private Short numTeamsQualified;

    @JsonProperty("public")
    @NotNull
    private Boolean isPublic = false;


    public static TournamentDTO fromTournament(Tournament tournament) {
        TournamentDTO dto = new TournamentDTO();
        dto.setId(tournament.getId());
        dto.setName(tournament.getName());
        dto.setDescription(tournament.getDescription() != null ? tournament.getDescription() : null);
        dto.setCoordinatorId(tournament.getCoordinator().getCoordinatorId());
        dto.setFieldId(tournament.getField().getId());
        dto.setCityId(tournament.getField().getCity().getId());
        dto.setSportId(tournament.getSport().getId());
        dto.setNumGroups(tournament.getNumGroups());
        dto.setNumTeams(tournament.getNumTeams());
        dto.setPaidTeams(tournament.getPaidTeams());
        dto.setStatus(tournament.getStatus().toString());
        dto.setAtlEvent(tournament.getAtlEvent());
        dto.setPrize1(tournament.getPrize1());
        dto.setPrize2(tournament.getPrize2());
        dto.setStartDate(tournament.getStartDate());
        dto.setFee(tournament.getFee());
        dto.setRound2GroupStage(tournament.getRound2GroupStage());
        dto.setRound2FinalStage(tournament.getRound2FinalStage());
        dto.setNumTeamsQualified(tournament.getNumTeamsQualified());

        dto.setPhotoUrl(tournament.getPhotoUrl() != null ? tournament.getPhotoUrl() : null);
        dto.setIsPublic(tournament.getIsPublic());
        return dto;
    }

    public static Tournament toTournament(TournamentDTO dto) {
        Tournament tournament = new Tournament();
        tournament.setId(dto.getId());
        tournament.setName(dto.getName());
        tournament.setDescription(dto.getDescription());
        tournament.setNumGroups(dto.getNumGroups());
        tournament.setNumTeams(dto.getNumTeams());
        tournament.setPaidTeams(dto.getPaidTeams());
        tournament.setStatus(TournamentStatus.valueOf(dto.getStatus()));
        tournament.setAtlEvent(dto.getAtlEvent());
        tournament.setPrize1(dto.getPrize1());
        tournament.setPrize2(dto.getPrize2());
        tournament.setStartDate(dto.getStartDate());
        tournament.setFee(dto.getFee());
        tournament.setRound2GroupStage(dto.getRound2GroupStage());
        tournament.setRound2FinalStage(dto.getRound2FinalStage());
        tournament.setNumTeamsQualified(dto.getNumTeamsQualified());
        tournament.setIsPublic(dto.getIsPublic());
        return tournament;
    }
}
