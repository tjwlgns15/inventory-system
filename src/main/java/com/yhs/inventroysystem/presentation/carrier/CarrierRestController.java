package com.yhs.inventroysystem.presentation.carrier;

import com.yhs.inventroysystem.application.carrier.CarrierService;
import com.yhs.inventroysystem.domain.carrier.entity.Carrier;
import com.yhs.inventroysystem.domain.carrier.service.CarrierCommand.CarrierRegisterCommand;
import com.yhs.inventroysystem.presentation.carrier.CarrierDtos.CarrierResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.yhs.inventroysystem.domain.carrier.service.CarrierCommand.CarrierUpdateCommand;
import static com.yhs.inventroysystem.presentation.carrier.CarrierDtos.CarrierRegister;
import static com.yhs.inventroysystem.presentation.carrier.CarrierDtos.CarrierUpdate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carriers")
public class CarrierRestController {

    private final CarrierService carrierService;


    @PostMapping
    public ResponseEntity<CarrierResponse> registerCarrier(@Valid @RequestBody CarrierRegister request) {
        CarrierRegisterCommand command = request.toCommand();
        Carrier carrier = carrierService.registerCarrier(command);
        return ResponseEntity.ok(CarrierResponse.from(carrier));
    }

    @GetMapping("/{carrierId}")
    public ResponseEntity<CarrierResponse> getCarrier(@PathVariable("carrierId") Long carrierId) {
        Carrier carrier = carrierService.findCarrier(carrierId);
        return ResponseEntity.ok(CarrierResponse.from(carrier));
    }

    @GetMapping
    public ResponseEntity<List<CarrierResponse>> getAllCarriers() {
        List<Carrier> carriers = carrierService.findAllCarrier();
        List<CarrierResponse> carrierResponses = carriers.stream()
                .map(CarrierResponse::from)
                .toList();
        return ResponseEntity.ok(carrierResponses);
    }

    @PutMapping("/{carrierId}")
    public ResponseEntity<CarrierResponse> updateCarrier(
            @PathVariable("carrierId") Long carrierId,
            @Valid @RequestBody CarrierUpdate request) {

        CarrierUpdateCommand command = request.toCommand();
        Carrier carrier = carrierService.updateCarrier(carrierId, command);
        return ResponseEntity.ok(CarrierResponse.from(carrier));
    }
}
