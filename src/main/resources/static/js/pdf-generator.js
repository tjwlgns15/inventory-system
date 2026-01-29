/**
 * 선적 문서 PDF 생성 모듈
 * jsPDF와 jsPDF-AutoTable 라이브러리 필요
 */

class ShipmentPDFGenerator {
    constructor(shipmentData) {
        this.data = shipmentData;
        console.log(shipmentData);
    }

    /**
     * COMMERCIAL INVOICE PDF 생성
     */
    generateCommercialInvoice() {
        const { jsPDF } = window.jspdf;
        const doc = new jsPDF('l', 'mm', 'a4'); // 'l' = landscape (가로)

        this._addCommercialInvoicePage(doc);

        doc.save(`${this.data.invoiceNumber}_Commercial_Invoice.pdf`);
    }

    /**
     * PACKING LIST PDF 생성
     */
    generatePackingList() {
        const { jsPDF } = window.jspdf;
        const doc = new jsPDF('l', 'mm', 'a4'); // 'l' = landscape (가로)

        this._addPackingListPage(doc);

        doc.save(`${this.data.invoiceNumber}_Packing_List.pdf`);
    }

    /**
     * 통합 PDF 생성 (Commercial Invoice + Packing List)
     */
    generateCombinedPDF() {
        const { jsPDF } = window.jspdf;
        const doc = new jsPDF('l', 'mm', 'a4'); // 'l' = landscape (가로)

        // 1페이지: Commercial Invoice
        this._addCommercialInvoicePage(doc);

        // 2페이지: Packing List
        doc.addPage();
        this._addPackingListPage(doc);

        doc.save(`${this.data.invoiceNumber}_Combined.pdf`);
    }

    /**
     * Commercial Invoice 페이지 추가
     */
    _addCommercialInvoicePage(doc) {
        const marginLeft = 10;
        const marginRight = 10;
        const pageWidth = 297;  // A4 가로: 297mm
        const pageHeight = 210; // A4 가로: 210mm
        const contentWidth = pageWidth - marginLeft - marginRight;

        let currentY = 10;

        // 헤더 - 파란색 배경
        doc.setFillColor(255, 255, 255);
        doc.rect(marginLeft, currentY, contentWidth, 10, 'F');

        doc.setTextColor(0, 0, 0);
        doc.setFontSize(14);
        doc.setFont(undefined, 'bold');
        doc.text('Solmith Co., Ltd.', marginLeft, currentY + 6.5);
        doc.text('COMMERCIAL INVOICE', pageWidth - marginRight - 75, currentY + 6.5);

        currentY += 7;
        doc.line(marginLeft, currentY, pageWidth - marginRight, currentY);

        currentY += 7;
        doc.setTextColor(0, 0, 0);
        doc.setFontSize(9);
        doc.setFont(undefined, 'normal');

        // 가로 방향: 왼쪽 65%, 오른쪽 35%
        const leftColWidth = 180;
        const rightColX = marginLeft + leftColWidth + 5;

        // 1. Shipper/Exporter & Invoice No.
        currentY = this._addShipperSection(doc, marginLeft, rightColX, currentY, pageWidth, marginRight);
        doc.line(marginLeft, currentY, pageWidth - marginRight, currentY);
        currentY += 4;

        // 2. Sold To & L/C Issuing Bank
        currentY = this._addSoldToSection(doc, marginLeft, rightColX, currentY, leftColWidth);
        doc.line(marginLeft, currentY, pageWidth - marginRight, currentY);
        currentY += 4;

        // 3. Ship To & Remarks
        currentY = this._addShipToSection(doc, marginLeft, rightColX, currentY, leftColWidth, contentWidth);
        doc.line(marginLeft, currentY, rightColX - 3, currentY);
        currentY += 4;

        // 4. Port of Loading & Final Destination
        currentY = this._addPortSection(doc, marginLeft, leftColWidth, currentY);
        currentY += 4;

        // 6. Carrier & Freight on or about
        currentY = this._addCarrierSection(doc, marginLeft, leftColWidth, currentY, pageWidth, marginRight);
        doc.line(marginLeft, currentY, pageWidth - marginRight, currentY);
        currentY += 5;

        // 제품 테이블
        this._addCommercialInvoiceTable(doc, currentY);

        // 서명란
        this._addSignature(doc, pageWidth, marginRight);
    }

