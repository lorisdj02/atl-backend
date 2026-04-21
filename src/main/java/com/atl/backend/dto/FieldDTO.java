package com.atl.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import com.atl.backend.model.Field;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldDTO {
    @NotNull
    @JsonProperty("id")
    private Long id;

    @NotNull
    @JsonProperty("name")
    private String name;

    @NotNull
    @JsonProperty("address")
    private String address;

    @NotNull
    @JsonProperty("locationLat")
    private Double locationLat;

    @NotNull
    @JsonProperty("locationLng")
    private Double locationLng;

    @NotNull
    @JsonProperty("cityId")
    private Long cityId;

    @NotNull
    @JsonProperty("hourCostTournament")
    private BigDecimal hourCostTournament;

    @NotNull
    @JsonProperty("hourCostPrivate")
    private BigDecimal hourCostPrivate;

    public static FieldDTO fromField(Field field) {
        if (field == null) {
            return null;
        }

        FieldDTO dto = new FieldDTO();
        dto.setId(field.getId());
        dto.setName(field.getName());
        dto.setAddress(field.getAddress());
        dto.setLocationLat(field.getLocationLat());
        dto.setLocationLng(field.getLocationLng());
        dto.setCityId(field.getCity().getId());
        dto.setHourCostTournament(field.getHourCostTournament());
        dto.setHourCostPrivate(field.getHourCostPrivate());
        return dto;
    }
}
