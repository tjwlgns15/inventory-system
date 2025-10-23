package com.yhs.inventroysystem.domain.client;

import com.yhs.inventroysystem.domain.exchange.Currency;
import com.yhs.inventroysystem.infrastructure.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clients")
@Getter
@NoArgsConstructor
public class Client extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String clientCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @Column(nullable = false)
    private String name;

    private String address;

    private String contactNumber;

    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ClientType clientType;

    // 계층 구조: 상위 거래처 (회사)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_client_id")
    private Client parentClient;

    // 하위 거래처들 (팀들)
    @OneToMany(mappedBy = "parentClient", cascade = CascadeType.ALL)
    private List<Client> childClients = new ArrayList<>();

    // 루트 거래처 생성
    public Client(String clientCode, Country country,
                  String name, String address, String contactNumber,
                  String email, Currency currency) {
        this.clientCode = clientCode;
        this.country = country;
        this.name = name;
        this.address = address;
        this.contactNumber = contactNumber;
        this.email = email;
        this.currency = currency;
        this.clientType = ClientType.PARENT;
    }

    // 하위 거래처 생성
    public Client(String clientCode, Client parentClient, Country country,
                  String name, String address, String contactNumber, String email) {
        if (parentClient.isTeam()) {
            throw new IllegalArgumentException("팀 하위에는 팀을 생성할 수 없습니다.");
        }
        this.clientCode = clientCode;
        this.parentClient = parentClient;
        this.country = country;
        this.name = name;
        this.address = address;
        this.contactNumber = contactNumber;
        this.email = email;
        this.currency = parentClient.getCurrency();
        this.clientType = ClientType.CHILD;
    }

    public void updateInfo(String name, Country country, String address, String contactNumber, String email, Currency currency) {
        this.name = name;
        this.country = country;
        this.address = address;
        this.contactNumber = contactNumber;
        this.email = email;
        this.currency = currency;
    }

    public void addChildClient(Client childClient) {
        ensureNotDeleted();
        if (this.isTeam()) {
            throw new IllegalArgumentException("팀 하위에는 팀을 추가할 수 없습니다.");
        }
        this.childClients.add(childClient);
    }

    public void markAsDeleted() {
        ensureNotDeleted();
        if (isOrganization() && !childClients.isEmpty()) {
            throw new IllegalStateException("하위 팀이 존재하는 회사는 삭제할 수 없습니다.");
        }
        this.clientCode = this.clientCode + "_DELETED_" + System.currentTimeMillis();
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isOrganization() {
        return this.clientType == ClientType.PARENT;
    }

    public boolean isTeam() {
        return this.clientType == ClientType.CHILD;
    }

    public Client getRootClient() {
        return this.parentClient != null ? this.parentClient : this;
    }

    public String getOrganizationName() {
        return isOrganization() ? this.name : this.parentClient.getName();
    }

    public boolean belongsToSameOrganization(Client other) {
        return this.getRootClient().equals(other.getRootClient());
    }

}

