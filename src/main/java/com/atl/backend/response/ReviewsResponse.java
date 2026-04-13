package com.atl.backend.response;

import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import com.atl.backend.config.ApiJsonKeys;
import com.atl.backend.dto.AccountDTO;
import com.atl.backend.dto.ReviewDTO;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewsResponse {

    @NotNull
    @JsonProperty(ApiJsonKeys.reviews)
    private List<ReviewDTO> reviews;

    @NotNull
    @JsonProperty(ApiJsonKeys.accountPerReview)
    private Map<Integer, AccountDTO> accountPerReview;

    @NotNull
    @JsonProperty(ApiJsonKeys.matchTeamsStringPerReview)
    private Map<Integer, String> matchTeamsStringPerReview;
    /*
     * final List<Review> reviews;
     * final Map<int, Account> accountPerReview;
     * final Map<int, String> matchTeamsStringPerReview;
     */
}
