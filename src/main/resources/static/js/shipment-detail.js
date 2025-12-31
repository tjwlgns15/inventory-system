// API Base URL
const API_BASE_URL = '/api';

// 현재 선적 문서 ID
let currentShipmentId = null;
let currentShipment = null;

// 서명 이미지 (전역 변수)
let signatureImage = null;

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', () => {
    // URL에서 ID 추출
    const pathParts = window.location.pathname.split('/');
    const shipmentIndex = pathParts.indexOf('shipments');

    if (shipmentIndex !== -1 && pathParts[shipmentIndex + 1]) {
        currentShipmentId = parseInt(pathParts[shipmentIndex + 1]);
        loadShipmentDetail();
    } else {
        showError('유효하지 않은 선적 문서 ID입니다.');
    }

    setupEventListeners();
    loadSignatureImage(); // 서명 이미지 로드
});

// 서명 이미지 로드
async function loadSignatureImage() {
    try {
        const response = await fetch('/images/signature.png');
        if (!response.ok) {
            console.warn('서명 이미지를 찾을 수 없습니다. 텍스트 서명을 사용합니다.');
            return;
        }

        const blob = await response.blob();
        const reader = new FileReader();

        reader.onload = (event) => {
            signatureImage = event.target.result;
            console.log('✅ 서명 이미지 로드 완료');
        };

        reader.readAsDataURL(blob);
    } catch (error) {
        console.warn('서명 이미지 로드 실패. 텍스트 서명을 사용합니다.', error);
    }
}

// 이벤트 리스너 설정
function setupEventListeners() {

    // // PDF 생성 버튼
    // document.getElementById('generateInvoiceBtn').addEventListener('click', () => {
    //     if (!currentShipment) {
    //         alert('선적 문서 데이터를 불러오는 중입니다.');
    //         return;
    //     }
    //
    //     const pdfGenerator = new ShipmentPDFGenerator(currentShipment);
    //
    //     // 서명 이미지가 있으면 설정
    //     if (signatureImage) {
    //         pdfGenerator.setSignatureImage(signatureImage);
    //     }
    //
    //     pdfGenerator.generateCommercialInvoice();
    // });
    //
    // document.getElementById('generatePackingBtn').addEventListener('click', () => {
    //     if (!currentShipment) {
    //         alert('선적 문서 데이터를 불러오는 중입니다.');
    //         return;
    //     }
    //
    //     const pdfGenerator = new ShipmentPDFGenerator(currentShipment);
    //
    //     // 서명 이미지가 있으면 설정
    //     if (signatureImage) {
    //         pdfGenerator.setSignatureImage(signatureImage);
    //     }
    //
    //     pdfGenerator.generatePackingList();
    // });

    document.getElementById('generateCombinedBtn').addEventListener('click', () => {
        if (!currentShipment) {
            alert('선적 문서 데이터를 불러오는 중입니다.');
            return;
        }

        const pdfGenerator = new ShipmentPDFGenerator(currentShipment);

        // 서명 이미지가 있으면 설정
        if (signatureImage) {
            pdfGenerator.setSignatureImage(signatureImage);
        }

        pdfGenerator.generateCombinedPDF();
    });

    // 삭제
    document.getElementById('deleteBtn').addEventListener('click', showDeleteModal);
    document.getElementById('closeDeleteModal').addEventListener('click', () => closeModal('deleteModal'));
    document.getElementById('cancelDeleteBtn').addEventListener('click', () => closeModal('deleteModal'));
    document.getElementById('confirmDeleteBtn').addEventListener('click', confirmDelete);

    // 모달 외부 클릭 시 닫기
    document.getElementById('deleteModal').addEventListener('click', (e) => {
        if (e.target.id === 'deleteModal') {
            closeModal('deleteModal');
        }
    });
}

// ===== 데이터 로드 =====

