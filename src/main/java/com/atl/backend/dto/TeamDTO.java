package com.atl.backend.dto;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamDTO {

    @NotNull
    @JsonProperty("id")
    private Long id;

    @JsonProperty("cityId")
    private Long cityId;

    @JsonProperty("captainPlayerId")
    private Long captainPlayerId;

    @JsonProperty("vice1PlayerId")
    private Long vice1PlayerId;

    @JsonProperty("vice2PlayerId")
    private Long vice2PlayerId;

    @JsonProperty("logoUrl")
    private String logoUrl;

    @NotNull
    @JsonProperty("rankPts")
    private Integer rankPts = 0;

    @NotNull
    @JsonProperty("name")
    private String name;

    @NotNull
    @JsonProperty("alias")
    private String alias;

    @JsonProperty("description")
    @Size(max = 511)
    private String description;

    @NotNull
    @JsonProperty("isVip")
    private Boolean isVip = false;

    @NotNull
    @JsonProperty("maxPlayers")
    private Short maxPlayers = 24;

    @NotNull
    @JsonProperty("numPlayers")
    private Short numPlayers = 1;

    @NotNull
    @JsonProperty("sportCategory")
    private String sportCategory;

    @NotNull
    @JsonProperty("visible")
    private Boolean visible = true;

    public static TeamDTO fromTeam(com.atl.backend.model.Team team) {
        if (team == null) {
            return null;
        }
        TeamDTO dto = new TeamDTO();

        dto.setId(team.getId());
        dto.setCityId(team.getCity() != null ? team.getCity().getId() : null);
        dto.setCaptainPlayerId(team.getCaptainPlayer().getPlayerId());
        dto.setVice1PlayerId(team.getVice1Player() != null ? team.getVice1Player().getPlayerId() : null);
        dto.setVice2PlayerId(team.getVice2Player() != null ? team.getVice2Player().getPlayerId() : null);

        dto.setLogoUrl(team.getLogoUrl());

        dto.setRankPts(team.getRankPts());
        dto.setName(team.getName());
        dto.setAlias(team.getAlias());
        dto.setDescription(team.getDescription());
        dto.setIsVip(team.getIsVip());
        dto.setMaxPlayers(team.getMaxPlayers());
        dto.setNumPlayers(team.getNumPlayers());
        dto.setSportCategory(team.getSportCategory() != null ? team.getSportCategory().toString() : null);
        dto.setVisible(team.getVisible());

        return dto;
    }
}