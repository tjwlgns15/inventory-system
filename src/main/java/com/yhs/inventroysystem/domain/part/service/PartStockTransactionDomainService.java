package com.yhs.inventroysystem.domain.part.service;

import com.yhs.inventroysystem.domain.part.entity.Part;
import com.yhs.inventroysystem.domain.part.entity.PartStockTransaction;
import com.yhs.inventroysystem.domain.part.entity.TransactionType;
import com.yhs.inventroysystem.domain.part.repository.PartStockTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PartStockTransactionDomainService {

    private final PartStockTransactionRepository partStockTransactionRepository;

    @Transactional
    public void recordTransaction(Part part, TransactionType type,
                                  int beforeStock, int changeQuantity) {
        int afterStock = part.getStockQuantity();

        PartStockTransaction transaction = PartStockTransaction.create(
                part,
                type,
                beforeStock,
                changeQuantity,
                afterStock
        );

        partStockTransactionRepository.save(transaction);
    }

    public List<PartStockTransaction> findByPartId(Long partId) {
        return partStockTransactionRepository.findByPartIdOrderByCreatedAtDesc(partId);
    }

    public List<PartStockTransaction> findAll() {
        return partStockTransactionRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public void recordTransactionWithNote(Part part, TransactionType type,
                                          int beforeStock, int changeQuantity, String note) {
        int afterStock = beforeStock + changeQuantity;

        PartStockTransaction transaction = PartStockTransaction.createWithNote(
                part,
                type,
                beforeStock,
                changeQuantity,
                afterStock,
                note
        );

        partStockTransactionRepository.save(transaction);
    }
}
