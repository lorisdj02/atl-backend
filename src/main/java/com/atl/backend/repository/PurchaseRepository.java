package com.atl.backend.repository;

import com.atl.backend.model.Purchase;
import com.atl.backend.model.enums.CheckoutType;
import com.atl.backend.model.enums.PurchaseStatus;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    List<Purchase> findByTypeAndObjectIdAndAccount_IdAndStatus(CheckoutType type, Long objectId, Long accountId,
            PurchaseStatus status);

    List<Purchase> findByTypeAndObjectIdAndStatus(CheckoutType type, Long objectId, PurchaseStatus status);

    List<Purchase> findByAccount_IdAndStatus(Long accountId, PurchaseStatus status);

    List<Purchase> findByAccount_Id(Long accountId);
}