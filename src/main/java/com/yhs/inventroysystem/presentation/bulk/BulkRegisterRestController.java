package com.yhs.inventroysystem.presentation.bulk;

import com.yhs.inventroysystem.application.auth.UserDetails.CustomUserDetails;
import com.yhs.inventroysystem.application.bulk.BulkRegisterService;
import com.yhs.inventroysystem.application.bulk.command.*;
import com.yhs.inventroysystem.presentation.bulk.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static com.yhs.inventroysystem.application.bulk.command.PartBulkRegisterCommand.*;
import static com.yhs.inventroysystem.presentation.bulk.dto.BulkPartDtos.*;
import static com.yhs.inventroysystem.presentation.bulk.dto.BulkProductDtos.*;

@RestController
@RequestMapping("/api/bulk-register")
@RequiredArgsConstructor
public class BulkRegisterRestController {

    private final BulkRegisterService bulkRegisterService;

    @PostMapping(value = "/parts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BulkRegisterResponse> bulkRegisterParts(@RequestPart("file") MultipartFile file) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }

        Result result = bulkRegisterService.bulkRegisterParts(file);

        BulkRegisterResponse response = BulkRegisterResponse.from(result);

        // 부분 성공의 경우 207 Multi-Status 반환
        if (result.failureCount() > 0 && result.successCount() > 0) {
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(response);
        }

        // 전체 실패의 경우 400 Bad Request 반환
        if (result.successCount() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // 전체 성공의 경우 201 Created 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PostMapping(value = "/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BulkProductRegisterResponse> bulkRegisterProducts(@RequestPart("file") MultipartFile file) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }

        ProductBulkRegisterCommand.Result result = bulkRegisterService.bulkRegisterProducts(file);

        BulkProductRegisterResponse response = BulkProductRegisterResponse.from(result);

        // 부분 성공의 경우 207 Multi-Status 반환
        if (result.failureCount() > 0 && result.successCount() > 0) {
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(response);
        }

        // 전체 실패의 경우 400 Bad Request 반환
        if (result.successCount() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // 전체 성공의 경우 201 Created 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/product-part-mappings", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BulkPartProductMappingDtos.BulkMappingRegisterResponse> bulkRegisterProductPartMappings(
            @RequestPart("file") MultipartFile file) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }

        ProductPartMappingBulkCommand.Result result = bulkRegisterService.bulkRegisterProductPartMappings(file);

        BulkPartProductMappingDtos.BulkMappingRegisterResponse response = BulkPartProductMappingDtos.BulkMappingRegisterResponse.from(result);

        // 부분 성공의 경우 207 Multi-Status 반환
        if (result.failureCount() > 0 && result.successCount() > 0) {
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(response);
        }

        // 전체 실패의 경우 400 Bad Request 반환
        if (result.successCount() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // 전체 성공의 경우 201 Created 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/clients", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BulkClientDtos.BulkClientRegisterResponse> bulkRegisterClients(@RequestPart("file") MultipartFile file) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }

        ClientBulkRegisterCommand.Result result = bulkRegisterService.bulkRegisterClients(file);

        BulkClientDtos.BulkClientRegisterResponse response = BulkClientDtos.BulkClientRegisterResponse.from(result);

        // 부분 성공의 경우 207 Multi-Status 반환
        if (result.failureCount() > 0 && result.successCount() > 0) {
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(response);
        }

        // 전체 실패의 경우 400 Bad Request 반환
        if (result.successCount() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // 전체 성공의 경우 201 Created 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/prices", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BulkPriceDtos.BulkPriceRegisterResponse> bulkRegisterPrices(
            @RequestPart("file") MultipartFile file) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }

        PriceBulkRegisterCommand.Result result = bulkRegisterService.bulkRegisterPrices(file);

        BulkPriceDtos.BulkPriceRegisterResponse response = BulkPriceDtos.BulkPriceRegisterResponse.from(result);

        // 부분 성공의 경우 207 Multi-Status 반환
        if (result.failureCount() > 0 && result.successCount() > 0) {
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(response);
        }

        // 전체 실패의 경우 400 Bad Request 반환
        if (result.successCount() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // 전체 성공의 경우 201 Created 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/deliveries", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BulkDeliveryDtos.BulkDeliveryRegisterResponse> bulkRegisterDeliveries(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }

        String userName = currentUser != null ? currentUser.getName() : "SYSTEM";

        DeliveryBulkRegisterCommand.Result result = bulkRegisterService.bulkRegisterDeliveries(file, userName);

        BulkDeliveryDtos.BulkDeliveryRegisterResponse response = BulkDeliveryDtos.BulkDeliveryRegisterResponse.from(result);

        // 부분 성공의 경우 207 Multi-Status 반환
        if (result.failureCount() > 0 && result.successCount() > 0) {
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(response);
        }

        // 전체 실패의 경우 400 Bad Request 반환
        if (result.successCount() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // 전체 성공의 경우 201 Created 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/delivery-items", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BulkDeliveryItemDtos.BulkDeliveryItemRegisterResponse> bulkRegisterDeliveryItems(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다");
        }

        DeliveryItemBulkRegisterCommand.Result result = bulkRegisterService.bulkRegisterDeliveryItems(file, currentUser);

        BulkDeliveryItemDtos.BulkDeliveryItemRegisterResponse response =
                BulkDeliveryItemDtos.BulkDeliveryItemRegisterResponse.from(result);

        // 부분 성공의 경우 207 Multi-Status 반환
        if (result.failureCount() > 0 && result.successCount() > 0) {
            return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(response);
        }

        // 전체 실패의 경우 400 Bad Request 반환
        if (result.successCount() == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        // 전체 성공의 경우 201 Created 반환
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