    /**
     * Packing List 페이지 추가
     */
    _addPackingListPage(doc) {
        const marginLeft = 10;
        const marginRight = 10;
        const pageWidth = 297;  // A4 가로: 297mm
        const pageHeight = 210; // A4 가로: 210mm
        const contentWidth = pageWidth - marginLeft - marginRight;

        let currentY = 10;

        // 헤더
        doc.setFillColor(255, 255, 255);
        doc.rect(marginLeft, currentY, contentWidth, 10, 'F');

        doc.setTextColor(0, 0, 0);
        doc.setFontSize(14);
        doc.setFont(undefined, 'bold');
        doc.text('Solmith Co., Ltd.', marginLeft, currentY + 6.5);
        doc.text('PACKING LIST', pageWidth - marginRight - 55, currentY + 6.5);

        currentY += 7;
        doc.line(marginLeft, currentY, pageWidth - marginRight, currentY);

        currentY += 7;
        doc.setTextColor(0, 0, 0);
        doc.setFontSize(9);
        doc.setFont(undefined, 'normal');

        const leftColWidth = 180;
        const rightColWidth = contentWidth - leftColWidth - 5;
        const rightColX = marginLeft + leftColWidth + 5;

        // 섹션들 추가
        const shipperEndY = this._addShipperSectionPacking(doc, marginLeft, rightColX, currentY);
        currentY = shipperEndY;
        doc.line(marginLeft, currentY, pageWidth - marginRight, currentY);
        currentY += 4;

        // 2.Customer & 9.Remarks 섹션
        const customerStartY = currentY;
        const customerEndY = this._addCustomerSection(doc, marginLeft, rightColX, currentY, leftColWidth, rightColWidth);
        currentY = customerEndY;
        doc.line(marginLeft, currentY, rightColX - 3, currentY);
        currentY += 4;

        // 3.Ship to 섹션
        const shipToStartY = currentY;
        const shipToEndY = this._addShipToSectionPacking(doc, marginLeft, rightColX, currentY, leftColWidth);
        currentY = shipToEndY;

        doc.line(rightColX - 3, customerStartY, rightColX - 3, shipToEndY);
        doc.line(marginLeft, currentY, rightColX - 3, currentY);
        currentY += 4;

        currentY = this._addPortSection(doc, marginLeft, leftColWidth, currentY);
        currentY += 4;

        currentY = this._addCarrierSection(doc, marginLeft, leftColWidth, currentY, pageWidth, marginRight);
        doc.line(marginLeft, currentY, pageWidth - marginRight, currentY);
        currentY += 5;

        // Packing List 테이블
        this._addPackingListTable(doc, currentY);

        // 서명란
        this._addSignature(doc, pageWidth, marginRight);
    }

    /**
     * Shipper 섹션 (Commercial Invoice용)
     */
    _addShipperSection(doc, marginLeft, rightColX, startY, pageWidth, marginRight) {
        let currentY = startY;
        const sectionStartY = startY;

        doc.setFont(undefined, 'bold');
        doc.text('1.Shipper / Exporter', marginLeft, currentY);
        doc.text('8.Invoice No. and Date', rightColX, currentY);
        doc.setFont(undefined, 'normal');
        currentY += 5;

        doc.text(this.data.shipperCompanyName, marginLeft + 2, currentY);

        doc.text(this.data.invoiceNumber, rightColX + 2, currentY);
        doc.text(this.data.invoiceDate, rightColX + 50, currentY);
        currentY += 1;

        doc.line(rightColX - 3, currentY, pageWidth - marginRight, currentY);
        currentY += 4;

        doc.text(this.data.shipperAddress, marginLeft + 2, currentY);
        currentY += 4;

        doc.text(this.data.shipperContactPerson + "\t" + this.data.shipperPhone, marginLeft + 2, currentY);
        // doc.text(this.data.shipperPhone, marginLeft + 90, currentY);

        let rightY = startY + 9;
        doc.setFont(undefined, 'bold');
        doc.text('9.No. & Date of L/C', rightColX, rightY);
        doc.setFont(undefined, 'normal');
        rightY += 5;

        doc.text(this.data.lcNo || '', rightColX + 2, rightY);
        doc.text(this.data.lcDate || '', rightColX + 50, rightY);
        currentY += 5;

        console.log(sectionStartY, currentY);
        // 좌우 구분 세로선 추가
        doc.line(rightColX - 3, sectionStartY - 4, rightColX - 3, currentY);

        return currentY;
    }

