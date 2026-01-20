// API Base URL
const API_BASE_URL = '/api/shipments';

// 상태 관리
let currentPage = 0;
let currentPageSize = 25;
let totalPages = 0;
let totalElements = 0;
let currentShipmentType = '';
let currentKeyword = '';
let currentSortBy = 'createdAt';
let currentDirection = 'desc';

let memoTargetId = null;
let currentShipmentId = null; // 문서 관리용 현재 선적 ID

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', () => {
    initializePage();
});

// 초기화
function initializePage() {
    setupEventListeners();
    loadShipments();
}

// 이벤트 리스너 설정
function setupEventListeners() {
    // 검색어 엔터키
    document.getElementById('searchInput').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            handleSearch();
        }
    });
}


// ===== 데이터 로드 =====

async function loadShipments() {
    try {
        showLoading();

        const params = new URLSearchParams({
            page: currentPage,
            size: currentPageSize,
            sortBy: currentSortBy,
            direction: currentDirection
        });

        if (currentKeyword) {
            params.append('keyword', currentKeyword);
        }

        if (currentShipmentType) {
            params.append('shipmentType', currentShipmentType);
        }

        const response = await fetch(`${API_BASE_URL}/paged?${params}`);
        if (!response.ok) throw new Error('선적 문서 목록을 불러올 수 없습니다.');

        const data = await response.json();

        totalPages = data.totalPages;
        totalElements = data.totalElements;

        renderShipments(data.content);
        renderPagination();

    } catch (error) {
        console.error('데이터 로드 실패:', error);
        showError('데이터를 불러오는 중 오류가 발생했습니다.');
    }
}

// ===== 검색 및 필터링 =====

function handleSearch(event) {
    if (event && event.key !== 'Enter') return;

    currentKeyword = document.getElementById('searchInput').value.trim();
    currentPage = 0;
    loadShipments();
}

function filterByType(type) {
    currentShipmentType = type;
    currentPage = 0;

    // 필터 버튼 스타일 업데이트
    document.querySelectorAll('.filter-btn').forEach(btn => {
        if (btn.getAttribute('data-type') === type) {
            btn.classList.add('active');
            btn.setAttribute('data-type', type);
        } else {
            btn.classList.remove('active');
        }
    });

    loadShipments();
}

function toggleDateSort() {
    currentSortBy = 'invoiceDate';
    currentDirection = currentDirection === 'desc' ? 'asc' : 'desc';

    const sortIcon = document.getElementById('sortIcon');
    sortIcon.textContent = currentDirection === 'desc' ? '▼' : '▲';

    currentPage = 0;
    loadShipments();
}

// ===== 렌더링 =====

