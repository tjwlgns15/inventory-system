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

    /**
     * 상위 거래처 생성
     */
    @Transactional
    public Client registerParentClient(ParentClientRegisterCommand command) {
        validateClientCodeDuplication(command.clientCode());

        Country country = countryRepository.findById(command.countryId())
                .orElseThrow(() -> ResourceNotFoundException.country(command.countryId()));

        Client parentClient = new Client(
                command.clientCode(),
                country,
                command.name(),
                command.address(),
                command.contactNumber(),
                command.email(),
                command.currency()
        );

        return clientRepository.save(parentClient);
    }

    /**
     * 하위 거래처 생성
     */
    @Transactional
    public Client registerChildClient(ChildClientRegisterCommand command) {
        Client parentClient = clientRepository.findById(command.parentClientId())
                .orElseThrow(() -> ResourceNotFoundException.client(command.parentClientId()));

        Country country = countryRepository.findById(command.countryId())
                .orElseThrow(() -> ResourceNotFoundException.country(command.countryId()));

        Client childClient = new Client(
                command.clientCode(),
                parentClient,
                country,
                command.name(),
                command.address(),
                command.contactNumber(),
                command.email()
        );

        parentClient.addChildClient(childClient);
        return clientRepository.save(childClient);
    }


    public List<Client> findAllClient() {
        return clientRepository.findAllActiveWithCountry();
    }

    public Client findClientById(Long clientId) {
        return clientRepository.findByIdAndDeletedAt(clientId)
                .orElseThrow(() -> ResourceNotFoundException.client(clientId));
    }

    @Transactional
    public Client updateClient(Long clientId, ChildClientUpdateCommand command) {
        Client client = findClientById(clientId);

        Country country = countryRepository.findById(command.countryId())
                .orElseThrow(() -> ResourceNotFoundException.country(command.countryId()));

        client.updateInfo(
                command.name(),
                country,
                command.address(),
                command.contactNumber(),
                command.email(),
                command.currency()
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