    /**
     * Shipper 섹션 (Packing List용)
     */
    _addShipperSectionPacking(doc, marginLeft, rightColX, startY) {
        let currentY = startY;
        const sectionStartY = startY;
        const pageWidth = 297;
        const marginRight = 10;

        doc.setFont(undefined, 'bold');
        doc.text('1.Shipper / Exporter', marginLeft, currentY);
        doc.text('8.Invoice No. and Date', rightColX, currentY);
        doc.setFont(undefined, 'normal');
        currentY += 5;

        // 8번 영역 시작 Y 위치
        const invoice8StartY = currentY;

        doc.text(this.data.shipperCompanyName, marginLeft + 2, currentY);
        doc.text(this.data.invoiceNumber, rightColX + 2, currentY);
        doc.text(this.data.invoiceDate, rightColX + 50, currentY);
        currentY += 4;

        doc.text(this.data.shipperAddress, marginLeft + 2, currentY);
        currentY += 4;

        doc.text(this.data.shipperContactPerson + "\t" + this.data.shipperPhone, marginLeft + 2, currentY);
        // doc.text(this.data.shipperPhone, marginLeft + 90, currentY);
        currentY += 5;

        // 좌우 구분 세로선 (1번과 8번 사이)
        doc.line(rightColX - 3, sectionStartY - 4, rightColX - 3, currentY);

        return currentY;
    }

    /**
     * Sold To 섹션(Invoice)
     */
    _addSoldToSection(doc, marginLeft, rightColX, startY, leftColWidth) {
        let currentY = startY;
        const sectionStartY = startY;

        doc.setFont(undefined, 'bold');
        doc.text('2.Sold to', marginLeft, currentY);
        doc.text('10.L/C Issuing Bank', rightColX, currentY);
        doc.setFont(undefined, 'normal');
        currentY += 4;

        // Sold To 정보 - 긴 텍스트는 splitTextToSize 사용
        let leftY = currentY;
        const fiscalText = doc.splitTextToSize(
            this.data.soldToCompanyName,
            leftColWidth - 5
        );
        fiscalText.forEach(line => {
            doc.text(line, marginLeft + 2, leftY);
            leftY += 4;
        });

        const addressLines = doc.splitTextToSize(this.data.soldToAddress || '', leftColWidth - 5);
        addressLines.forEach(line => {
            doc.text(line, marginLeft + 2, leftY);
            leftY += 4;
        });

        doc.text(this.data.soldToContactPerson + "\t" + this.data.soldToPhone, marginLeft + 2, leftY);
        // doc.text(this.data.soldToPhone || '', marginLeft + 90, leftY);
        leftY += 4;

        // L/C Issuing Bank (오른쪽)
        let rightY = currentY;
        if (this.data.lcIssuingBank) {
            doc.text(this.data.lcIssuingBank, rightColX + 2, rightY);
            rightY += 4;
        }

        currentY = Math.max(leftY, rightY);

        // 좌우 구분 세로선 추가
        doc.line(rightColX - 3, sectionStartY - 4, rightColX - 3, currentY);

        return currentY;
    }

    /**
     * Customer 섹션 (Packing List용)
     */
    _addCustomerSection(doc, marginLeft, rightColX, startY, leftColWidth, rightColWidth) {
        let currentY = startY;
        const sectionStartY = startY;

        doc.setFont(undefined, 'bold');
        doc.text('2.Customer', marginLeft, currentY);
        doc.text('9.Remarks', rightColX, currentY);
        doc.setFont(undefined, 'normal');
        currentY += 4;

        // Customer 정보
        let leftY = currentY;
        const fiscalText = doc.splitTextToSize(
            this.data.soldToCompanyName,
            leftColWidth - 5
        );

        fiscalText.forEach(line => {
            doc.text(line, marginLeft + 2, leftY);
            leftY += 4;
        });

        const addressLines = doc.splitTextToSize(this.data.soldToAddress || '', leftColWidth - 5);
        addressLines.forEach(line => {
            doc.text(line, marginLeft + 2, leftY);
            leftY += 4;
        });

        doc.text(this.data.soldToContactPerson + "\t" + this.data.soldToPhone, marginLeft + 2, leftY);
        // doc.text(this.data.soldToPhone || '', marginLeft + 90, leftY);
        leftY += 4;

        // Remarks
        let rightY = currentY;
        const remarkLines = [
            this.data.shipmentTypeDisplayEn || '',
            this.data.tradeTermsDisplayEn || ''
        ];

        if (this.data.originDescription) {
            const originLines = doc.splitTextToSize(
                this.data.originDescription,
                rightColWidth - 4
            );
            remarkLines.push(...originLines);
        }

        if (this.data.additionalRemarks) {
            const additionalLines = doc.splitTextToSize(
                this.data.additionalRemarks,
                rightColWidth - 4
            );
            remarkLines.push(...additionalLines);
        }

        remarkLines.forEach(line => {
            if (line) {
                doc.text(line, rightColX + 2, rightY);
                rightY += 4;
            }
        });

        currentY = leftY;

        // 좌우 구분 세로선 추가
        doc.line(rightColX - 3, sectionStartY - 4, rightColX - 3, currentY);

        return currentY;
    }