async function loadShipmentDetail() {
    try {
        const response = await fetch(`${API_BASE_URL}/shipments/${currentShipmentId}`);

        if (!response.ok) {
            if (response.status === 404) {
                throw new Error('선적 문서를 찾을 수 없습니다.');
            }
            throw new Error('선적 문서를 불러올 수 없습니다.');
        }

        currentShipment = await response.json();
        renderShipmentDetail();

    } catch (error) {
        console.error('데이터 로드 실패:', error);
        showError(error.message);
    }
}

// ===== 렌더링 =====

function renderShipmentDetail() {
    const shipment = currentShipment;

    // 로딩 숨기고 메인 콘텐츠 표시
    document.getElementById('loadingSection').style.display = 'none';
    document.getElementById('mainContent').style.display = 'block';

    // 헤더
    document.getElementById('invoiceNumber').textContent = `Invoice No: ${shipment.invoiceNumber}`;
    document.getElementById('editBtn').href = `/shipments/${shipment.id}/edit`;

    // 1. Invoice 기본 정보
    document.getElementById('detailInvoiceNumber').textContent = shipment.invoiceNumber;
    document.getElementById('invoiceDate').textContent = formatDate(shipment.invoiceDate);
    document.getElementById('freightDate').textContent = formatDate(shipment.freightDate);
    document.getElementById('yearSequence').textContent = `${shipment.year} / ${shipment.sequence}`;

    // 2. Shipper 정보
    document.getElementById('shipperCompanyName').textContent = shipment.shipperCompanyName || '-';
    document.getElementById('shipperAddress').textContent = shipment.shipperAddress || '-';
    document.getElementById('shipperContactPerson').textContent = shipment.shipperContactPerson || '-';
    document.getElementById('shipperPhone').textContent = shipment.shipperPhone || '-';

    // 3. Sold To 정보
    document.getElementById('soldToCompanyName').textContent = shipment.soldToCompanyName || '-';
    document.getElementById('soldToAddress').textContent = shipment.soldToAddress || '-';
    document.getElementById('soldToContactPerson').textContent = shipment.soldToContactPerson || '-';
    document.getElementById('soldToPhone').textContent = shipment.soldToPhone || '-';

    // 4. Ship To 정보
    document.getElementById('shipToCompanyName').textContent = shipment.shipToCompanyName || '-';
    document.getElementById('shipToAddress').textContent = shipment.shipToAddress || '-';
    document.getElementById('shipToContactPerson').textContent = shipment.shipToContactPerson || '-';
    document.getElementById('shipToPhone').textContent = shipment.shipToPhone || '-';

    // 5. 운송 정보
    document.getElementById('portOfLoading').textContent = shipment.portOfLoading || '-';
    document.getElementById('finalDestination').textContent = shipment.finalDestination || '-';
    document.getElementById('carrierName').textContent = shipment.carrierName || '-';

    // 6. 신용장 정보 (있는 경우만 표시)
    if (shipment.lcNo || shipment.lcDate || shipment.lcIssuingBank) {
        document.getElementById('lcSection').style.display = 'block';
        document.getElementById('lcNo').textContent = shipment.lcNo || '-';
        document.getElementById('lcDate').textContent = formatDate(shipment.lcDate) || '-';
        document.getElementById('lcIssuingBank').textContent = shipment.lcIssuingBank || '-';
    }

    // 7. Remark
    document.getElementById('tradeTerms').textContent =
        `${shipment.tradeTermsDisplay} (${shipment.tradeTermsDisplayEn})`;

    let remarkText = '';
    if (shipment.shipmentTypeDisplay) {
        remarkText += `${shipment.shipmentTypeDisplay} (${shipment.shipmentTypeDisplayEn})\n`;
    }
    if (shipment.tradeTermsDisplay) {
        remarkText += `${shipment.tradeTermsDisplay} (${shipment.tradeTermsDisplayEn})\n`;
    }
    if (shipment.originDescription) {
        remarkText += `\n${shipment.originDescription}\n`;
    }
    if (shipment.additionalRemarks) {
        remarkText += `\n${shipment.additionalRemarks}`;
    }
    document.getElementById('remarkContent').textContent = remarkText || '-';

    // 8. 박스 정보
    if (shipment.boxes && shipment.boxes.length > 0) {
        const boxTableBody = document.getElementById('boxTableBody');
        boxTableBody.innerHTML = shipment.boxes.map(box => `
            <tr>
                <td>${box.sequence}</td>
                <td>${box.title}</td>
                <td>${box.dimensionString}</td>
                <td>${box.weight}</td>
                <td>${box.quantity}</td>
            </tr>
        `).join('');

        document.getElementById('totalBoxCount').textContent = shipment.totalBoxCount || 0;
    } else {
        document.getElementById('boxSection').style.display = 'none';
    }

    // 9. 제품 정보
    const itemTableBody = document.getElementById('itemTableBody');
    itemTableBody.innerHTML = shipment.items.map(item => `
        <tr>
            <td>${item.sequence}</td>
            <td>${item.productCode}</td>
            <td>${item.productDescription || '-'}</td>
            <td>${item.hsCode || '-'}</td>
            <td>${item.unit}</td>
            <td>${item.quantity}</td>
            <td>${formatCurrency(item.unitPrice)}</td>
            <td>${formatCurrency(item.amount)}</td>
            <td>${item.netWeight || '-'}</td>
            <td>${item.grossWeight || '-'}</td>
            <td>${item.cbm || '-'}</td>
        </tr>
    `).join('');

    document.getElementById('totalQuantity').textContent = shipment.totalQuantity || 0;
    document.getElementById('totalAmount').textContent =
        `${shipment.currency} ${formatCurrency(shipment.totalAmount)}`;
    document.getElementById('totalNetWeight').textContent = shipment.totalNetWeight || '-';
    document.getElementById('totalGrossWeight').textContent = shipment.totalGrossWeight || '-';
    document.getElementById('totalCbm').textContent = shipment.totalCbm || '-';

    // 10. 금액 및 합계 중량 정보
    document.getElementById('currency').textContent = shipment.currency || '-';
    document.getElementById('totalNetWeightSummary').textContent = shipment.totalNetWeight || '-';
    document.getElementById('totalGrossWeightSummary').textContent = shipment.totalGrossWeight || '-';
    document.getElementById('totalCbmSummary').textContent = shipment.totalCbm || '-';

    // 11. 생성/수정 정보
    document.getElementById('createdAt').textContent = formatDateTime(shipment.createdAt);
    document.getElementById('updatedAt').textContent = formatDateTime(shipment.updatedAt);
}

