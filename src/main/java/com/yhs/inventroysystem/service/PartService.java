package com.yhs.inventroysystem.service;


import com.yhs.inventroysystem.dto.PartCategoryResponse;
import com.yhs.inventroysystem.dto.PartRequest;
import com.yhs.inventroysystem.dto.PartResponse;
import com.yhs.inventroysystem.dto.PartUpdateRequest;
import com.yhs.inventroysystem.entity.Part;
import com.yhs.inventroysystem.entity.PartCategory;
import com.yhs.inventroysystem.entity.StockTransaction;
import com.yhs.inventroysystem.entity.enumerate.TransactionType;
import com.yhs.inventroysystem.repository.PartCategoryRepository;
import com.yhs.inventroysystem.repository.PartRepository;
import com.yhs.inventroysystem.repository.StockTransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.yhs.inventroysystem.entity.enumerate.TransactionType.INBOUND;

@Service
@RequiredArgsConstructor
@Transactional  // 클래스 전체에 트랜잭션 적용
public class PartService {

    private final PartRepository partRepo;
    private final PartCategoryRepository categoryRepo;
    private final StockTransactionRepository stockTransactionRepo;

    /**
     * 전체 부품 목록 조회
     * - Entity → DTO로 변환
     */
    public List<PartResponse> findAllParts(){
        return partRepo.findAll().stream()
                .map(p -> new PartResponse(
                        p.getId(),
                        p.getPartCode(),
                        p.getName(),
                        p.getStock(),
                        p.getInitialQty(),
                        p.getCategory().getName()
                )).toList();
    }

    /**
     * 전체 부품 분류 목록 조회
     */
    public List<PartCategoryResponse> findAllPartCategories(){
        return categoryRepo.findAll().stream()
                .map(c -> new PartCategoryResponse(c.getId(), c.getName()))
                .toList();
    }

    /**
     * 새로운 부품 등록
     * - 중복 코드 검사
     * - 분류 유효성 확인
     * - 입고 수량 > 0일 경우 입고 이력도 함께 저장
     */
    public void addPart(PartRequest request){
        if(partRepo.existsByPartCode(request.getCode())){
            throw new RuntimeException("중복된 부품 코드입니다.");
        }

        PartCategory category = categoryRepo.findByName(request.getCategory())
                .orElseThrow(() -> new RuntimeException("해당 분류 없음"));

        Part part = Part.create(request.getCode(), request.getName(), request.getQty(), category);

        partRepo.save(part);

        // 입고 이력 저장 (초기 수량이 0보다 큰 경우)
        if (request.getQty() > 0) {
            StockTransaction partStockTransaction = StockTransaction.create(part, INBOUND, 0, request.getQty(), request.getQty());
            stockTransactionRepo.save(partStockTransaction);
        }
    }

    /**
     * 부품 정보 일부 수정
     * - 이름, 재고, 분류 변경 가능
     */
    public void updatePart(Long id, PartUpdateRequest updates){
        Part part = partRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("부품이 존재하지 않습니다."));


        if (updates.getName() != null && !updates.getName().trim().isEmpty()) {
            // 카테고리는 기존 값 유지하고 이름만 변경
            part.updateName(updates.getName());
        }

        if (updates.getCategory() != null && !updates.getCategory().trim().isEmpty()) {
            PartCategory category = categoryRepo.findByName(updates.getCategory())
                    .orElseThrow(() -> new RuntimeException("분류가 존재하지 않습니다: " + updates.getCategory()));

            // 이름은 기존 값 유지하고 카테고리만 변경
            part.updateCategory(category);
        }

        // 재고 수정
        if (updates.getStock() != null){
            part.adjustStock(updates.getStock());
        }

        partRepo.save(part);
    }

    /**
     * 부품 삭제
     */
    public void deletePart(Long id){
        Part part = partRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("부품이 존재하지 않습니다."));

        partRepo.delete(part);
    }

    /**
     * 부품 분류 삭제
     */
    @Transactional
    public void deleteCategory(Long id){
        categoryRepo.deleteById(id);
    }

    /**
     * 새 분류 추가
     * - 이름 중복 검사 포함
     */
    public void addCategory(String name){
        if(categoryRepo.existsByName(name)){
            throw new RuntimeException("이미 존재하는 분류입니다.");
        }
        categoryRepo.save(new PartCategory(null, name));
    }

    /**
     * 재고 입출고 처리
     * - delta가 양수면 입고, 음수면 출고
     * - 출고 시 재고 부족 검사
     * - 변경 이력을 StockTransaction에 저장
     */
    @Transactional
    public void changeStock(Long partId, int delta, String type) {
        Part part = partRepo.findById(partId)
                .orElseThrow(() -> new RuntimeException("부품이 존재하지 않습니다."));

        TransactionType transactionType = TransactionType.valueOf(type);

        int before = part.getStock();

        if (transactionType.equals(TransactionType.INBOUND)) {
            part.increaseStock(Math.abs(delta));
        } else {
            part.decreaseStock(Math.abs(delta));
        }

        int after = part.getStock();

        partRepo.save(part);

        // 트랜잭션 기록 시 설명 추가
        StockTransaction partStockTransaction = StockTransaction.create(part, transactionType, before, delta, after);

        stockTransactionRepo.save(partStockTransaction);
    }

}