    /**
     * Ship To 섹션
     */
    _addShipToSection(doc, marginLeft, rightColX, startY, leftColWidth, contentWidth) {
        let currentY = startY;
        const sectionStartY = startY;
        const rightColWidth = contentWidth - leftColWidth - 5;

        doc.setFont(undefined, 'bold');
        doc.text('3.Ship to', marginLeft, currentY);
        doc.text('11.Remarks', rightColX, currentY);
        doc.setFont(undefined, 'normal');
        currentY += 4;

        // Ship To
        let leftY = currentY;
        doc.text(this.data.shipToCompanyName || '', marginLeft + 2, leftY);
        leftY += 4;

        const shipAddressLines = doc.splitTextToSize(this.data.shipToAddress || '', leftColWidth - 5);
        shipAddressLines.forEach(line => {
            doc.text(line, marginLeft + 2, leftY);
            leftY += 4;
        });

        doc.text(this.data.shipToContactPerson + "\t" + this.data.shipToPhone, marginLeft + 2, leftY);
        // doc.text(this.data.shipToPhone || '', marginLeft + 90, leftY);
        leftY += 4;

        // Remarks
        let rightY = currentY;
        const remarkLines = [
            this.data.shipmentTypeDisplayEn || '',
            this.data.tradeTermsDisplayEn || ''
        ];

        if (this.data.originDescription) {
            const originLines = doc.splitTextToSize(
                this.data.originDescription,
                rightColWidth - 4
            );
            remarkLines.push(...originLines);
        }

        if (this.data.additionalRemarks) {
            const additionalLines = doc.splitTextToSize(
                this.data.additionalRemarks,
                rightColWidth - 4
            );
            remarkLines.push(...additionalLines);
        }

        remarkLines.forEach(line => {
            if (line) {
                doc.text(line, rightColX + 2, rightY);
                rightY += 4;
            }
        });

        currentY = leftY;

        // 좌우 구분 세로선 추가
        doc.line(rightColX - 3, sectionStartY - 4, rightColX - 3, currentY);

        return currentY;
    }

    /**
     * Ship To 섹션 (Packing List용 - Remarks 없음)
     */
    _addShipToSectionPacking(doc, marginLeft, rightColX, startY, leftColWidth) {
        let currentY = startY;
        const sectionStartY = startY;


        doc.setFont(undefined, 'bold');
        doc.text('3.Ship to', marginLeft, currentY);
        doc.setFont(undefined, 'normal');
        currentY += 4;

        doc.text(this.data.shipToCompanyName || '', marginLeft + 2, currentY);
        currentY += 4;

        const shipAddressLines = doc.splitTextToSize(this.data.shipToAddress || '', leftColWidth - 5);
        shipAddressLines.forEach(line => {
            doc.text(line, marginLeft + 2, currentY);
            currentY += 4;
        });

        doc.text(this.data.shipToContactPerson + "\t" + this.data.shipToPhone, marginLeft + 2, currentY);
        // doc.text(this.data.shipToPhone || '', marginLeft + 90, currentY);
        currentY += 4;

        return currentY;
    }