function renderShipments(shipments) {
    const tbody = document.getElementById('shipmentTableBody');

    if (shipments.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="12">
                    <div class="empty-state">
                        <div class="empty-state-icon">📭</div>
                        <h3>선적 문서가 없습니다</h3>
                        <p>새 문서를 작성하거나 검색어를 변경해보세요.</p>
                    </div>
                </td>
            </tr>
        `;
        return;
    }

    const rows = shipments.map(shipment => `
        <tr onclick="viewShipmentDetail(${shipment.id})" style="cursor: pointer;">
            <td>${formatDate(shipment.invoiceDate)}</td>
            <td>
                <strong style="color: #667eea;">${escapeHtml(shipment.invoiceNumber)}</strong>
            </td>
            <td>
                <span class="badge ${getTypeBadgeClass(shipment.shipmentType)}">
                    ${escapeHtml(shipment.shipmentTypeDisplay)}
                </span>
            </td>
            <td>
                <div class="font-weight-600">${escapeHtml(shipment.soldToCompanyName)}</div>
            </td>
            <td>${formatProductNames(shipment.items)}</td>
            <td>${formatProductQuantities(shipment.items)}</td>
            <td>
                <div class="font-weight-600">${escapeHtml(shipment.currency)} ${formatCurrency(shipment.totalAmount)}</div>
            </td>
            <td>${escapeHtml(shipment.finalDestination || '-')}</td>
            <td>${formatDate(shipment.freightDate)}</td>
            <td>${escapeHtml(shipment.trackingNumber || '-')}</td>
            <td>${escapeHtml(shipment.exportLicenseNumber || '-')}</td>
            <td onclick="event.stopPropagation();">
                <div class="action-buttons">
                    <button class="btn btn-info" onclick="window.location.href='/shipments/${shipment.id}'">
                        수정
                    </button>
                    <button class="btn btn-info"
                            data-shipment-id="${shipment.id}"
                            data-memo="${escapeHtml(shipment.memo || '')}"
                            onclick="openMemoModalFromButton(this)">메모</button>
                </div>
            </td>
        </tr>
    `).join('');

    tbody.innerHTML = rows;
}

// ===== 페이지네이션 =====

function renderPagination() {
    const paginationControls = document.getElementById('paginationControls');
    const pageInfo = document.getElementById('pageInfo');

    const start = totalElements === 0 ? 0 : currentPage * currentPageSize + 1;
    const end = Math.min((currentPage + 1) * currentPageSize, totalElements);
    pageInfo.textContent = `${start}-${end} / 전체 ${totalElements}개`;

    paginationControls.innerHTML = '';

    // 이전 버튼
    const prevButton = document.createElement('button');
    prevButton.className = 'page-button';
    prevButton.textContent = '‹';
    prevButton.disabled = currentPage === 0;
    prevButton.onclick = () => goToPage(currentPage - 1);
    paginationControls.appendChild(prevButton);

    // 페이지 번호 버튼
    const maxVisiblePages = 5;
    let startPage = Math.max(0, currentPage - Math.floor(maxVisiblePages / 2));
    let endPage = Math.min(totalPages - 1, startPage + maxVisiblePages - 1);

    if (endPage - startPage < maxVisiblePages - 1) {
        startPage = Math.max(0, endPage - maxVisiblePages + 1);
    }

    for (let i = startPage; i <= endPage; i++) {
        const pageButton = document.createElement('button');
        pageButton.className = 'page-button' + (i === currentPage ? ' active' : '');
        pageButton.textContent = i + 1;
        pageButton.onclick = () => goToPage(i);
        paginationControls.appendChild(pageButton);
    }

    // 다음 버튼
    const nextButton = document.createElement('button');
    nextButton.className = 'page-button';
    nextButton.textContent = '›';
    nextButton.disabled = currentPage >= totalPages - 1;
    nextButton.onclick = () => goToPage(currentPage + 1);
    paginationControls.appendChild(nextButton);
}

function goToPage(page) {
    if (page < 0 || page >= totalPages) return;
    currentPage = page;
    loadShipments();
}

function changePageSize() {
    currentPageSize = parseInt(document.getElementById('pageSizeSelect').value);
    currentPage = 0;
    loadShipments();
}

// ===== 상세 정보 모달 =====

/**
 * 선적 상세 정보 조회 및 모달 표시
 */
async function viewShipmentDetail(shipmentId) {
    try {
        const response = await fetch(`${API_BASE_URL}/${shipmentId}`);
        if (!response.ok) throw new Error('선적 상세 정보를 불러오는데 실패했습니다');

        const shipment = await response.json();
        const detailContent = document.getElementById('detailContent');

        detailContent.innerHTML = `
            <div class="detail-form-container">
                <!-- 좌측: 기본 정보 -->
                <div class="detail-left-column">
                    <div class="detail-info">
                        <p><strong>Invoice No:</strong> ${escapeHtml(shipment.invoiceNumber)}</p>
                        <p><strong>Invoice 작성일:</strong> ${formatDate(shipment.invoiceDate)}</p>
                        <p><strong>거래 유형:</strong> ${escapeHtml(shipment.shipmentTypeDisplay)}</p>
                        <p><strong>거래 조건:</strong> ${escapeHtml(shipment.tradeTermsDisplay)}</p>
                        <p><strong>통화:</strong> ${escapeHtml(shipment.currency)}</p>
                        <hr style="margin: 12px 0; border: none; border-top: 1px solid #e5e7eb;">
                        <p><strong>Shipper:</strong> ${escapeHtml(shipment.shipperCompanyName)}</p>
                        <p><strong>Sold To:</strong> ${escapeHtml(shipment.soldToCompanyName)}</p>
                        <p><strong>Ship To:</strong> ${escapeHtml(shipment.shipToCompanyName)}</p>
                        <hr style="margin: 12px 0; border: none; border-top: 1px solid #e5e7eb;">
                        <p><strong>출발지:</strong> ${escapeHtml(shipment.portOfLoading || '-')}</p>
                        <p><strong>최종 목적지:</strong> ${escapeHtml(shipment.finalDestination || '-')}</p>
                        <p><strong>발송일:</strong> ${formatDate(shipment.freightDate)}</p>
                        <p><strong>운송사:</strong> ${escapeHtml(shipment.carrierName || '-')}</p>
                        <p><strong>운송장번호:</strong> ${escapeHtml(shipment.trackingNumber || '-')}</p>
                        <p><strong>면장번호:</strong> ${escapeHtml(shipment.exportLicenseNumber || '-')}</p>
                        ${shipment.lcNo ? `<p><strong>L/C No:</strong> ${escapeHtml(shipment.lcNo)}</p>` : ''}
                        <hr style="margin: 12px 0; border: none; border-top: 1px solid #e5e7eb;">
                        <p><strong>총 수량:</strong> ${shipment.totalQuantity}</p>
                        <p><strong>총 금액:</strong> ${formatCurrencyWithSymbol(shipment.totalAmount, shipment.currency)}</p>
                        ${shipment.totalNetWeight ? `<p><strong>순중량:</strong> ${shipment.totalNetWeight} kg</p>` : ''}
                        ${shipment.totalGrossWeight ? `<p><strong>총중량:</strong> ${shipment.totalGrossWeight} kg</p>` : ''}
                        ${shipment.totalCbm ? `<p><strong>CBM:</strong> ${shipment.totalCbm} m³</p>` : ''}
                        ${shipment.memo ? `<hr style="margin: 12px 0; border: none; border-top: 1px solid #e5e7eb;"><p><strong>메모:</strong> ${escapeHtml(shipment.memo)}</p>` : ''}
                    </div>
                </div>

                <!-- 중앙: 제품 목록 -->
                <div class="detail-right-column">
                    <div class="detail-info">
                        <h3 style="margin-bottom: 16px; color: #667eea; font-size: 1rem; font-weight: 700;">제품 목록</h3>
                        ${shipment.items.map((item, index) => `
                            <div style="margin-bottom: 16px; padding-bottom: 16px; ${index < shipment.items.length - 1 ? 'border-bottom: 1px solid #e5e7eb;' : ''}">
                                <p style="font-weight: 600; color: #374151;"><strong>${index + 1}. ${escapeHtml(item.productCode)}</strong></p>
                                ${item.productDescription ? `<p style="margin-left: 16px; color: #6b7280; font-size: 0.8rem; margin-top: 4px;">${escapeHtml(item.productDescription)}</p>` : ''}
                                <p style="margin-left: 16px; margin-top: 4px;"><strong>수량:</strong> ${formatNumber(item.quantity)} ${item.unit}</p>
                                <p style="margin-left: 16px;"><strong>단가:</strong> ${formatCurrencyWithSymbol(item.unitPrice, shipment.currency)}</p>
                                <p style="margin-left: 16px;"><strong>금액:</strong> ${formatCurrencyWithSymbol(item.amount, shipment.currency)}</p>
                                ${item.hsCode ? `<p style="margin-left: 16px;"><strong>HS Code:</strong> ${escapeHtml(item.hsCode)}</p>` : ''}
                                ${item.netWeight ? `<p style="margin-left: 16px;"><strong>순중량:</strong> ${item.netWeight} kg</p>` : ''}
                                ${item.grossWeight ? `<p style="margin-left: 16px;"><strong>총중량:</strong> ${item.grossWeight} kg</p>` : ''}
                            </div>
                        `).join('')}
                        
                        ${shipment.boxes && shipment.boxes.length > 0 ? `
                            <hr style="margin: 16px 0; border: none; border-top: 2px solid #e5e7eb;">
                            <h3 style="margin-bottom: 12px; color: #667eea; font-size: 1rem; font-weight: 700;">박스 정보</h3>
                            ${shipment.boxes.map((box, index) => `
                                <div style="margin-bottom: 12px; padding: 12px; background: #f9fafb; border-radius: 8px;">
                                    <p style="font-weight: 600;"><strong>${box.title || `Box ${index + 1}`}</strong></p>
                                    <p style="margin-left: 12px; font-size: 0.85rem; color: #6b7280;">
                                        ${box.dimensionString} × ${box.quantity}개
                                    </p>
                                </div>
                            `).join('')}
                        ` : ''}
                    </div>
                </div>

                <!-- 우측: 문서 관리 -->
                <div class="detail-documents-column">
                    <div class="documents-section">
                        <div class="documents-header">
                            <h3 class="documents-title">관련 문서</h3>
                            <button class="btn-upload" onclick="toggleUploadArea()">
                                + 업로드
                            </button>
                        </div>

                        <div class="upload-area" id="uploadArea">
                            <div class="upload-input-group">
                                <input type="file" id="documentFile" accept="*/*">
                                <textarea id="documentDescription" placeholder="문서 설명 (선택사항, 최대 500자)" maxlength="500"></textarea>
                                <div class="upload-buttons">
                                    <button class="btn-small btn-secondary" onclick="cancelUpload()">취소</button>
                                    <button class="btn-small btn-primary" onclick="uploadDocument()">완료</button>
                                </div>
                            </div>
                        </div>

                        <div class="documents-list" id="documentsList">
                            <div class="no-documents">문서가 없습니다</div>
                        </div>
                    </div>
                </div>
            </div>
        `;

        document.getElementById('detailModal').style.display = 'block';

        // 현재 선적 ID 저장 및 문서 목록 로드
        currentShipmentId = shipmentId;
        await loadDocuments(shipmentId);
    } catch (error) {
        console.error('Error:', error);
        showNotification(error.message, 'error');
    }
}

/**
 * 상세 모달 닫기
 */
function closeDetailModal() {
    document.getElementById('detailModal').style.display = 'none';
    currentShipmentId = null;

    // 업로드 영역 초기화
    const uploadArea = document.getElementById('uploadArea');
    if (uploadArea) {
        uploadArea.classList.remove('active');
    }
}

// ===== 문서 관리 함수 =====

/**
 * 업로드 영역 토글
 */
function toggleUploadArea() {
    const uploadArea = document.getElementById('uploadArea');
    uploadArea.classList.toggle('active');

    if (!uploadArea.classList.contains('active')) {
        document.getElementById('documentFile').value = '';
        document.getElementById('documentDescription').value = '';
    }
}

/**
 * 업로드 취소
 */
function cancelUpload() {
    document.getElementById('documentFile').value = '';
    document.getElementById('documentDescription').value = '';
    document.getElementById('uploadArea').classList.remove('active');
}

/**
 * 문서 업로드
 */
async function uploadDocument() {
    const fileInput = document.getElementById('documentFile');
    const description = document.getElementById('documentDescription').value;

    if (!fileInput.files || fileInput.files.length === 0) {
        showNotification('파일을 선택해주세요', 'error');
        return;
    }

    const file = fileInput.files[0];
    const formData = new FormData();
    formData.append('file', file);
    if (description) {
        formData.append('description', description);
    }

    try {
        const response = await fetch(`${API_BASE_URL}/${currentShipmentId}/documents`, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '문서 업로드에 실패했습니다');
        }

        showNotification('문서가 업로드되었습니다', 'success');
        cancelUpload();
        await loadDocuments(currentShipmentId);
    } catch (error) {
        console.error('Error:', error);
        showNotification(error.message, 'error');
    }
}

/**
 * 문서 목록 로드
 */
async function loadDocuments(shipmentId) {
    try {
        const response = await fetch(`${API_BASE_URL}/${shipmentId}/documents`);
        if (!response.ok) throw new Error('문서 목록을 불러오는데 실패했습니다');

        const data = await response.json();
        const documents = data.documents;
        const documentsList = document.getElementById('documentsList');

        if (!documents || documents.length === 0) {
            documentsList.innerHTML = '<div class="no-documents">문서가 없습니다</div>';
            return;
        }

        documentsList.innerHTML = documents.map(doc => `
            <div class="document-item">
                <div class="document-header">
                    <div class="document-filename">${escapeHtml(doc.originalFileName)}</div>
                    <div class="document-actions">
                        <button class="btn-icon btn-download" onclick="downloadDocument(${doc.id})" title="다운로드">
                            다운로드
                        </button>
                        <button class="btn-icon btn-delete-doc" onclick="confirmDeleteDocument(${doc.id})" title="삭제">
                            삭제
                        </button>
                    </div>
                </div>
                ${doc.description ? `<div class="document-description">${escapeHtml(doc.description)}</div>` : ''}
                <div class="document-info">
                    <span>${formatFileSize(doc.fileSize)}</span>
                    <span>${formatDateTime(doc.createdAt)}</span>
                </div>
            </div>
        `).join('');
    } catch (error) {
        console.error('Error:', error);
        showNotification('문서 목록을 불러오는데 실패했습니다', 'error');
    }
}

/**
 * 문서 다운로드
 */
async function downloadDocument(documentId) {
    try {
        const response = await fetch(`${API_BASE_URL}/${currentShipmentId}/documents/${documentId}/download`);
        if (!response.ok) throw new Error('문서 다운로드에 실패했습니다');

        const blob = await response.blob();
        const contentDisposition = response.headers.get('Content-Disposition');
        let filename = 'document';

        if (contentDisposition) {
            // filename*=UTF-8''%EC%B2... 형태 우선 파싱
            const filenameStarMatch = contentDisposition.match(/filename\*\=UTF-8''([^;]+)/);
            if (filenameStarMatch) {
                filename = decodeURIComponent(filenameStarMatch[1]);
            } else {
                // fallback: filename="..."
                const filenameMatch = contentDisposition.match(/filename=\"([^"]+)\"/);
                if (filenameMatch) {
                    filename = filenameMatch[1];
                }
            }
        }

        const url = window.URL.createObjectURL(blob);

        // Content-Disposition이 inline이면 새 탭에서 열기
        const isInline = contentDisposition && contentDisposition.toLowerCase().startsWith('inline');

        if (isInline) {
            // 새 탭에서 열기
            window.open(url, '_blank');
            showNotification('문서가 새 탭에서 열렸습니다', 'success');

            // 일정 시간 후 URL 해제
            setTimeout(() => window.URL.revokeObjectURL(url), 1000);
        } else {
            // 다운로드
            const a = document.createElement('a');
            a.href = url;
            a.download = filename;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);

            showNotification('문서가 다운로드되었습니다', 'success');
        }
    } catch (error) {
        console.error('Error:', error);
        showNotification(error.message, 'error');
    }
}

/**
 * 문서 삭제 확인
 */
function confirmDeleteDocument(documentId) {
    if (confirm('이 문서를 삭제하시겠습니까?')) {
        deleteDocument(documentId);
    }
}

/**
 * 문서 삭제
 */
async function deleteDocument(documentId) {
    try {
        const response = await fetch(`${API_BASE_URL}/${currentShipmentId}/documents/${documentId}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '문서 삭제에 실패했습니다');
        }

        showNotification('문서가 삭제되었습니다', 'success');
        await loadDocuments(currentShipmentId);
    } catch (error) {
        console.error('Error:', error);
        showNotification(error.message, 'error');
    }
}

