package com.yhs.inventroysystem.presentation.price;

import com.yhs.inventroysystem.domain.price.ClientProductPrice;
import com.yhs.inventroysystem.application.price.PriceService;
import com.yhs.inventroysystem.presentation.client.ClientDtos.*;
import com.yhs.inventroysystem.presentation.price.PriceDtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.yhs.inventroysystem.application.price.PriceCommands.*;

@RestController
@RequestMapping("/api/prices")
@RequiredArgsConstructor
public class PriceRestController {

    private final PriceService priceService;

    @PostMapping
    public ResponseEntity<PriceResponse> registerPrice(@Valid @RequestBody PriceRegisterRequest request) {
        PriceRegisterCommand command = new PriceRegisterCommand(
                request.clientId(),
                request.productId(),
                request.unitPrice()
        );

        ClientProductPrice price = priceService.registerPrice(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PriceResponse.from(price));
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<PriceResponse>> getPricesByClient(@PathVariable Long clientId) {
        List<ClientProductPrice> prices = priceService.findPricesByClientId(clientId);
        List<PriceResponse> responses = prices.stream()
                .map(PriceResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/all")
    public ResponseEntity<List<PriceResponse>> getAllPrices() {
        List<ClientProductPrice> prices = priceService.findAllPrices();
        List<PriceResponse> responses = prices.stream()
                .map(PriceResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PatchMapping
    public ResponseEntity<Void> updatePrice(@Valid @RequestBody PriceUpdateRequest request) {
        PriceUpdateCommand command = new PriceUpdateCommand(
                request.clientId(),
                request.productId(),
                request.newPrice()
        );
        priceService.updatePrice(command);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<PriceResponse> getPrice(
            @RequestParam Long clientId,
            @RequestParam Long productId) {
        ClientProductPrice price = priceService.findPrice(clientId, productId);
        return ResponseEntity.ok(PriceResponse.from(price));
    }
}