    /**
     * Port 섹션
     */
    _addPortSection(doc, marginLeft, leftColWidth, startY) {
        let currentY = startY;
        const sectionStartY = startY;
        const rightColX = marginLeft + leftColWidth + 5;

        doc.setFont(undefined, 'bold');
        doc.text('4.Port of Loading', marginLeft, currentY);
        doc.text('5.Final Destination', marginLeft + 90, currentY);
        doc.setFont(undefined, 'normal');
        currentY += 4;

        doc.text(this.data.portOfLoading || '', marginLeft + 2, currentY);
        doc.text(this.data.finalDestination || '', marginLeft + 92, currentY);
        currentY += 4;

        doc.line(marginLeft + 87, sectionStartY - 4, marginLeft + 87, currentY);

        // 세로선 (3.Ship to와 11.Remarks 사이 세로선과 같은 x축)
        doc.line(rightColX - 3, sectionStartY - 4, rightColX - 3, currentY);

        // 가로선 (왼쪽 끝부터 세로선까지만)
        doc.line(marginLeft, currentY, rightColX - 3, currentY);

        return currentY;
    }

    /**
     * Carrier 섹션
     */
    _addCarrierSection(doc, marginLeft, leftColWidth, startY, pageWidth, marginRight) {
        let currentY = startY;
        const sectionStartY = startY;
        const rightColX = marginLeft + leftColWidth + 5;

        doc.setFont(undefined, 'bold');
        doc.text('6.Carrier', marginLeft, currentY);
        doc.text('7.Freight on or about', marginLeft + 90, currentY);
        doc.setFont(undefined, 'normal');
        currentY += 4;

        doc.text(this.data.carrierName || '', marginLeft + 2, currentY);
        doc.text(this.data.freightDate || '', marginLeft + 92, currentY);
        currentY += 4;

        doc.line(marginLeft + 87, sectionStartY - 4, marginLeft + 87, currentY);

        // 세로선 (3.Ship to와 11.Remarks 사이 세로선과 같은 x축)
        doc.line(rightColX - 3, sectionStartY - 4, rightColX - 3, currentY);

        return currentY;
    }

    /**
     * Commercial Invoice 테이블
     */
    _addCommercialInvoiceTable(doc, startY) {
        const tableData = [];

        // 박스 데이터와 제품 데이터를 순서대로 배치
        const maxRows = Math.max(
            (this.data.boxes || []).length,
            (this.data.items || []).length
        );

        for (let i = 0; i < maxRows; i++) {
            const box = this.data.boxes && this.data.boxes[i] ? this.data.boxes[i] : null;
            const item = this.data.items && this.data.items[i] ? this.data.items[i] : null;

            const row = [];

            // Marks 3개 서브열
            if (box) {
                row.push(box.title || 'Box');
                row.push(box.dimensionString || `${box.width}x${box.length}x${box.height}cm`);
                row.push(`${box.quantity}box`);
            } else {
                row.push('', '', '');
            }

            // 제품 정보 열
            if (item) {
                row.push(item.sequence);
                row.push(item.productCode || item.model);
                row.push(item.productDescription || item.description);
                row.push(item.hsCode || '');
                row.push(`${item.quantity}${item.unit}`);
                row.push(this.data.currency);
                row.push(this._formatNumberWithComma(item.unitPrice));
                row.push(this.data.currency);
                row.push(this._formatNumberWithComma(item.amount));
            } else {
                row.push('', '', '', '', '', '', '', '', '');
            }

            tableData.push(row);
        }

        // Total 행
        const totalBoxes = this.data.boxes && this.data.boxes.length > 0
            ? this.data.boxes.reduce((sum, box) => sum + (box.quantity || 0), 0)
            : 0;

        tableData.push([
            { content: 'Total :'+ `${totalBoxes}` + 'box', colSpan: 3 },
            '',
            '',
            '',
            '',
            `${this.data.totalQuantity}EA`,
            '',
            '',
            this.data.currency,
            this._formatNumberWithComma(this.data.totalAmount)
        ]);

        doc.autoTable({
            startY: startY,
            margin: { left: 10, right: 10 },
            head: [[
                { content: '12.Marks and No.of PKGS', colSpan: 3 },
                '13.No.',
                '14.Model',
                '15.Description of Goods',
                '16.H.S.Code',
                '17.Quantity',
                { content: '18.Unit Price', colSpan: 2 },
                { content: '19.Amount', colSpan: 2 }
            ]],
            body: tableData,
            theme: 'grid',
            styles: {
                fontSize: 9,
                cellPadding: 1.5,
                lineColor: [0, 0, 0],
                lineWidth: 0.2,
                halign: 'center',
                valign: 'middle'
            },
            headStyles: {
                fontSize: 8,
                fillColor: [255, 255, 255],
                textColor: [0, 0, 0],
                fontStyle: 'bold',
                halign: 'center',
                valign: 'middle'
            },
            columnStyles: {
                0: { cellWidth: 12, halign: 'center' },    // Box 명
                1: { cellWidth: 21, halign: 'center' },    // 사이즈
                2: { cellWidth: 12, halign: 'center' },    // Box 수량
                3: { cellWidth: 13, halign: 'center' },                // No.
                4: { cellWidth: 34, halign: 'left' },                  // Model
                5: { cellWidth: 70, halign: 'left' },                  // Description
                6: { cellWidth: 27, halign: 'center' },                // HS Code
                7: { cellWidth: 19, halign: 'center' },                // Quantity
                8: { cellWidth: 11, halign: 'center' },                // Currency
                9: { cellWidth: 22, halign: 'right' },                 // Unit Price
                10: { cellWidth: 11, halign: 'center' },               // Currency
                11: { cellWidth: 25, halign: 'right' }                 // Amount
            },
            didParseCell: (data) => {
                // Total 행 스타일
                if (data.row.index === tableData.length - 1) {
                    data.cell.styles.fontStyle = 'bold';
                    data.cell.styles.fillColor = [240, 240, 240];
                }
            }
        });
    }

