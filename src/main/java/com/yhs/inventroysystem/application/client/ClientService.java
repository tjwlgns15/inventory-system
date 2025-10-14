package com.yhs.inventroysystem.application.client;

import com.yhs.inventroysystem.domain.client.Client;
import com.yhs.inventroysystem.domain.client.ClientRepository;
import com.yhs.inventroysystem.domain.client.Country;
import com.yhs.inventroysystem.domain.client.CountryRepository;
import com.yhs.inventroysystem.domain.exception.DuplicateResourceException;
import com.yhs.inventroysystem.domain.exception.ResourceNotFoundException;
import com.yhs.inventroysystem.presentation.client.ClientDtos.ClientUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.yhs.inventroysystem.application.client.ClientCommands.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientService {

    private final ClientRepository clientRepository;
    private final CountryRepository countryRepository;

    @Transactional
    public Client registerClient(ClientRegisterCommand command) {
        validateClientCodeDuplication(command.clientCode());

        Country country = countryRepository.findById(command.countryId())
                .orElseThrow(() -> ResourceNotFoundException.country(command.countryId()));

        Client client = new Client(
                command.clientCode(),
                country,
                command.name(),
                command.address(),
                command.contactNumber(),
                command.email(),
                command.currency()
        );

        return clientRepository.save(client);
    }

    public List<Client> findAllClient() {
        return clientRepository.findAllActiveWithCountry();
    }

    public Client findClientById(Long clientId) {
        return clientRepository.findByIdAndDeletedAt(clientId)
                .orElseThrow(() -> ResourceNotFoundException.client(clientId));
    }

    @Transactional
    public Client updateClient(Long clientId, ClientUpdateRequest request) {
        Client client = findClientById(clientId);

        Country country = countryRepository.findById(request.countryId())
                .orElseThrow(() -> ResourceNotFoundException.country(request.countryId()));

        client.updateInfo(
                request.name(),
                country,
                request.address(),
                request.contactNumber(),
                request.email(),
                request.currency()
        );

        return client;
    }

    @Transactional
    public void deleteClient(Long clientId) {
        Client client = findClientById(clientId);
        client.markAsDeleted();
    }

    private void validateClientCodeDuplication(String clientCode) {
        if (clientRepository.existsByClientCodeAndNotDeleted(clientCode)) {
            throw DuplicateResourceException.clientCode(clientCode);
        }
    }
}
