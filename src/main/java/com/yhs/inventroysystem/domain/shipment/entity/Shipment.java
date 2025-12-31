package com.yhs.inventroysystem.domain.shipment.entity;

import com.yhs.inventroysystem.domain.carrier.entity.Carrier;
import com.yhs.inventroysystem.infrastructure.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shipments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Shipment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========== Invoice 기본 정보 ==========
    @Column(nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @Column(nullable = false)
    private LocalDate invoiceDate;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer sequence;

    // ========== Shipper / Exporter 정보 (판매자 = 솔미테크) ==========
    @Column(nullable = false, length = 200)
    private String shipperCompanyName;

    @Column(nullable = false, length = 500)
    private String shipperAddress;

    @Column(length = 100)
    private String shipperContactPerson;

    @Column(length = 50)
    private String shipperPhone;

    // ========== Sold To 정보 (고객사) ==========
    @Column(name = "client_id")
    private Long clientId;

    @Column(nullable = false, length = 200)
    private String soldToCompanyName;

    @Column(nullable = false, length = 500)
    private String soldToAddress;

    @Column(length = 100)
    private String soldToContactPerson;

    @Column(length = 50)
    private String soldToPhone;

    // ========== Ship To 정보 (제품 발송처) ==========
    @Column(nullable = false, length = 200)
    private String shipToCompanyName;

    @Column(nullable = false, length = 500)
    private String shipToAddress;

    @Column(length = 100)
    private String shipToContactPerson;

    @Column(length = 50)
    private String shipToPhone;

    // ========== 운송 정보 ==========
    @Column(nullable = false, length = 200)
    private String portOfLoading;

    @Column(nullable = false, length = 200)
    private String finalDestination;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrier_id")
    private Carrier carrier;

    @Column(length = 200)
    private String carrierName;

    @Column(nullable = false)
    private LocalDate freightDate;


    // ========== 신용장 정보 (선택) ==========
    @Column(length = 100)
    private String lcNo; // 신용장 번호

    private LocalDate lcDate; // 신용장 발급일

    @Column(length = 200)
    private String lcIssuingBank; // 신용장 발행 은행명

    // ========== Remark 정보 ==========

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShipmentType shipmentType;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private TradeTerms tradeTerms; // 거래 조건

    @Column(length = 500)
    private String originDescription; // 원산지 설명

    @Column(length = 1000)
    private String additionalRemarks;

    // ========== 패키징 정보 ==========
    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 100)
    private List<ShipmentBoxItem> boxItems = new ArrayList<>();

    @Column(nullable = false)
    private Integer totalBoxCount = 0;

    // ========== 제품 정보 ==========
    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 100)
    private List<ShipmentItem> items = new ArrayList<>();

    @Column(nullable = false)
    private Integer totalQuantity = 0;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false, length = 10)
    private String currency;

    // ========== Packing List 추가 정보 (합산값) ==========
    @Column(precision = 10, scale = 3)
    private BigDecimal totalNetWeight; // 순중량 (Net Weight) - kg

    @Column(precision = 10, scale = 3)
    private BigDecimal totalGrossWeight; // 총중량 (Gross Weight) - kg

    @Column(precision = 10, scale = 3)
    private BigDecimal totalCbm; // CBM (Cubic Meter)


    private Shipment(String invoiceNumber, LocalDate invoiceDate, int year, int sequence,
                     String shipperCompanyName, String shipperAddress, String shipperContactPerson, String shipperPhone,
                     Long clientId, String soldToCompanyName, String soldToAddress, String soldToContactPerson, String soldToPhone,
                     String shipToCompanyName, String shipToAddress, String shipToContactPerson, String shipToPhone,
                     String portOfLoading, String finalDestination, Carrier carrier, String carrierName, LocalDate freightDate,
                     String lcNo, LocalDate lcDate, String lcIssuingBank,
                     ShipmentType shipmentType, TradeTerms tradeTerms, String originDescription, String additionalRemarks,
                     String currency) {
        this.invoiceNumber = invoiceNumber;
        this.invoiceDate = invoiceDate;
        this.shipperCompanyName = shipperCompanyName;
        this.year = year;
        this.sequence = sequence;
        this.shipperAddress = shipperAddress;
        this.shipperContactPerson = shipperContactPerson;
        this.shipperPhone = shipperPhone;
        this.clientId = clientId;
        this.soldToCompanyName = soldToCompanyName;
        this.soldToAddress = soldToAddress;
        this.soldToContactPerson = soldToContactPerson;
        this.soldToPhone = soldToPhone;
        this.shipToCompanyName = shipToCompanyName;
        this.shipToAddress = shipToAddress;
        this.shipToContactPerson = shipToContactPerson;
        this.shipToPhone = shipToPhone;
        this.portOfLoading = portOfLoading != null ? portOfLoading : "Incheon, Korea";
        this.finalDestination = finalDestination;
        this.carrier = carrier;
        this.carrierName = carrierName;
        this.freightDate = freightDate;
        this.lcNo = lcNo;
        this.lcDate = lcDate;
        this.lcIssuingBank = lcIssuingBank;
        this.shipmentType = shipmentType;
        this.tradeTerms = tradeTerms;
        this.originDescription = originDescription;
        this.additionalRemarks = additionalRemarks;
        this.currency = currency != null ? currency : "USD";
        this.totalBoxCount = 0;
        this.totalQuantity = 0;
        this.totalAmount = BigDecimal.ZERO;
        this.totalNetWeight = BigDecimal.ZERO;
        this.totalGrossWeight = BigDecimal.ZERO;
        this.totalCbm = BigDecimal.ZERO;
    }

    /**
     * Shipment 생성
     */
    public static Shipment create(String invoiceNumber, LocalDate invoiceDate, int year, int sequence,
                                  String shipperCompanyName, String shipperAddress,
                                  String shipperContactPerson, String shipperPhone,
                                  Long clientId, String soldToCompanyName, String soldToAddress,
                                  String soldToContactPerson, String soldToPhone,
                                  String shipToCompanyName, String shipToAddress,
                                  String shipToContactPerson, String shipToPhone,
                                  String portOfLoading, String finalDestination,
                                  Carrier carrier, String carrierName, LocalDate freightDate,
                                  String lcNo, LocalDate lcDate, String lcIssuingBank,
                                  ShipmentType shipmentType, TradeTerms tradeTerms,
                                  String originDescription, String additionalRemarks,
                                  String currency) {
        return new Shipment(
                invoiceNumber,
                invoiceDate,
                year,
                sequence,
                shipperCompanyName,
                shipperAddress,
                shipperContactPerson,
                shipperPhone,
                clientId,
                soldToCompanyName,
                soldToAddress,
                soldToContactPerson,
                soldToPhone,
                shipToCompanyName,
                shipToAddress,
                shipToContactPerson,
                shipToPhone,
                portOfLoading != null ? portOfLoading : "Incheon, Korea",
                finalDestination,
                carrier,
                carrierName,
                freightDate,
                lcNo,
                lcDate,
                lcIssuingBank,
                shipmentType,
                tradeTerms,
                originDescription,
                additionalRemarks,
                currency != null ? currency : "USD"
        );
    }


    // ========== 박스 관리 메서드 ==========

    /**
     * 박스 추가
     */
    public void addBoxItem(ShipmentBoxItem boxItem) {
        boxItems.add(boxItem);
        boxItem.assignToShipment(this);
        recalculateTotalBoxCount();
    }

    /**
     * 박스 제거
     */
    public void removeBoxItem(ShipmentBoxItem boxItem) {
        boxItems.remove(boxItem);
        boxItem.assignToShipment(null);
        recalculateTotalBoxCount();
    }

    /**
     * 모든 박스 제거
     */
    public void clearBoxItems() {
        boxItems.clear();
        this.totalBoxCount = 0;
    }

    /**
     * 전체 박스 수량 재계산
     */
    private void recalculateTotalBoxCount() {
        this.totalBoxCount = boxItems.stream()
                .mapToInt(ShipmentBoxItem::getQuantity)
                .sum();
    }

    // ========== 제품 관리 메서드 ==========

    /**
     * 제품 추가
     */
    public void addItem(ShipmentItem item) {
        items.add(item);
        item.assignToShipment(this);
        recalculateTotals();
    }

    /**
     * 제품 제거
     */
    public void removeItem(ShipmentItem item) {
        items.remove(item);
        item.assignToShipment(null);
        recalculateTotals();
    }

    /**
     * 모든 제품 제거
     */
    public void clearItems() {
        items.clear();
        this.totalQuantity = 0;
        this.totalAmount = BigDecimal.ZERO;
        this.totalNetWeight = BigDecimal.ZERO;
        this.totalGrossWeight = BigDecimal.ZERO;
        this.totalCbm = BigDecimal.ZERO;
    }

    /**
     * 전체 수량 및 금액 재계산
     */
    private void recalculateTotals() {
        this.totalQuantity = items.stream()
                .mapToInt(ShipmentItem::getQuantity)
                .sum();

        this.totalAmount = items.stream()
                .map(ShipmentItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalNetWeight = items.stream()
                .map(ShipmentItem::getNetWeight)
                .filter(weight -> weight != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalGrossWeight = items.stream()
                .map(ShipmentItem::getGrossWeight)
                .filter(weight -> weight != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.totalCbm = items.stream()
                .map(ShipmentItem::getCbm)
                .filter(cbm -> cbm != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 수동으로 합계 재계산 (외부 호출용)
     */
    public void updateTotals() {
        recalculateTotals();
        recalculateTotalBoxCount();
    }

    // ========== 운송업체 관리 ==========

    /**
     * 운송업체 설정
     */
    public void assignCarrier(Carrier carrier, String carrierName) {
        this.carrier = carrier;
        this.carrierName = carrierName;
    }


    // ========== 기본 정보 업데이트 ==========

    /**
     * Invoice 기본 정보 수정
     */
    public void updateInvoiceInfo(LocalDate invoiceDate, LocalDate freightDate) {
        this.invoiceDate = invoiceDate;
        this.freightDate = freightDate;
    }

    /**
     * Shipper 정보 수정
     */
    public void updateShipperInfo(String shipperCompanyName, String shipperAddress,
                                  String shipperContactPerson, String shipperPhone) {
        this.shipperCompanyName = shipperCompanyName;
        this.shipperAddress = shipperAddress;
        this.shipperContactPerson = shipperContactPerson;
        this.shipperPhone = shipperPhone;
    }

    /**
     * Sold To 정보 수정
     */
    public void updateSoldToInfo(String soldToCompanyName, String soldToAddress,
                                 String soldToContactPerson, String soldToPhone) {
        this.soldToCompanyName = soldToCompanyName;
        this.soldToAddress = soldToAddress;
        this.soldToContactPerson = soldToContactPerson;
        this.soldToPhone = soldToPhone;
    }

    /**
     * Ship To 정보 수정
     */
    public void updateShipToInfo(String shipToCompanyName, String shipToAddress,
                                 String shipToContactPerson, String shipToPhone) {
        this.shipToCompanyName = shipToCompanyName;
        this.shipToAddress = shipToAddress;
        this.shipToContactPerson = shipToContactPerson;
        this.shipToPhone = shipToPhone;
    }

    /**
     * 운송 정보 수정
     */
    public void updateShippingInfo(String portOfLoading, String finalDestination) {
        this.portOfLoading = portOfLoading;
        this.finalDestination = finalDestination;
    }

    /**
     * Remark 정보 수정
     */
    public void updateRemarks(ShipmentType shipmentType, TradeTerms tradeTerms,
                              String originDescription, String additionalRemarks) {
        this.shipmentType = shipmentType;
        this.tradeTerms = tradeTerms;
        this.originDescription = originDescription;
        this.additionalRemarks = additionalRemarks;
    }

    /**
     * 신용장 정보 수정
     */
    public void updateLcInfo(String lcNo, LocalDate lcDate, String lcIssuingBank) {
        this.lcNo = lcNo;
        this.lcDate = lcDate;
        this.lcIssuingBank = lcIssuingBank;
    }
}
