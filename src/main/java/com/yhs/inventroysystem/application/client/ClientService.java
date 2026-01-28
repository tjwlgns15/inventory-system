package com.yhs.inventroysystem.application.client;

import com.yhs.inventroysystem.domain.client.entity.Client;
import com.yhs.inventroysystem.domain.client.service.ClientDomainService;
import com.yhs.inventroysystem.domain.client.entity.Country;
import com.yhs.inventroysystem.domain.client.service.CountryDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.yhs.inventroysystem.application.client.ClientCommands.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientService {

    private final ClientDomainService clientDomainService;
    private final CountryDomainService countryDomainService;

    /**
     * 상위 거래처 생성
     */
    @Transactional
    public Client registerParentClient(ParentClientRegisterCommand command) {
        clientDomainService.validateClientCodeDuplication(command.clientCode());

        Country country = countryDomainService.findById(command.countryId());

        Client parentClient = new Client(
                command.clientCode(),
                country,
                command.name(),
                command.shortName(),
                command.address(),
                command.contactNumber(),
                command.email(),
                command.representative(),
                command.currency(),
                command.shipmentDestination(),
                command.shipmentAddress(),
                command.shipmentRepresentative(),
                command.shipmentContactNumber(),
                command.finalDestination()
        );

        return clientDomainService.saveClient(parentClient);
    }

    /**
     * 하위 거래처 생성
     */
    @Transactional
    public Client registerChildClient(ChildClientRegisterCommand command) {
        Client parentClient = clientDomainService.findById(command.parentClientId());

        Country country = countryDomainService.findById(command.countryId());

        Client childClient = new Client(
                command.clientCode(),
                parentClient,
                country,
                command.name(),
                command.shortName(),
                command.address(),
                command.contactNumber(),
                command.email(),
                command.representative(),
                command.currency(),
                command.shipmentDestination(),
                command.shipmentAddress(),
                command.shipmentRepresentative(),
                command.shipmentContactNumber(),
                command.finalDestination()
        );

        parentClient.addChildClient(childClient);
        return clientDomainService.saveClient(childClient);
    }


    public List<Client> findAllClient() {
        return clientDomainService.findAllActiveWithCountry();
    }

    public Client findClientById(Long clientId) {
        return clientDomainService.findClientById(clientId);
    }

    @Transactional
    public Client updateClient(Long clientId, ChildClientUpdateCommand command) {
        Client client = findClientById(clientId);

        Country country = countryDomainService.findById(command.countryId());

        client.updateInfo(
                command.name(),
                command.shortName(),
                country,
                command.address(),
                command.contactNumber(),
                command.email(),
                command.representative(),
                command.currency(),
                command.shipmentDestination(),
                command.shipmentAddress(),
                command.shipmentRepresentative(),
                command.shipmentContactNumber(),
                command.finalDestination()
        );

        return client;
    }

    @Transactional
    public void deleteClient(Long clientId) {
        Client client = findClientById(clientId);
        client.markAsDeleted();
    }
}