// ===== 액션 함수들 =====

function showDeleteModal() {
    document.getElementById('deleteInvoiceNo').textContent = currentShipment.invoiceNumber;
    document.getElementById('deleteModal').classList.add('active');
}

async function confirmDelete() {
    try {
        const response = await fetch(`${API_BASE_URL}/shipments/${currentShipmentId}`, {
            method: 'DELETE'
        });

        if (!response.ok) throw new Error('삭제에 실패했습니다.');

        alert('선적 문서가 삭제되었습니다.');
        window.location.href = '/shipments';

    } catch (error) {
        console.error('삭제 실패:', error);
        alert('오류: ' + error.message);
    }
}

// ===== 유틸리티 함수들 =====

function formatDate(dateString) {
    if (!dateString) return '-';
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    });
}

function formatDateTime(dateTimeString) {
    if (!dateTimeString) return '-';
    const date = new Date(dateTimeString);
    return date.toLocaleString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function formatCurrency(amount) {
    if (!amount && amount !== 0) return '-';
    return parseFloat(amount).toLocaleString('en-US', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

function closeModal(modalId) {
    document.getElementById(modalId).classList.remove('active');
}

function showError(message) {
    document.getElementById('loadingSection').innerHTML = `
        <div style="text-align: center; padding: 60px 20px; color: #718096;">
            <div style="font-size: 4em; margin-bottom: 20px; opacity: 0.5;">⚠️</div>
            <h3 style="color: #4a5568; margin-bottom: 10px;">오류가 발생했습니다</h3>
            <p>${message}</p>
            <a href="/shipments" class="btn btn-primary" style="margin-top: 20px;">목록으로 돌아가기</a>
        </div>
    `;
}