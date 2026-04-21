package com.atl.backend.response;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import com.atl.backend.config.ApiJsonKeys;
import com.atl.backend.dto.FieldDTO;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldsAndMaxPlayersPerFieldResponse {
    
    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.fields)
    private List<FieldDTO> fields = new ArrayList<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.maxPlayersPerField)
    private Map<Long, Short> maxPlayersPerField = new HashMap<>();

    /*
     * final List<Field> fields;
     * final Map<Long, Short> maxPlayersPerField;
     */
}
