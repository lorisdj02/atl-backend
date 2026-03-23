package com.atl.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;

import com.atl.backend.model.Tournament;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TournamentDTO {

    @NotNull
    @JsonProperty("id")
    private Integer id;

    @NotNull
    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    @Size(max = 511)
    private String description;

    @NotNull
    @JsonProperty("coordinatorId")
    private Integer coordinatorId;

    @NotNull
    @JsonProperty("fieldId")
    private Integer fieldId;

    @NotNull
    @JsonProperty("cityId")
    private Integer cityId;

    @NotNull
    @JsonProperty("sportId")
    private Integer sportId;

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
        return dto;
    }
}
