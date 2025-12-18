package com.yhs.inventroysystem.application.price;

import com.yhs.inventroysystem.domain.client.entity.Client;
import com.yhs.inventroysystem.domain.client.service.ClientDomainService;
import com.yhs.inventroysystem.domain.price.entity.ClientProductPrice;
import com.yhs.inventroysystem.domain.price.service.PriceDomainService;
import com.yhs.inventroysystem.domain.product.entity.Product;
import com.yhs.inventroysystem.domain.product.service.ProductDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.yhs.inventroysystem.application.price.PriceCommands.PriceRegisterCommand;
import static com.yhs.inventroysystem.application.price.PriceCommands.PriceUpdateCommand;

// 연관관계 엔티티는 도메인 서비스가 아닌 비즈니스 서비스로 보는 게 합리적
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PriceService {

    private final PriceDomainService priceDomainService;
    private final ClientDomainService clientDomainService;
    private final ProductDomainService productDomainService;

    @Transactional
    public ClientProductPrice registerPrice(PriceRegisterCommand command) {
        Client client = clientDomainService.findClientById(command.clientId());

        Product product = productDomainService.findById(command.productId());

        priceDomainService.existsByClientIdAndProductId(client.getId(), product.getId());

        ClientProductPrice price = new ClientProductPrice(
                client,
                product,
                command.unitPrice()
        );
        return priceDomainService.savePrice(price);
    }

    public List<ClientProductPrice> findPricesByClientId(Long clientId) {
        return priceDomainService.findByClientId(clientId);
    }

    public List<ClientProductPrice> findAllPrices() {
        return priceDomainService.findAllWithClientAndProduct();
    }

    @Transactional
    public void updatePrice(PriceUpdateCommand command) {
        ClientProductPrice price = priceDomainService.findByClientIdAndProductId(command.clientId(), command.productId());
        price.updatePrice(command.newPrice());
    }

    public ClientProductPrice findPrice(Long clientId, Long productId) {
        return priceDomainService.findByClientIdAndProductId(clientId, productId);
    }
}
