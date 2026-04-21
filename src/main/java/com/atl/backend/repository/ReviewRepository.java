package com.atl.backend.repository;

import com.atl.backend.model.Review;
import com.atl.backend.model.enums.ReviewStatus;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByReviewerAccount_Id(Long reviewerAccountId);

    List<Review> findByReviewerAccount_IdAndStatus(Long reviewerAccountId, ReviewStatus reviewStatus);

    Optional<Review> findByReviewerAccount_IdAndReviewedAccount_IdAndTournament_IdAndMatch_Id(Long reviewerAccountId,
            Long reviewedAccountId, Long tournamentId, Long matchId);

    Optional<Review> findByReviewerAccount_IdAndReviewedAccount_IdAndTournament_Id(Long reviewerAccountId,
            Long reviewedAccountId, Long tournamentId);
}