    /**
     * Packing List 테이블
     */
    _addPackingListTable(doc, startY) {
        const packingData = [];

        // 박스 데이터와 제품 데이터를 순서대로 배치
        const maxRows = Math.max(
            (this.data.boxes || []).length,
            (this.data.items || []).length
        );

        for (let i = 0; i < maxRows; i++) {
            const box = this.data.boxes && this.data.boxes[i] ? this.data.boxes[i] : null;
            const item = this.data.items && this.data.items[i] ? this.data.items[i] : null;

            const row = [];

            // Marks 3개 서브열
            if (box) {
                row.push(box.title || 'Box');
                row.push(box.dimensionString || `${box.width}x${box.length}x${box.height}cm`);
                row.push(`${box.quantity}box`);
            } else {
                row.push('', '', '');
            }

            // 제품 정보 열
            if (item) {
                row.push(item.sequence);
                row.push(item.productCode || item.model);
                row.push(item.productDescription || item.description);
                row.push(`${item.quantity}${item.unit}`);
                row.push(item.netWeight ? `${this._formatWeight(item.netWeight)}kg` : '');
                row.push(item.grossWeight ? `${this._formatWeight(item.grossWeight)}kg` : '');
                row.push(item.cbm ? `${this._formatWeight(item.cbm)}m³` : '');
            } else {
                row.push('', '', '', '', '', '', '');
            }

            packingData.push(row);
        }

        // Total 행
        const totalBoxes = this.data.boxes && this.data.boxes.length > 0
            ? this.data.boxes.reduce((sum, box) => sum + (box.quantity || 0), 0)
            : 0;

        packingData.push([
            { content: 'Total :'+ `${totalBoxes}` + 'box', colSpan: 3 },
            '',
            '',
            '',
            `${this.data.totalQuantity}EA`,
            this.data.totalNetWeight ? `${this._formatWeight(this.data.totalNetWeight)}kg` : '',
            this.data.totalGrossWeight ? `${this._formatWeight(this.data.totalGrossWeight)}kg` : '',
            this.data.totalCbm ? `${this._formatWeight(this.data.totalCbm)}m³` : ''
        ]);

        head: [[
            { content: '12.Marks and No.of PKGS', colSpan: 3 },
            '13.No.',
            '14.Model',
            '15.Description of Goods',
            '16.H.S.Code',
            '17.Quantity',
            { content: '18.Unit Price', colSpan: 2 },
            { content: '19.Amount', colSpan: 2 }
        ]],

        doc.autoTable({
            startY: startY,
            margin: { left: 10, right: 10 },
            head: [[
                { content: '10.Marks and No.of PKGS', colSpan: 3 },
                '11.No.',
                '12.Model',
                '13.Description of Goods',
                '14.Quantity',
                '15.Net Weight',
                '16.Gross Weight',
                '17.Measurement'
            ]],
            body: packingData,
            theme: 'grid',
            styles: {
                fontSize: 9,
                cellPadding: 1.5,
                lineColor: [0, 0, 0],
                lineWidth: 0.2,
                halign: 'center',
                valign: 'middle'
            },
            headStyles: {
                fontSize: 8,
                fillColor: [255, 255, 255],
                textColor: [0, 0, 0],
                fontStyle: 'bold',
                halign: 'center',
                valign: 'middle'
            },
            columnStyles: {
                0: { cellWidth: 14, halign: 'center' },   // Box 명
                1: { cellWidth: 24, halign: 'center' },   // 사이즈
                2: { cellWidth: 14, halign: 'center' },   // Box 수량
                3: { cellWidth: 12, halign: 'center' },                // No.
                4: { cellWidth: 35, halign: 'left' },                  // Model
                5: { cellWidth: 77, halign: 'left' },                 // Description
                6: { cellWidth: 25, halign: 'center' },                // Quantity
                7: { cellWidth: 25, halign: 'center' },                // Net Weight
                8: { cellWidth: 25, halign: 'center' },                // Gross Weight
                9: { cellWidth: 25, halign: 'center' }                 // Measurement
            },
            didParseCell: (data) => {
                // Total 행 스타일
                if (data.row.index === packingData.length - 1) {
                    data.cell.styles.fontStyle = 'bold';
                    data.cell.styles.fillColor = [240, 240, 240];
                }
            }
        });
    }

