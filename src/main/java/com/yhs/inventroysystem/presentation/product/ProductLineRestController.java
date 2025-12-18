package com.yhs.inventroysystem.presentation.product;

import com.yhs.inventroysystem.application.product.ProductLineCommands.PLRegisterCommand;
import com.yhs.inventroysystem.application.product.ProductLineService;
import com.yhs.inventroysystem.domain.product.entity.ProductLine;
import com.yhs.inventroysystem.presentation.product.ProductLineDtos.PLRegisterRequest;
import com.yhs.inventroysystem.presentation.product.ProductLineDtos.PLResponse;
import com.yhs.inventroysystem.presentation.product.ProductLineDtos.PLUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.yhs.inventroysystem.application.product.ProductLineCommands.*;

@RestController
@RequestMapping("/api/product-lines")
@RequiredArgsConstructor
public class ProductLineRestController {

    private final ProductLineService productLineService;

    @PostMapping
    public ResponseEntity<PLResponse> registerProductLine(@Valid @RequestBody PLRegisterRequest request) {
        PLRegisterCommand command = new PLRegisterCommand(request.name());
        ProductLine productLine = productLineService.registerProductLine(command);

        return ResponseEntity.status(HttpStatus.CREATED).body(PLResponse.from(productLine));
    }

    @GetMapping
    public ResponseEntity<List<PLResponse>> getAllProductLines() {
        List<ProductLine> productLines = productLineService.getAllProductLines();
        List<PLResponse> responses = productLines.stream()
                .map(PLResponse::from)
                .toList();
        return ResponseEntity.ok().body(responses);
    }

    @PatchMapping("/{productLineId}")
    public ResponseEntity<PLResponse> updateProductLine(
            @PathVariable Long productLineId,
            @Valid @RequestBody PLUpdateRequest request) {

        PLUpdateCommand command = new PLUpdateCommand(request.name());
        ProductLine productLine = productLineService.updateProductLine(productLineId, command);

        return ResponseEntity.ok().body(PLResponse.from(productLine));
    }

    @DeleteMapping("/{productLineId}")
    public ResponseEntity<Void>  deleteProductLine(@PathVariable Long productLineId) {
        productLineService.deleteProductLine(productLineId);
        return ResponseEntity.noContent().build();
    }
}