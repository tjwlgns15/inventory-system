package com.yhs.inventroysystem.service;

import com.yhs.inventroysystem.dto.StockTransactionResponse;
import com.yhs.inventroysystem.entity.StockTransaction;
import com.yhs.inventroysystem.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockTransactionService {

    private final StockTransactionRepository stockTransactionRepository;

    public List<StockTransactionResponse> findAll() {
        return stockTransactionRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(StockTransactionResponse::from)
                .toList();
    }

    public List<StockTransactionResponse> findByPartId(Long partId) {
        return stockTransactionRepository.findByPartIdOrderByCreatedAtDesc(partId).stream()
                .map((StockTransactionResponse::from))
                .toList();
    }

}
