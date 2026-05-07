package com.atl.backend.service.accountServices;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.ReviewDTO;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.exception.UnauthorizedException;
import com.atl.backend.model.Account;
import com.atl.backend.model.Match;
import com.atl.backend.model.Review;
import com.atl.backend.model.Tournament;
import com.atl.backend.model.Transaction;
import com.atl.backend.model.enums.ReviewStatus;
import com.atl.backend.model.enums.ReviewType;
import com.atl.backend.model.enums.SportCategory;
import com.atl.backend.repository.ReviewRepository;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    public Review updateReview(Long id, Transaction linkedTransaction ,ReviewStatus status) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorsString.REVIEW_NOT_FOUND));
        review.setStatus(status);
        review.setLinkedTransaction(linkedTransaction);
        review.setUpdatedAt(Instant.now());
        return reviewRepository.save(review);
    }

    @Transactional(rollbackFor = Exception.class)
    public Review commitReview(ReviewDTO reviewDTO) {
        if (reviewDTO == null || reviewDTO.getId() == null) {
            throw new NotFoundException(ErrorsString.REVIEW_NOT_FOUND);
        }

        if (reviewDTO.getRating() == null || reviewDTO.getRating().compareTo(BigDecimal.ZERO) < 0
                || reviewDTO.getRating().compareTo(BigDecimal.valueOf(5)) > 0) {
            throw new BadRequestException(ErrorsString.INVALID_REVIEW_RATING);
        }

        Review review = reviewRepository.findById(reviewDTO.getId())
                .orElseThrow(() -> new NotFoundException(ErrorsString.REVIEW_NOT_FOUND));

        if (!review.getReviewerAccount().getId().equals(AccountContext.getAccountId())) {
            throw new UnauthorizedException(ErrorsString.UNAUTHORIZED);
        }

        if (!review.getReviewedAccount().getId().equals(reviewDTO.getReviewedAccountId())) {
            throw new NotFoundException(ErrorsString.REVIEW_NOT_FOUND);
        }

        review.setComment(reviewDTO.getComment());
        review.setRating(reviewDTO.getRating().round(new MathContext(2)));
        review.setStatus(ReviewStatus.COMMITTED);
        review.setUpdatedAt(Instant.now());
        review.setCommittedAt(Instant.now());
        return reviewRepository.save(review);
    }

    public Review createReview(Account reviewerAccount, Account reviewedAccount, Tournament tournament, Match match,
            ReviewType reviewType, BigDecimal weight, Transaction linkedTransaction, SportCategory sportCategory) {
        Review review = new Review();
        review.setReviewerAccount(reviewerAccount);
        review.setReviewedAccount(reviewedAccount);
        review.setTournament(tournament);
        review.setMatch(match);
        review.setReviewType(reviewType);
        review.setWeight(weight);
        review.setLinkedTransaction(linkedTransaction);
        review.setStatus(ReviewStatus.WAITING);
        review.setSportCategory(sportCategory);
        return reviewRepository.save(review);
    }

    public Optional<Review> getReviewByReviewerAccount_IdAndReviewedAccount_IdAndTournament_Id(Long reviewerAccountId,
            Long reviewedAccountId, Long tournamentId) {
        return reviewRepository
                .findByReviewerAccount_IdAndReviewedAccount_IdAndTournament_Id(reviewerAccountId,
                        reviewedAccountId, tournamentId);
    }

    public Review getReviewByReviewerAccountIdAndReviewedAccountIdAndTournamentIdAndMatchId(Long reviewerAccountId,
            Long reviewedAccountId, Long tournamentId, Long matchId) {
        return reviewRepository
                .findByReviewerAccount_IdAndReviewedAccount_IdAndTournament_IdAndMatch_Id(reviewerAccountId,
                        reviewedAccountId, tournamentId, matchId)
                .orElseThrow(() -> new NotFoundException(ErrorsString.REVIEW_NOT_FOUND));
    }

    public Review saveReview(Review review) {
        return reviewRepository.save(review);
    }
}