    /**
     * 서명란 추가 (이미지 사용)
     */
    _addSignature(doc, pageWidth, marginRight) {
        const finalY = doc.lastAutoTable.finalY + 15;

        doc.setFontSize(9);
        doc.setFont(undefined, 'normal');
        doc.text('Signed by', pageWidth - marginRight - 90, finalY + 10);

        // 서명 라인
        doc.setDrawColor(0, 0, 0);
        doc.line(pageWidth - marginRight - 70, finalY + 10, pageWidth - marginRight - 10, finalY + 10);

        // 서명 이미지 또는 텍스트
        if (this.signatureImage) {
            try {
                doc.addImage(
                    this.signatureImage,
                    'PNG',
                    pageWidth - marginRight - 65,
                    finalY - 1,
                    50,  // width
                    10    // height
                );
            } catch (error) {
                console.error('서명 이미지 추가 실패:', error);
                doc.setFont(undefined, 'italic');
                doc.setFontSize(12);
                doc.text('Seung C. Shim', pageWidth - marginRight - 60, finalY + 8);
            }
        } else {
            doc.setFont(undefined, 'italic');
            doc.setFontSize(12);
            doc.text('Seung C. Shim', pageWidth - marginRight - 60, finalY + 8);
        }
    }

    /**
     * 서명 이미지 설정
     * @param {string} imageDataUrl - Base64 인코딩된 이미지 데이터 URL
     */
    setSignatureImage(imageDataUrl) {
        this.signatureImage = imageDataUrl;
    }

    /**
     * 숫자를 천단위 콤마 형식으로 변환 (소수점 자릿수 지정)
     * @param {number|string} value - 변환할 숫자 값
     * @param {number} decimalPlaces - 소수점 자릿수 (기본값: 2)
     * @returns {string} 천단위 콤마가 적용된 문자열
     */
    _formatNumberWithComma(value, decimalPlaces = 2) {
        if (value === null || value === undefined || value === '') {
            return '';
        }

        const number = typeof value === 'number' ? parseFloat(value) : value;

        if (isNaN(number)) {
            return '';
        }

        // 천단위 콤마 적용
        return number.toLocaleString('en-US', {
            minimumFractionDigits: decimalPlaces,
            maximumFractionDigits: decimalPlaces
        });
    }

    /**
     * 중량을 소수점 2자리까지 포맷팅
     * @param {number|string} weight - 중량 값
     * @returns {string} 소수점 2자리로 포맷된 문자열
     */
    _formatWeight(weight) {
        if (weight === null || weight === undefined || weight === '') {
            return '';
        }

        const number = typeof weight === 'string' ? parseFloat(weight) : weight;

        if (isNaN(number)) {
            return '';
        }

        return number.toFixed(2);
    }
}

// 모듈 내보내기
if (typeof module !== 'undefined' && module.exports) {
    module.exports = ShipmentPDFGenerator;
}