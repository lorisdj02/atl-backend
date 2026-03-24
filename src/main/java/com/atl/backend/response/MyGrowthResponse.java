package com.atl.backend.response;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atl.backend.config.ApiJsonKeys;
import com.atl.backend.dto.GrowthStepDTO;
import com.atl.backend.model.enums.SportCategory;
import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MyGrowthResponse {
    @NotNull
    @JsonProperty(ApiJsonKeys.growthSteps)
    private List<GrowthStepDTO> growthSteps;

    @NotNull
    @JsonProperty(ApiJsonKeys.level)
    private Short level;

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.minRefereePayPerCategoryPerLevel)
    private Map<Short, Map<SportCategory, BigDecimal>> minRefereePayPerCategoryPerLevel = new HashMap<>();

    @NotNull
    @Builder.Default
    @JsonProperty(ApiJsonKeys.maxRefereePayPerCategoryPerLevel)
    private Map<Short, Map<SportCategory, BigDecimal>> maxRefereePayPerCategoryPerLevel = new HashMap<>();
}
