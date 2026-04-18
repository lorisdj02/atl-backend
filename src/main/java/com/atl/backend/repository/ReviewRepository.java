package com.atl.backend.repository;

import com.atl.backend.model.Review;
import com.atl.backend.model.enums.ReviewStatus;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByReviewerAccount_Id(Integer reviewerAccountId);

    List<Review> findByReviewerAccount_IdAndStatus(Integer reviewerAccountId, ReviewStatus reviewStatus);

    Optional<Review> findByReviewerAccount_IdAndReviewedAccount_IdAndTournament_IdAndMatch_Id(Integer reviewerAccountId,
            Integer reviewedAccountId, Integer tournamentId, Integer matchId);
}