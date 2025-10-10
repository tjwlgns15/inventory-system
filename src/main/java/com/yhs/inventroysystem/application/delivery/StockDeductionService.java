package com.yhs.inventroysystem.application.delivery;

import com.yhs.inventroysystem.domain.delivery.Delivery;
import com.yhs.inventroysystem.domain.delivery.DeliveryItem;
import com.yhs.inventroysystem.domain.exception.ResourceNotFoundException;
import com.yhs.inventroysystem.domain.part.Part;
import com.yhs.inventroysystem.domain.product.Product;
import com.yhs.inventroysystem.domain.product.ProductPart;
import com.yhs.inventroysystem.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StockDeductionService {

    private final ProductRepository productRepository;

    public void deductStock(Delivery delivery) {
        for (DeliveryItem item : delivery.getItems()) {
            Product product = productRepository.findByIdWithParts(item.getProduct().getId())
                    .orElseThrow(() -> ResourceNotFoundException.product(item.getProduct().getId()));

            // 제품 재고 차감
            product.decreaseStock(item.getQuantity());

            // 부품 재고 차감
//            deductPartStock(product, item.getQuantity());
        }
    }

    private void deductPartStock(Product product, Integer productQuantity) {
        for (ProductPart mapping : product.getPartMappings()) {
            Part part = mapping.getPart();
            Integer requiredQuantity = mapping.calculateTotalRequired(productQuantity);
            part.decreaseStock(requiredQuantity);
        }
    }
}