/**
 * 파일 크기 포맷팅
 */
function formatFileSize(bytes) {
    if (!bytes) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i];
}

/**
 * 날짜+시간 포맷팅
 */
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

// ===== 메모 관리 =====

function openMemoModalFromButton(button) {
    const shipmentId = parseInt(button.dataset.shipmentId);

    let rawMemo = button.dataset.memo || '';

    try {
        if (rawMemo.startsWith('"') && rawMemo.endsWith('"')) {
            rawMemo = JSON.parse(rawMemo);
        }
    } catch (e) {
        console.log('Not a JSON string, using as is');
    }

    const textarea = document.createElement('textarea');
    textarea.innerHTML = rawMemo;
    const currentMemo = textarea.value;

    openMemoModal(shipmentId, currentMemo);
}


function openMemoModal(shipmentId, currentMemo) {
    memoTargetId = shipmentId;
    const memoText = document.getElementById('memoText');
    memoText.value = currentMemo || '';
    updateCharCount();
    document.getElementById('memoModal').style.display = 'block';
}

function closeMemoModal() {
    document.getElementById('memoModal').style.display = 'none';
    memoTargetId = null;
}

function updateCharCount() {
    const memoText = document.getElementById('memoText');
    const charCount = document.getElementById('memoCharCount');
    charCount.textContent = memoText.value.length;
}

