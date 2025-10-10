package com.yhs.inventroysystem.presentation.client;

import com.yhs.inventroysystem.domain.client.Client;
import com.yhs.inventroysystem.application.client.ClientService;
import com.yhs.inventroysystem.presentation.client.ClientDtos.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.yhs.inventroysystem.application.client.ClientCommands.*;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientRestController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<ClientResponse> registerClient(@Valid @RequestBody ClientRegisterRequest request) {
        ClientRegisterCommand command = new ClientRegisterCommand(
                request.clientCode(),
                request.countryId(),
                request.name(),
                request.address(),
                request.contactNumber(),
                request.email(),
                request.currency()
        );

        Client client = clientService.registerClient(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ClientResponse.from(client));
    }

    @GetMapping
    public ResponseEntity<List<ClientResponse>> findAllClients() {
        List<Client> clients = clientService.findAllClient();
        List<ClientResponse> responses = clients.stream()
                .map(ClientResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<ClientResponse> getClient(@PathVariable Long clientId) {
        Client client = clientService.findClientById(clientId);
        return ResponseEntity.ok(ClientResponse.from(client));
    }

    @PatchMapping("/{clientId}")
    public ResponseEntity<ClientResponse> updateClient(
            @PathVariable Long clientId,
            @Valid @RequestBody ClientUpdateRequest request) {

        // Service 호출
        Client client = clientService.updateClient(clientId, request);
        return ResponseEntity.ok(ClientResponse.from(client));
    }
}
