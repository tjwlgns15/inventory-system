package com.yhs.inventroysystem.presentation.contract;

import com.yhs.inventroysystem.application.contract.ContractCommands;
import com.yhs.inventroysystem.application.contract.ContractService;
import com.yhs.inventroysystem.domain.contract.entity.Contract;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.yhs.inventroysystem.presentation.contract.ContractDtos.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contracts")
public class ContractRestController {

    private final ContractService contractService;


    @PostMapping
    public ResponseEntity<Void> createContract(@Valid @RequestBody ContractCreate request) {
        ContractCommands.CreateCommand command = request.toCommand();
        contractService.createContract(command);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<PageContractResponse> getContractsPaged(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "recordDate") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String direction,
            @RequestParam(required = false) String keyword) {

        Page<Contract> contractPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            contractPage = contractService.searchContracts(keyword, page, size, sortBy, direction);
        } else {
            contractPage = contractService.findAllContractsPaged(page, size, sortBy, direction);
        }

        return ResponseEntity.ok(PageContractResponse.from(contractPage));
    }

    @GetMapping("/{contractId}")
    public ResponseEntity<ContractResponse> getContract(@PathVariable Long contractId) {
        Contract contract = contractService.findContractById(contractId);

        return ResponseEntity.ok(ContractResponse.from(contract));
    }

    @PatchMapping("/{contractId}")
    public ResponseEntity<ContractResponse> updateContract(
            @PathVariable Long contractId,
            @RequestBody ContractUpdate request) {

        ContractCommands.UpdateCommand command = request.toCommand();
        Contract contract = contractService.updateContract(contractId, command);

        return ResponseEntity.ok(ContractResponse.from(contract));
    }

    @DeleteMapping("/{contractId}")
    public ResponseEntity<Void> deleteContract(@PathVariable Long contractId) {
        contractService.deleteContract(contractId);
        return ResponseEntity.ok().build();
    }
}