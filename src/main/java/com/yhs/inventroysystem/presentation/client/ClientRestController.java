package com.yhs.inventroysystem.presentation.client;

import com.yhs.inventroysystem.domain.client.entity.Client;
import com.yhs.inventroysystem.application.client.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.yhs.inventroysystem.application.client.ClientCommands.*;
import static com.yhs.inventroysystem.presentation.client.ClientDtos.*;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ClientRestController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<ClientResponse> registerParentClient(@Valid @RequestBody ParentClientRegisterRequest request) {
        ParentClientRegisterCommand command = new ParentClientRegisterCommand(
                request.clientCode(),
                request.countryId(),
                request.name(),
                request.address(),
                request.contactNumber(),
                request.email(),
                request.currency()
        );

        Client client = clientService.registerParentClient(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ClientResponse.from(client));
    }

    @PostMapping("/child")
    public ResponseEntity<ClientResponse> registerChildClient(@Valid @RequestBody ChildClientRegisterRequest request) {
        ChildClientRegisterCommand command = new ChildClientRegisterCommand(
                request.parentClientId(),
                request.clientCode(),
                request.countryId(),
                request.name(),
                request.address(),
                request.contactNumber(),
                request.email(),
                request.currency()
        );

        Client client = clientService.registerChildClient(command);
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

        ChildClientUpdateCommand command = new ChildClientUpdateCommand(
                request.name(),
                request.countryId(),
                request.address(),
                request.contactNumber(),
                request.email(),
                request.currency()
        );
        Client client = clientService.updateClient(clientId, command);
        return ResponseEntity.ok(ClientResponse.from(client));
    }

    @DeleteMapping("/{clientId}")
    public ResponseEntity<ClientResponse> deleteClient(@PathVariable Long clientId) {
        clientService.deleteClient(clientId);
        return ResponseEntity.noContent().build();
    }
}
