package com.yhs.inventroysystem.domain.client.service;

import com.yhs.inventroysystem.domain.client.entity.Client;
import com.yhs.inventroysystem.domain.client.repository.ClientRepository;
import com.yhs.inventroysystem.domain.client.repository.CountryRepository;
import com.yhs.inventroysystem.domain.exception.DuplicateResourceException;
import com.yhs.inventroysystem.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientDomainService {

    private final ClientRepository clientRepository;
    private final CountryRepository countryRepository;

    @Transactional
    public Client saveClient(Client client) {
        return clientRepository.save(client);
    }

    public Client findById(Long clientId) {
        return clientRepository.findById(clientId)
                .orElseThrow(() -> ResourceNotFoundException.client(clientId));
    }

    public List<Client> findAllActiveWithCountry() {
        return clientRepository.findAllActiveWithCountry();
    }

    public Client findClientById(Long clientId) {
        return clientRepository.findByIdAndDeletedAt(clientId)
                .orElseThrow(() -> ResourceNotFoundException.client(clientId));
    }

    public void validateClientCodeDuplication(String clientCode) {
        if (clientRepository.existsByClientCodeAndNotDeleted(clientCode)) {
            throw DuplicateResourceException.clientCode(clientCode);
        }
    }
}
