package com.yhs.inventroysystem.application.price;

import com.yhs.inventroysystem.domain.exception.DuplicateResourceException;
import com.yhs.inventroysystem.domain.exception.ResourceNotFoundException;
import com.yhs.inventroysystem.domain.price.ClientProductPriceRepository;
import com.yhs.inventroysystem.domain.client.Client;
import com.yhs.inventroysystem.domain.price.ClientProductPrice;
import com.yhs.inventroysystem.domain.product.Product;
import com.yhs.inventroysystem.domain.client.ClientRepository;
import com.yhs.inventroysystem.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

import static com.yhs.inventroysystem.application.price.PriceCommands.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PriceService {

    private final ClientProductPriceRepository priceRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;

    @Transactional
    public ClientProductPrice registerPrice(PriceRegisterCommand command) {
        Client client = clientRepository.findById(command.clientId())
                .orElseThrow(() -> ResourceNotFoundException.client(command.clientId()));

        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> ResourceNotFoundException.product(command.productId()));

        if (priceRepository.existsByClientIdAndProductId(command.clientId(), command.productId())) {
            throw DuplicateResourceException.price(command.clientId(), command.productId());
        }

        ClientProductPrice price = new ClientProductPrice(
                client,
                product,
                command.unitPrice()
        );
        return priceRepository.save(price);
    }

    public List<ClientProductPrice> findPricesByClientId(Long clientId) {
        return priceRepository.findByClientId(clientId);
    }

    public List<ClientProductPrice> findAllPrices() {
        return priceRepository.findAllWithClientAndProduct();
    }

    @Transactional
    public void updatePrice(PriceUpdateCommand command) {
        ClientProductPrice price = priceRepository.findByClientIdAndProductId(command.clientId(), command.productId())
                .orElseThrow(() -> ResourceNotFoundException.price(command.clientId(), command.productId()));

        price.updatePrice(command.newPrice());
    }

    public ClientProductPrice findPrice(Long clientId, Long productId) {
        return priceRepository.findByClientIdAndProductId(clientId, productId)
                .orElseThrow(() -> ResourceNotFoundException.price(clientId, productId));
    }
}
