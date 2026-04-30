package com.atl.backend.dto;

import com.atl.backend.model.Player;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlayerDTO {
    @NotNull
    @JsonProperty("playerId")
    private Long playerId;

    @NotNull
    @JsonProperty("accountId")
    private Long accountId;

    @NotNull
    @JsonProperty("username")
    private String username;

    @NotNull
    @JsonProperty("name")
    private String name;

    @NotNull
    @JsonProperty("surname")
    private String surname;

    @JsonProperty("imageUrl")
    private String imageUrl;

    @NotNull
    @JsonProperty("vip")
    private boolean vip;

    @NotNull
    @JsonProperty("vipType")
    private String vipType;

    @JsonProperty("vipStart")
    private String vipStart;

    @JsonProperty("vipEnd")
    private String vipEnd;

    @JsonProperty("preferredCityId")
    private Long preferredCityId;

    @NotNull
    @JsonProperty("birthdate")
    private String birthdate;

    public static PlayerDTO fromPlayer(Player player) {
        if (player == null) {
            return null;
        }

        PlayerDTO dto = new PlayerDTO();
        dto.setPlayerId(player.getPlayerId());
        dto.setAccountId(player.getAccount().getId());
        dto.setUsername(player.getAccount().getUsername());
        dto.setName(player.getAccount().getName());
        dto.setSurname(player.getAccount().getSurname());
        dto.setImageUrl(player.getAccount().getImageUrl());
        dto.setVip(player.getAccount().getVip());
        dto.setVipType(player.getAccount().getVipType() != null ? player.getAccount().getVipType().toString() : null);
        dto.setVipStart(
                player.getAccount().getVipStart() != null ? player.getAccount().getVipStart().toString() : null);
        dto.setVipEnd(player.getAccount().getVipEnd() != null ? player.getAccount().getVipEnd().toString() : null);
        dto.setPreferredCityId(
                player.getAccount().getPreferredCity() != null ? player.getAccount().getPreferredCity().getId() : null);
        dto.setBirthdate(
                player.getAccount().getBirthdate() != null ? player.getAccount().getBirthdate().toString() : null);

        return dto;
    }

}
