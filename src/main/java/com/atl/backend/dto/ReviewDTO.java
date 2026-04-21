package com.atl.backend.dto;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

import com.atl.backend.model.Review;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewDTO {
    @NotNull
    @JsonProperty("id")
    private Long id;

    @NotNull
    @JsonProperty("tournamentId")
    private Long tournamentId;

    @JsonProperty("matchId")
    private Long matchId;

    @NotNull
    @JsonProperty("reviewerAccountId")
    private Long reviewerAccountId;

    @NotNull
    @JsonProperty("reviewedAccountId")
    private Long reviewedAccountId;

    @JsonProperty("comment")
    private String comment;

    @NotNull
    @JsonProperty("rating")
    private BigDecimal rating;

    @NotNull
    @JsonProperty("reviewType")
    private String reviewType;

    @NotNull
    @JsonProperty("sportCategory")
    private String sportCategory;

    public static ReviewDTO fromReview(Review review) {
        if (review == null) {
            return null;
        }

        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setTournamentId(review.getTournament().getId());
        dto.setMatchId(review.getMatch() != null ? review.getMatch().getId() : null);
        dto.setReviewerAccountId(review.getReviewerAccount().getId());
        dto.setReviewedAccountId(review.getReviewedAccount().getId());
        dto.setComment(review.getComment());
        dto.setRating(review.getRating());
        dto.setReviewType(review.getReviewType().name());
        dto.setSportCategory(review.getSportCategory().name());
        return dto;
    }
}
