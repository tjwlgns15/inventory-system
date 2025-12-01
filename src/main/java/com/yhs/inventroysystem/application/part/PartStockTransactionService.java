package com.yhs.inventroysystem.application.part;

import com.yhs.inventroysystem.domain.part.Part;
import com.yhs.inventroysystem.domain.part.PartStockTransaction;
import com.yhs.inventroysystem.domain.part.PartStockTransactionRepository;
import com.yhs.inventroysystem.domain.part.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PartStockTransactionService {

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
                part, type, beforeStock, changeQuantity, afterStock, note
        );

        partStockTransactionRepository.save(transaction);
    }
}
