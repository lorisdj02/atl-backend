package com.atl.backend.service.accountServices;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atl.backend.auth.AccountContext;
import com.atl.backend.config.ErrorsString;
import com.atl.backend.dto.ReviewDTO;
import com.atl.backend.exception.BadRequestException;
import com.atl.backend.exception.NotFoundException;
import com.atl.backend.exception.UnauthorizedException;
import com.atl.backend.model.Review;
import com.atl.backend.model.enums.ReviewType;
import com.atl.backend.repository.AccountRepository;
import com.atl.backend.repository.ReviewRepository;

@Service
public class ReviewService {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ReviewRepository reviewRepository;

    @Transactional(rollbackFor = Exception.class)
    public void commitReview(ReviewDTO reviewDTO) {
        if (reviewDTO == null || reviewDTO.getId() == null) {
            throw new NotFoundException(ErrorsString.REVIEW_NOT_FOUND);
        }

        if (reviewDTO.getRating() == null || reviewDTO.getRating().compareTo(BigDecimal.ZERO) < 0
                || reviewDTO.getRating().compareTo(BigDecimal.valueOf(5)) > 0) {
            throw new BadRequestException(ErrorsString.INVALID_REVIEW_RATING);
        }

        Review review = reviewRepository.findById(reviewDTO.getId())
                .orElseThrow(() -> new NotFoundException(ErrorsString.REVIEW_NOT_FOUND));

        if (review.getReviewerAccount().getId() != AccountContext.getAccountId()) {
            throw new UnauthorizedException(ErrorsString.UNAUTHORIZED);
        }

        if (review.getReviewedAccount().getId() != reviewDTO.getReviewedAccountId()) {
            throw new NotFoundException(ErrorsString.REVIEW_NOT_FOUND);
        }

        /*
         * TODO: Manca:
         *
         * A) Creazione di una review in stato WAITING e della corrispettiva transazione
         * di pagamento.
         * Questa può essere fatta:
         *
         * 1) Inizia un torneo -> ogni convocato ha una sua review in stato WAITING. NON
         * collegata ad una transazione, tranne per il vincitore.
         *
         * 2) Inizio di un match -> Si creano le recensioni e rispettive transazioni per
         * i referee.
         *
         * 3) Fine di un match -> Si creano le recensioni e rispettive transazioni per i
         * coordinatori.
         * Le transazioni sono BLOCCANTI.
         *
         *
         * B) Passaggio in stato PENDING. Può esser fatto:
         *
         * 1) Alla fine di un percorso di una squadra per un torneo, quando si è sicuri
         * che non ci saranno più partite per quella squadra per i player.
         * 2) Alla fine di un match per i referee.
         * 3) A metà e fine torneo per i coordinatori.
         *
         * C) Passaggio in stato COMMITTED. Questo DEVE sbloccare la/le transazione/i di pagamento.
         *
         */

        review.setComment(reviewDTO.getComment());
        review.setRating(reviewDTO.getRating());
        review.setUpdatedAt(LocalDateTime.now());
        reviewRepository.save(review);
    }
}