async function updateMemo(event) {
    event.preventDefault();

    if (!memoTargetId) {
        return;
    }

    const memo = document.getElementById('memoText').value;

    try {
        const response = await fetch(`${API_BASE_URL}/${memoTargetId}/memo`, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ memo: memo })
        });


        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '메모 업데이트에 실패했습니다');
        }

        showNotification('메모가 저장되었습니다', 'success');
        closeMemoModal();
        await loadShipments();
    } catch (error) {
        console.error('Error:', error);
        showNotification(error.message, 'error');
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

function formatCurrency(amount) {
    if (!amount) return '0.00';
    return parseFloat(amount).toLocaleString('en-US', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}

function formatCurrencyWithSymbol(amount, currency) {
    if (!amount) return '-';

    const localeMap = {
        'USD': 'en-US',
        'KRW': 'ko-KR',
        'JPY': 'ja-JP',
        'EUR': 'de-DE',
        'CNY': 'zh-CN',
        'GBP': 'en-GB'
    };

    const locale = localeMap[currency] || 'en-US';

    return new Intl.NumberFormat(locale, {
        style: 'currency',
        currency: currency || 'USD'
    }).format(amount);
}

function formatNumber(num) {
    return new Intl.NumberFormat('ko-KR').format(num);
}

function formatProductNames(items) {
    if (!items || items.length === 0) return '-';

    const maxDisplay = 3;
    const displayItems = items.slice(0, maxDisplay);

    let html = `<div class="items-list">
        ${displayItems.map(item => `
            <span class="item-badge">
                ${escapeHtml(item.productCode)}
            </span>
        `).join('')}`;

    if (items.length > maxDisplay) {
        html += `
            <span class="item-badge" style="background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%); color: #4338ca;">
                외 ${items.length - maxDisplay}건
            </span>`;
    }

    html += `</div>`;
    return html;
}

function formatProductQuantities(items) {
    if (!items || items.length === 0) return '-';

    const maxDisplay = 3;
    const displayItems = items.slice(0, maxDisplay);

    let html = `<div class="items-list">
        ${displayItems.map(item => `
            <span class="item-badge">
                <strong>${formatNumber(item.quantity)}</strong>
            </span>
        `).join('')}`;

    if (items.length > maxDisplay) {
        // 앞 3개 제외한 나머지 수량 합계
        const remainingItems = items.slice(maxDisplay);
        const remainingQty = remainingItems.reduce((sum, item) => sum + item.quantity, 0);
        html += `
            <span class="item-badge" style="background: linear-gradient(135deg, #e0e7ff 0%, #c7d2fe 100%);">
                <strong style="color: #4338ca;">${formatNumber(remainingQty)}</strong>
            </span>`;
    }

    html += `</div>`;
    return html;
}

function getTypeBadgeClass(type) {
    const classes = {
        'EXPORT': 'badge-export',
        'SAMPLE': 'badge-sample'
    };
    return classes[type] || 'badge-export';
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function showLoading() {
    const tbody = document.getElementById('shipmentTableBody');
    tbody.innerHTML = `
        <tr>
            <td colspan="12" class="loading">
                <div class="spinner"></div>
                데이터를 불러오는 중...
            </td>
        </tr>
    `;
}

function showError(message) {
    const tbody = document.getElementById('shipmentTableBody');
    tbody.innerHTML = `
        <tr>
            <td colspan="12">
                <div class="empty-state">
                    <div class="empty-state-icon">⚠️</div>
                    <h3>오류가 발생했습니다</h3>
                    <p>${message}</p>
                </div>
            </td>
        </tr>
    `;
}

function showNotification(message, type = 'success') {
    const notification = document.createElement('div');
    notification.className = `notification ${type}`;
    notification.textContent = message;
    notification.style.cssText = `
        position: fixed;
        top: 20px;
        right: 20px;
        padding: 16px 24px;
        background: ${type === 'success' ? 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' : '#dc2626'};
        color: white;
        border-radius: 12px;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        z-index: 10000;
        font-weight: 600;
        transition: opacity 0.3s;
    `;
    document.body.appendChild(notification);

    setTimeout(() => {
        notification.style.opacity = '0';
        setTimeout(() => notification.remove(), 300);
    }, 3000);
}

// ===== Excel 다운로드 =====

// Excel 다운로드 기간별
function exportExcelByPeriod(period) {
    const today = new Date();
    let startDate, endDate;

    switch(period) {
        case 'thisYear':
            startDate = new Date(today.getFullYear(), 0, 1);
            endDate = new Date(today.getFullYear(), 11, 31);
            break;
        case '12months':
            startDate = new Date(today.getFullYear(), today.getMonth() - 11, 1);
            endDate = today;
            break;
        case '6months':
            startDate = new Date(today.getFullYear(), today.getMonth() - 5, 1);
            endDate = today;
            break;
        case '3months':
            startDate = new Date(today.getFullYear(), today.getMonth() - 2, 1);
            endDate = today;
            break;
        case '1month':
            startDate = new Date(today.getFullYear(), today.getMonth(), 1);
            endDate = today;
            break;
    }

    downloadExcel(formatDateForAPI(startDate), formatDateForAPI(endDate));
}

// Excel 다운로드 사용자 지정 범위
function exportExcelCustomRange() {
    const startDate = document.getElementById('customStartDate').value;
    const endDate = document.getElementById('customEndDate').value;

    if (!startDate || !endDate) {
        showNotification('시작일과 종료일을 모두 선택해주세요', 'error');
        return;
    }

    if (new Date(startDate) > new Date(endDate)) {
        showNotification('시작일이 종료일보다 늦을 수 없습니다', 'error');
        return;
    }

    downloadExcel(startDate, endDate);
}

// Excel 다운로드 실행
async function downloadExcel(startDate, endDate) {
    try {
        const url = `${API_BASE_URL}/export/excel?startDate=${startDate}&endDate=${endDate}`;
        const response = await fetch(url);

        if (!response.ok) throw new Error('Excel 다운로드에 실패했습니다');

        const blob = await response.blob();
        const downloadUrl = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = downloadUrl;
        a.download = `shipments_${startDate}_${endDate}.xlsx`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(downloadUrl);

        showNotification('Excel 파일이 다운로드되었습니다', 'success');
    } catch (error) {
        console.error('Error:', error);
        showNotification('Excel 다운로드에 실패했습니다', 'error');
    }
}

// 날짜 포맷 헬퍼 함수
function formatDateForAPI(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}

// ESC 키로 모달 닫기
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape' || event.key === 'Esc') {
        const detailModal = document.getElementById('detailModal');
        const memoModal = document.getElementById('memoModal');

        if (detailModal && detailModal.style.display === 'block') {
            closeDetailModal();
        } else if (memoModal && memoModal.style.display === 'block') {
            closeMemoModal();
        }
    }
});