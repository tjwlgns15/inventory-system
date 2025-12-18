package com.yhs.inventroysystem.domain.price.service;

import com.yhs.inventroysystem.domain.exception.DuplicateResourceException;
import com.yhs.inventroysystem.domain.exception.ResourceNotFoundException;
import com.yhs.inventroysystem.domain.price.entity.ClientProductPrice;
import com.yhs.inventroysystem.domain.price.repository.ClientProductPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class PriceDomainService {

    private final ClientProductPriceRepository clientProductPriceRepository;

    @Transactional
    public ClientProductPrice savePrice(ClientProductPrice clientProductPrice) {
        return clientProductPriceRepository.save(clientProductPrice);
    }

    public void existsByClientIdAndProductId(Long  clientId, Long productId) {
        if (clientProductPriceRepository.existsByClientIdAndProductId(clientId, productId)) {
            throw DuplicateResourceException.price(clientId, productId);
        }
    }

    public List<ClientProductPrice> findByClientId(Long clientId) {
        return clientProductPriceRepository.findByClientId(clientId);
    }

    public List<ClientProductPrice> findAllWithClientAndProduct() {
        return clientProductPriceRepository.findAllWithClientAndProduct();
    }


    public ClientProductPrice findByClientIdAndProductId(Long clientId, Long productId) {
        return clientProductPriceRepository.findByClientIdAndProductId(clientId, productId)
                .orElseThrow(() -> ResourceNotFoundException.price(clientId, productId));
    }

    public Optional<ClientProductPrice> optionalClientIdAndProductId(Long clientId, Long productId) {
        return clientProductPriceRepository.findByClientIdAndProductId(clientId, productId);
    }

}
