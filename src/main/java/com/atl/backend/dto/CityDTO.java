package com.atl.backend.dto;


import com.atl.backend.model.City;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CityDTO {
    @NotNull
    @JsonProperty("id")
    private Long id;

    @NotNull
    @JsonProperty("name")
    private String name;

    public static CityDTO fromCity(City city) {
        if (city == null) {
            return null;
        }

        CityDTO dto = new CityDTO();
        dto.setId(city.getId());
        dto.setName(city.getName());
        return dto;
    }
}
