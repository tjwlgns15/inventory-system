// API Base URL
const API_BASE_URL = '/api';

// 상태 관리
const state = {
    clients: [],
    products: [],
    carriers: [],
    boxes: [],
    prices: [],
    selectedClientId: null,
    boxCounter: 0,
    itemCounter: 0
};

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', () => {
    initializePage();
    setupEventListeners();
    setDefaultDates();
});

// 초기화
async function initializePage() {
    try {
        await Promise.all([
            loadClients(),
            loadProducts(),
            loadCarriers(),
            loadBoxes(),
            loadPrices()
        ]);
        console.log('데이터 로딩 완료');
    } catch (error) {
        console.error('초기화 중 오류:', error);
        alert('데이터를 불러오는 중 오류가 발생했습니다.');
    }
}

// 기본 날짜 설정
function setDefaultDates() {
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('invoiceDate').value = today;
    document.getElementById('freightDate').value = today;
}

// 이벤트 리스너 설정
function setupEventListeners() {
    // 거래처 관련
    document.getElementById('selectClientBtn').addEventListener('click', showClientModal);
    document.getElementById('manualInputBtn').addEventListener('click', enableManualInput);
    document.getElementById('closeClientModal').addEventListener('click', () => closeModal('clientModal'));
    document.getElementById('sameAsSoldTo').addEventListener('change', copySoldToShipTo);

    // 제품 관련
    document.getElementById('selectProductBtn').addEventListener('click', showProductModal);
    document.getElementById('closeProductModal').addEventListener('click', () => closeModal('productModal'));
    document.getElementById('addItemBtn').addEventListener('click', () => addItemRow());

    // 운송사 관련
    document.getElementById('selectCarrierBtn').addEventListener('click', showCarrierModal);
    document.getElementById('closeCarrierModal').addEventListener('click', () => closeModal('carrierModal'));
    document.getElementById('createCarrierBtn').addEventListener('click', openCarrierCreateModal);
    document.getElementById('closeCarrierFormModal').addEventListener('click', closeCarrierFormModal);
    document.getElementById('carrierForm').addEventListener('submit', handleCarrierFormSubmit);

    // 박스 관련
    document.getElementById('selectBoxBtn').addEventListener('click', showBoxModal);
    document.getElementById('closeBoxModal').addEventListener('click', () => closeModal('boxModal'));
    document.getElementById('addBoxBtn').addEventListener('click', () => addBoxRow());
    document.getElementById('createBoxBtn').addEventListener('click', openBoxCreateModal);
    document.getElementById('closeBoxFormModal').addEventListener('click', closeBoxFormModal);
    document.getElementById('boxForm').addEventListener('submit', handleBoxFormSubmit);

    // Remark 관련
    document.getElementById('previewRemarkBtn').addEventListener('click', showRemarkPreview);
    document.getElementById('closeRemarkModal').addEventListener('click', () => closeModal('remarkModal'));
    document.getElementById('copyRemarkBtn').addEventListener('click', copyRemarkToClipboard);

    // 원산지 정보 템플릿 체크박스
    document.getElementById('useOriginTemplate').addEventListener('change', (e) => {
        const textarea = document.getElementById('originDescription');
        if (e.target.checked) {
            textarea.value = 'The exporter of the products covered by this document declares that, except where otherwise clearly indicated, these products are of the Republic of Korea preferential origin.';
        } else {
            textarea.value = '';
        }
    });

    // 폼 제출
    document.getElementById('shipmentForm').addEventListener('submit', handleSubmit);
    document.getElementById('cancelBtn').addEventListener('click', () => {
        if (confirm('작성 중인 내용이 사라집니다. 취소하시겠습니까?')) {
            window.history.back();
        }
    });

    // 모달 외부 클릭 시 닫기
    document.querySelectorAll('.modal').forEach(modal => {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                modal.classList.remove('active');
            }
        });
    });
}

// ===== 데이터 로드 함수들 =====

async function loadClients() {
    try {
        const response = await fetch(`${API_BASE_URL}/clients`);
        if (!response.ok) throw new Error('거래처 목록을 불러올 수 없습니다.');
        state.clients = await response.json();
    } catch (error) {
        console.error('거래처 로드 실패:', error);
    }
}

async function loadProducts() {
    try {
        const response = await fetch(`${API_BASE_URL}/products/all`);
        if (!response.ok) throw new Error('제품 목록을 불러올 수 없습니다.');
        state.products = await response.json();
        console.log(state.products);
    } catch (error) {
        console.error('제품 로드 실패:', error);
    }
}

async function loadCarriers() {
    try {
        const response = await fetch(`${API_BASE_URL}/carriers`);
        if (!response.ok) throw new Error('운송 방법 목록을 불러올 수 없습니다.');
        state.carriers = await response.json();
    } catch (error) {
        console.error('운송 방법 로드 실패:', error);
    }
}

async function loadBoxes() {
    try {
        const response = await fetch(`${API_BASE_URL}/shipment-boxes`);
        if (!response.ok) throw new Error('박스 목록을 불러올 수 없습니다.');
        state.boxes = await response.json();
    } catch (error) {
        console.error('박스 로드 실패:', error);
    }
}

async function loadPrices() {
    try {
        const response = await fetch(`${API_BASE_URL}/prices/all`);
        if (!response.ok) throw new Error('가격 정보를 불러올 수 없습니다.');
        state.prices = await response.json();
    } catch (error) {
        console.error('가격 정보 로드 실패:', error);
    }
}

// ===== 거래처 관련 함수들 =====

function showClientModal() {
    const modal = document.getElementById('clientModal');
    const clientList = document.getElementById('clientList');

    clientList.innerHTML = state.clients.map(client => `
        <div class="product-item" onclick="selectClient(${client.id})">
            <div style="display: flex; justify-content: space-between; align-items: start;">
                <div>
                    <strong style="font-size: 1.1em; color: #2d3748;">${client.name}</strong>
                    <div style="color: #718096; margin-top: 5px; font-size: 0.9em;">
                        ${client.clientCode} | ${client.countryName || ''}
                    </div>
                    <div style="color: #4a5568; margin-top: 5px;">
                        ${client.address || '주소 없음'}
                    </div>
                </div>
                <span class="info-badge">${client.currencySymbol}</span>
            </div>
        </div>
    `).join('');

    modal.classList.add('active');
}

function selectClient(clientId) {
    const client = state.clients.find(c => c.id === clientId);
    if (!client) return;

    state.selectedClientId = clientId;

    // Client ID 설정
    document.getElementById('clientId').value = clientId;

    // Sold To 정보 채우기
    document.getElementById('soldToCompanyName').value = client.name;
    document.getElementById('soldToAddress').value = client.address || '';
    document.getElementById('soldToContactPerson').value = client.representative || '';
    document.getElementById('soldToPhone').value = client.contactNumber || '';

    // Ship To에도 동일하게 채우기
    document.getElementById('shipToCompanyName').value = client.shipmentDestination;
    document.getElementById('shipToAddress').value = client.shipmentAddress || '';
    document.getElementById('shipToContactPerson').value = client.shipmentRepresentative || '';
    document.getElementById('shipToPhone').value = client.shipmentContactNumber || '';
    document.getElementById('finalDestination').value = client.finalDestination || '';

    // 통화 설정
    if (client.currency) {
        const currencyMap = {
            'USD': 'USD',
            'EUR': 'EUR',
            'KRW': 'KRW'
        };
        const currencySelect = document.getElementById('currency');
        if (currencyMap[client.currency]) {
            currencySelect.value = currencyMap[client.currency];
        }
    }

    closeModal('clientModal');
}

function enableManualInput() {
    state.selectedClientId = null;
    document.getElementById('clientId').value = '';

    // 모든 필드 비우기
    document.getElementById('soldToCompanyName').value = '';
    document.getElementById('soldToAddress').value = '';
    document.getElementById('soldToContactPerson').value = '';
    document.getElementById('soldToPhone').value = '';

    document.getElementById('shipToCompanyName').value = '';
    document.getElementById('shipToAddress').value = '';
    document.getElementById('shipToContactPerson').value = '';
    document.getElementById('shipToPhone').value = '';

    closeModal('clientModal');
    alert('직접 입력 모드로 전환되었습니다.');
}

function copySoldToShipTo(event) {
    if (event.target.checked) {
        document.getElementById('shipToCompanyName').value = document.getElementById('soldToCompanyName').value;
        document.getElementById('shipToAddress').value = document.getElementById('soldToAddress').value;
        document.getElementById('shipToContactPerson').value = document.getElementById('soldToContactPerson').value;
        document.getElementById('shipToPhone').value = document.getElementById('soldToPhone').value;
    }
}

// ===== 운송사 관련 함수들 =====

function showCarrierModal() {
    renderCarrierList();
    document.getElementById('carrierModal').classList.add('active');
}

function renderCarrierList() {
    const carrierList = document.getElementById('carrierList');

    if (state.carriers.length === 0) {
        carrierList.innerHTML = `
            <div style="text-align: center; padding: 40px; color: #718096;">
                <div style="font-size: 3em; margin-bottom: 15px;">🚢</div>
                <p>등록된 운송 방법이 없습니다.</p>
                <p style="font-size: 0.9em; margin-top: 5px;">새 운송 방법 버튼을 클릭하여 추가해보세요.</p>
            </div>
        `;
        return;
    }

    carrierList.innerHTML = state.carriers.map(carrier => `
        <div class="box-template-item">
            <div class="box-template-info">
                <div class="box-template-title">${carrier.name}${carrier.nameEn ? ` (${carrier.nameEn})` : ''}</div>
                <div class="box-template-specs">
                    ${carrier.contactNumber ? `연락처: ${carrier.contactNumber}` : ''}
                    ${carrier.email ? ` | 이메일: ${carrier.email}` : ''}
                </div>
            </div>
            <div class="box-template-actions">
                <button class="btn-icon btn-use" onclick="selectCarrier(${carrier.id})">
                    선택
                </button>
                <button class="btn-icon btn-edit" onclick="openCarrierEditModal(${carrier.id})">
                    수정
                </button>
            </div>
        </div>
    `).join('');
}

function openCarrierCreateModal() {
    document.getElementById('carrierFormModalTitle').textContent = '운송 방법 등록';
    document.getElementById('carrierForm').reset();
    document.getElementById('carrierFormId').value = '';
    document.getElementById('carrierFormModal').classList.add('active');
}

function openCarrierEditModal(carrierId) {
    const carrier = state.carriers.find(c => c.id === carrierId);
    if (!carrier) return;

    document.getElementById('carrierFormModalTitle').textContent = '운송 방법 수정';
    document.getElementById('carrierFormId').value = carrier.id;
    document.getElementById('carrierFormName').value = carrier.name || '';
    document.getElementById('carrierFormNameEn').value = carrier.nameEn || '';
    document.getElementById('carrierFormContact').value = carrier.contactNumber || '';
    document.getElementById('carrierFormEmail').value = carrier.email || '';
    document.getElementById('carrierFormNotes').value = carrier.notes || '';

    document.getElementById('carrierFormModal').classList.add('active');
}

function closeCarrierFormModal() {
    document.getElementById('carrierFormModal').classList.remove('active');
    document.getElementById('carrierForm').reset();
}

async function handleCarrierFormSubmit(e) {
    e.preventDefault();

    const carrierId = document.getElementById('carrierFormId').value;
    const carrierData = {
        name: document.getElementById('carrierFormName').value,
        nameEn: document.getElementById('carrierFormNameEn').value || null,
        contactNumber: document.getElementById('carrierFormContact').value || null,
        email: document.getElementById('carrierFormEmail').value || null,
        notes: document.getElementById('carrierFormNotes').value || null
    };

    try {
        let response;
        if (carrierId) {
            // 수정
            response = await fetch(`${API_BASE_URL}/carriers/${carrierId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(carrierData)
            });
        } else {
            // 생성
            response = await fetch(`${API_BASE_URL}/carriers`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(carrierData)
            });
        }

        if (!response.ok) {
            throw new Error(carrierId ? '운송 방법 수정에 실패했습니다.' : '운송 방법 등록에 실패했습니다.');
        }

        alert(carrierId ? '운송 방법이 수정되었습니다.' : '운송 방법이 등록되었습니다.');
        closeCarrierFormModal();
        await loadCarriers();
        renderCarrierList();
    } catch (error) {
        console.error('Error:', error);
        alert('오류: ' + error.message);
    }
}

function selectCarrier(carrierId) {
    const carrier = state.carriers.find(c => c.id === carrierId);
    console.log(carrier);
    if (!carrier) return;

    document.getElementById('carrierId').value = carrierId;
    document.getElementById('carrierName').value = carrier.nameEn;

    closeModal('carrierModal');
}

// ===== 박스 관련 함수들 =====

function showBoxModal() {
    renderBoxList();
    document.getElementById('boxModal').classList.add('active');
}

function renderBoxList() {
    const boxList = document.getElementById('boxList');

    if (state.boxes.length === 0) {
        boxList.innerHTML = `
            <div style="text-align: center; padding: 40px; color: #718096;">
                <div style="font-size: 3em; margin-bottom: 15px;">📦</div>
                <p>등록된 박스 템플릿이 없습니다.</p>
                <p style="font-size: 0.9em; margin-top: 5px;">새 박스 템플릿 버튼을 클릭하여 추가해보세요.</p>
            </div>
        `;
        return;
    }

    boxList.innerHTML = state.boxes.map(box => `
        <div class="box-template-item">
            <div class="box-template-info">
                <div class="box-template-title">${box.title}</div>
                <div class="box-template-specs">
                    규격: ${box.width} × ${box.length} × ${box.height} cm
                </div>
            </div>
            <div class="box-template-actions">
                <button class="btn-icon btn-use" onclick="selectBox(${box.id})">
                    사용
                </button>
                <button class="btn-icon btn-edit" onclick="openBoxEditModal(${box.id})">
                    수정
                </button>
                <button class="btn-icon btn-delete" onclick="deleteBox(${box.id})">
                    삭제
                </button>
            </div>
        </div>
    `).join('');
}

function openBoxCreateModal() {
    document.getElementById('boxFormModalTitle').textContent = '박스 템플릿 등록';
    document.getElementById('boxForm').reset();
    document.getElementById('boxFormId').value = '';
    document.getElementById('boxFormModal').classList.add('active');
}

function openBoxEditModal(boxId) {
    const box = state.boxes.find(b => b.id === boxId);
    if (!box) return;

    document.getElementById('boxFormModalTitle').textContent = '박스 템플릿 수정';
    document.getElementById('boxFormId').value = box.id;
    document.getElementById('boxFormTitleInput').value = box.title;
    document.getElementById('boxFormWidth').value = box.width;
    document.getElementById('boxFormLength').value = box.length;
    document.getElementById('boxFormHeight').value = box.height;

    document.getElementById('boxFormModal').classList.add('active');
}

function closeBoxFormModal() {
    document.getElementById('boxFormModal').classList.remove('active');
    document.getElementById('boxForm').reset();
}

async function handleBoxFormSubmit(e) {
    e.preventDefault();

    const boxId = document.getElementById('boxFormId').value;
    const boxData = {
        title: document.getElementById('boxFormTitleInput').value,
        width: parseFloat(document.getElementById('boxFormWidth').value),
        length: parseFloat(document.getElementById('boxFormLength').value),
        height: parseFloat(document.getElementById('boxFormHeight').value),
    };

    try {
        let response;
        if (boxId) {
            // 수정
            response = await fetch(`${API_BASE_URL}/shipment-boxes/${boxId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(boxData)
            });
        } else {
            // 생성
            response = await fetch(`${API_BASE_URL}/shipment-boxes`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(boxData)
            });
        }

        if (!response.ok) {
            throw new Error(boxId ? '박스 수정에 실패했습니다.' : '박스 등록에 실패했습니다.');
        }

        alert(boxId ? '박스가 수정되었습니다.' : '박스가 등록되었습니다.');
        closeBoxFormModal();
        await loadBoxes();
        renderBoxList();
    } catch (error) {
        console.error('Error:', error);
        alert('오류: ' + error.message);
    }
}

async function deleteBox(boxId) {
    if (!confirm('이 박스 템플릿을 삭제하시겠습니까?')) {
        return;
    }

    try {
        const response = await fetch(`${API_BASE_URL}/shipment-boxes/${boxId}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            throw new Error('박스 삭제에 실패했습니다.');
        }

        alert('박스가 삭제되었습니다.');
        await loadBoxes();
        renderBoxList();
    } catch (error) {
        console.error('Error:', error);
        alert('오류: ' + error.message);
    }
}

function selectBox(boxId) {
    const box = state.boxes.find(b => b.id === boxId);
    if (!box) return;

    addBoxRow(box);
    closeModal('boxModal');
}

function addBoxRow(boxData = null) {
    state.boxCounter++;
    const boxId = `box-${state.boxCounter}`;

    const boxRow = document.createElement('div');
    boxRow.className = 'item-row';
    boxRow.id = boxId;

    const sequence = document.querySelectorAll('#boxesContainer .item-row').length + 1;

    boxRow.innerHTML = `
        <div class="item-header">
            <span class="item-number">박스 #${sequence}</span>
            <button type="button" class="remove-btn" onclick="removeBox('${boxId}')">삭제</button>
        </div>
        <input type="hidden" class="box-sequence" value="${sequence}">
        <input type="hidden" class="box-template-id" value="${boxData?.id || ''}">
        <div class="form-grid" style="grid-template-columns: repeat(3, 1fr);">
            <div class="form-group">
                <label>박스명 <span class="required">*</span></label>
                <input type="text" class="box-title" value="${boxData?.title || ''}" required>
            </div>
            <div class="form-group">
                <label>가로 (cm) <span class="required">*</span></label>
                <input type="number" class="box-width" step="0.01" value="${boxData?.width || ''}" required>
            </div>
            <div class="form-group">
                <label>세로 (cm) <span class="required">*</span></label>
                <input type="number" class="box-length" step="0.01" value="${boxData?.length || ''}" required>
            </div>
            <div class="form-group">
                <label>높이 (cm) <span class="required">*</span></label>
                <input type="number" class="box-height" step="0.01" value="${boxData?.height || ''}" required>
            </div>
            <div class="form-group">
                <label>수량 <span class="required">*</span></label>
                <input type="number" class="box-quantity" value="${boxData?.quantity || 1}" required>
            </div>
        </div>
    `;

    document.getElementById('boxesContainer').appendChild(boxRow);
}

function removeBox(boxId) {
    const boxDiv = document.getElementById(boxId);
    if (boxDiv && confirm('이 박스를 삭제하시겠습니까?')) {
        boxDiv.remove();
        renumberBoxes();
    }
}

function renumberBoxes() {
    const container = document.getElementById('boxesContainer');
    const boxDivs = container.querySelectorAll('.item-row');

    boxDivs.forEach((boxDiv, index) => {
        const newNumber = index + 1;
        const header = boxDiv.querySelector('.item-header .item-number');
        if (header) {
            header.textContent = `박스 #${newNumber}`;
        }
        const sequenceInput = boxDiv.querySelector('.box-sequence');
        if (sequenceInput) {
            sequenceInput.value = newNumber;
        }
    });
}

// ===== 제품 관련 함수들 =====

function showProductModal() {
    const modal = document.getElementById('productModal');
    const productList = document.getElementById('productList');

    // 거래처가 선택된 경우 해당 거래처의 가격 정보가 있는 제품을 우선 표시
    let priorityProducts = [];
    let otherProducts = [];

    if (state.selectedClientId) {
        const clientPrices = state.prices.filter(p => p.clientId === state.selectedClientId);
        const priorityProductIds = new Set(clientPrices.map(p => p.productId));

        priorityProducts = state.products.filter(p => priorityProductIds.has(p.id));
        otherProducts = state.products.filter(p => !priorityProductIds.has(p.id));
    } else {
        otherProducts = state.products;
    }

    const renderProduct = (product, isPriority = false) => {
        const price = state.prices.find(p =>
            p.productId === product.id &&
            p.clientId === state.selectedClientId
        );

        return `
            <div class="product-item ${isPriority ? 'priority' : ''}" onclick="selectProduct(${product.id})">
                <div style="display: flex; justify-content: space-between; align-items: start;">
                    <div>
                        <strong style="font-size: 1.1em; color: #2d3748;">
                            ${product.name}
                            ${isPriority ? '<span class="product-badge">매핑됨</span>' : ''}
                        </strong>
                        <div style="color: #718096; margin-top: 5px; font-size: 0.9em;">
                            ${product.productCode} | ${product.unit}
                        </div>
                        ${product.hsCode ? `<div style="color: #4a5568; margin-top: 3px; font-size: 0.85em;">HS Code: ${product.hsCode}</div>` : ''}
                        ${price ? `
                            <div style="color: #48bb78; margin-top: 5px; font-weight: 600;">
                                단가: ${price.currencySymbol}${price.unitPrice.toFixed(2)}
                            </div>
                        ` : ''}
                    </div>
                </div>
            </div>
        `;
    };

    let html = '';

    if (priorityProducts.length > 0) {
        html += '<h3 style="color: #48bb78; margin-bottom: 10px; font-size: 1.1em;">📌 매핑된 제품</h3>';
        html += priorityProducts.map(p => renderProduct(p, true)).join('');

        if (otherProducts.length > 0) {
            html += '<h3 style="color: #718096; margin: 20px 0 10px; font-size: 1.1em;">기타 제품</h3>';
            html += otherProducts.map(p => renderProduct(p, false)).join('');
        }
    } else {
        html = otherProducts.map(p => renderProduct(p, false)).join('');
    }

    productList.innerHTML = html;
    modal.classList.add('active');
}

function selectProduct(productId) {
    const product = state.products.find(p => p.id === productId);
    if (!product) return;

    const price = state.selectedClientId
        ? state.prices.find(p => p.clientId === state.selectedClientId && p.productId === productId)
        : null;

    addItemRow({
        productId: product.id,
        productCode: product.productCode,
        productDescription: product.description,
        hsCode: product.hsCode || '',
        unit: product.unit,
        unitPrice: price ? price.unitPrice : 0
    });

    closeModal('productModal');
}

function addItemRow(productData = null) {
    state.itemCounter++;
    const itemId = `item-${state.itemCounter}`;

    const itemRow = document.createElement('div');
    itemRow.className = 'item-row';
    itemRow.id = itemId;

    const sequence = document.querySelectorAll('#itemsContainer .item-row').length + 1;

    console.log(productData);
    itemRow.innerHTML = `
        <div class="item-header">
            <span class="item-number">제품 #${sequence}</span>
            <button type="button" class="remove-btn" onclick="removeItem('${itemId}')">삭제</button>
        </div>
        <input type="hidden" class="item-sequence" value="${sequence}">
        <input type="hidden" class="item-product-id" value="${productData?.productId || ''}">
        <div class="form-grid" style="grid-template-columns: repeat(2, 1fr);">
            <div class="form-group">
                <label>제품 코드 <span class="required">*</span></label>
                <input type="text" class="item-product-code" value="${productData?.productCode || ''}" required>
            </div>
            <div class="form-group">
                <label>제품 설명 <span class="required">*</span></label>
                <input type="text" class="item-product-description" value="${productData?.productDescription || ''}" required>
            </div>
            <div class="form-group">
                <label>HS Code <span class="required">*</span></label>
                <input type="text" class="item-hs-code" value="${productData?.hsCode || ''}" required>
            </div>
            <div class="form-group">
                <label>단위 <span class="required">*</span></label>
                <input type="text" class="item-unit" value="${productData?.unit || 'EA'}" required>
            </div>
            <div class="form-group">
                <label>수량 <span class="required">*</span></label>
                <input type="number" class="item-quantity" value="${productData?.quantity || ''}" required 
                       oninput="calculateItemAmount('${itemId}')">
            </div>
            <div class="form-group">
                <label>단가 <span class="required">*</span></label>
                <input type="number" class="item-unit-price" step="0.01" 
                       value="${productData?.unitPrice || ''}" required
                       oninput="calculateItemAmount('${itemId}')">
            </div>
            <div class="form-group">
                <label>금액</label>
                <input type="number" class="item-amount" step="0.01" readonly>
            </div>
        </div>
        
        <!-- 중량 및 CBM 정보 추가 -->
        <div class="form-grid" style="grid-template-columns: repeat(3, 1fr); margin-top: 15px; padding-top: 15px; border-top: 1px solid #e2e8f0;">
            <div class="form-group">
                <label>순중량 (kg) <span class="required">*</span></label>
                <input type="number" class="item-net-weight" step="0.001" min="0" 
                       value="${productData?.netWeight || ''}" required>
            </div>
            <div class="form-group">
                <label>총중량 (kg) <span class="required">*</span></label>
                <input type="number" class="item-gross-weight" step="0.001" min="0"
                       value="${productData?.grossWeight || ''}" required>
            </div>
            <div class="form-group">
                <label>CBM (m³)</label>
                <input type="number" class="item-cbm" step="0.001" min="0"
                       value="${productData?.cbm || ''}">
            </div>
        </div>
    `;

    document.getElementById('itemsContainer').appendChild(itemRow);

    // 금액 초기 계산
    if (productData) {
        calculateItemAmount(itemId);
    }
}

function removeItem(itemId) {
    const itemDiv = document.getElementById(itemId);
    if (itemDiv && confirm('이 제품을 삭제하시겠습니까?')) {
        itemDiv.remove();
        renumberItems();  // 재정렬
    }
}

// 제품 번호 재정렬
function renumberItems() {
    const container = document.getElementById('itemsContainer');
    const itemDivs = container.querySelectorAll('.item-row');

    itemDivs.forEach((itemDiv, index) => {
        const newNumber = index + 1;
        // 헤더의 번호 업데이트
        const header = itemDiv.querySelector('.item-header .item-number');
        if (header) {
            header.textContent = `제품 #${newNumber}`;
        }
        // sequence 업데이트
        const sequenceInput = itemDiv.querySelector('.item-sequence');
        if (sequenceInput) {
            sequenceInput.value = newNumber;
        }
    });
}

function calculateItemAmount(itemId) {
    const itemDiv = document.getElementById(itemId);
    if (!itemDiv) return;

    const quantity = parseFloat(itemDiv.querySelector('.item-quantity').value) || 0;
    const unitPrice = parseFloat(itemDiv.querySelector('.item-unit-price').value) || 0;
    const amount = quantity * unitPrice;

    itemDiv.querySelector('.item-amount').value = amount.toFixed(2);
}

// ===== Remark 관련 함수들 =====

function showRemarkPreview() {
    const shipmentType = document.getElementById('shipmentType');
    const tradeTerms = document.getElementById('tradeTerms');
    const originDescription = document.getElementById('originDescription').value.trim();
    const additionalRemarks = document.getElementById('additionalRemarks').value.trim();

    const shipmentTypeText = shipmentType.options[shipmentType.selectedIndex]?.text || '';
    const tradeTermsText = tradeTerms.options[tradeTerms.selectedIndex]?.text || '';

    let remarkText = '';

    if (shipmentTypeText) {
        remarkText += shipmentTypeText + '\n';
    }

    if (tradeTermsText) {
        remarkText += tradeTermsText + '\n';
    }

    if (originDescription) {
        remarkText += originDescription + '\n';
    }

    if (additionalRemarks) {
        remarkText += '\n' + additionalRemarks;
    }

    document.getElementById('remarkPreview').textContent = remarkText || '(입력된 내용이 없습니다)';
    document.getElementById('remarkModal').classList.add('active');
}

function copyRemarkToClipboard() {
    const remarkText = document.getElementById('remarkPreview').textContent;

    navigator.clipboard.writeText(remarkText).then(() => {
        alert('Remark 내용이 클립보드에 복사되었습니다.');
    }).catch(err => {
        console.error('복사 실패:', err);
        alert('복사에 실패했습니다.');
    });
}

// ===== 모달 관련 함수들 =====

function closeModal(modalId) {
    document.getElementById(modalId).classList.remove('active');
}

// ===== 폼 제출 =====

// 헬퍼 함수: 값이 없거나 'undefined' 문자열이면 null 반환
function getValueOrNull(elementId) {
    const element = document.getElementById(elementId);
    if (!element) return null;

    const value = element.value;
    if (!value || value === 'undefined' || value.trim() === '') {
        return null;
    }
    return value;
}

// 헬퍼 함수: 숫자 값 또는 null 반환
function getNumberOrNull(elementId) {
    const value = getValueOrNull(elementId);
    if (value === null) return null;

    const num = parseFloat(value);
    return isNaN(num) ? null : num;
}

// 헬퍼 함수: input element에서 숫자 값 또는 null 반환
function getInputNumberOrNull(input) {
    if (!input) return null;
    const value = input.value;
    if (!value || value.trim() === '') return null;
    const num = parseFloat(value);
    return isNaN(num) ? null : num;
}

async function handleSubmit(e) {
    e.preventDefault();

    // 박스 정보 수집
    const boxes = [];
    document.querySelectorAll('#boxesContainer .item-row').forEach(boxDiv => {
        const boxTemplateIdValue = boxDiv.querySelector('.box-template-id').value;
        boxes.push({
            sequence: parseInt(boxDiv.querySelector('.box-sequence').value),
            boxTemplateId: boxTemplateIdValue && boxTemplateIdValue !== 'undefined' && boxTemplateIdValue !== ''
                ? parseInt(boxTemplateIdValue)
                : null,
            title: boxDiv.querySelector('.box-title').value,
            width: parseFloat(boxDiv.querySelector('.box-width').value),
            length: parseFloat(boxDiv.querySelector('.box-length').value),
            height: parseFloat(boxDiv.querySelector('.box-height').value),
            quantity: parseInt(boxDiv.querySelector('.box-quantity').value)
        });
    });

    // 제품 정보 수집 (중량 및 CBM 포함)
    const items = [];
    document.querySelectorAll('#itemsContainer .item-row').forEach(itemDiv => {
        const productIdValue = itemDiv.querySelector('.item-product-id').value;
        const hsCodeValue = itemDiv.querySelector('.item-hs-code').value;

        items.push({
            sequence: parseInt(itemDiv.querySelector('.item-sequence').value),
            productId: productIdValue && productIdValue !== 'undefined' && productIdValue !== ''
                ? parseInt(productIdValue)
                : null,
            productCode: itemDiv.querySelector('.item-product-code').value,
            productDescription: itemDiv.querySelector('.item-product-description').value,
            hsCode: hsCodeValue && hsCodeValue !== 'undefined' && hsCodeValue.trim() !== ''
                ? hsCodeValue
                : null,
            unit: itemDiv.querySelector('.item-unit').value,
            quantity: parseInt(itemDiv.querySelector('.item-quantity').value),
            unitPrice: parseFloat(itemDiv.querySelector('.item-unit-price').value),
            netWeight: getInputNumberOrNull(itemDiv.querySelector('.item-net-weight')),
            grossWeight: getInputNumberOrNull(itemDiv.querySelector('.item-gross-weight')),
            cbm: getInputNumberOrNull(itemDiv.querySelector('.item-cbm'))
        });
    });

    if (items.length === 0) {
        alert('최소 1개 이상의 제품을 추가해주세요.');
        return;
    }

    // 요청 데이터 구성 (중량/CBM 필드 제거)
    const requestData = {
        invoiceDate: document.getElementById('invoiceDate').value,
        freightDate: document.getElementById('freightDate').value,

        shipperCompanyName: document.getElementById('shipperCompanyName').value,
        shipperAddress: document.getElementById('shipperAddress').value,
        shipperContactPerson: getValueOrNull('shipperContactPerson'),
        shipperPhone: getValueOrNull('shipperPhone'),

        clientId: getNumberOrNull('clientId'),
        soldToCompanyName: document.getElementById('soldToCompanyName').value,
        soldToAddress: document.getElementById('soldToAddress').value,
        soldToContactPerson: getValueOrNull('soldToContactPerson'),
        soldToPhone: getValueOrNull('soldToPhone'),

        shipToCompanyName: document.getElementById('shipToCompanyName').value,
        shipToAddress: document.getElementById('shipToAddress').value,
        shipToContactPerson: getValueOrNull('shipToContactPerson'),
        shipToPhone: getValueOrNull('shipToPhone'),

        portOfLoading: document.getElementById('portOfLoading').value,
        finalDestination: document.getElementById('finalDestination').value,
        carrierId: getNumberOrNull('carrierId'),
        carrierName: getValueOrNull('carrierName'),
        trackingNumber: getValueOrNull('trackingNumber'),
        exportLicenseNumber: getValueOrNull('exportLicenseNumber'),

        lcNo: getValueOrNull('lcNo'),
        lcDate: getValueOrNull('lcDate'),
        lcIssuingBank: getValueOrNull('lcIssuingBank'),

        shipmentType: document.getElementById('shipmentType').value,
        tradeTerms: document.getElementById('tradeTerms').value,
        originDescription: getValueOrNull('originDescription'),
        additionalRemarks: getValueOrNull('additionalRemarks'),

        currency: document.getElementById('currency').value,

        boxes: boxes.length > 0 ? boxes : null,
        items: items
    };

    console.log('전송 데이터:', requestData);

    try {
        const response = await fetch(`${API_BASE_URL}/shipments`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestData)
        });

        if (!response.ok) {
            const errorData = await response.json();
            throw new Error(errorData.message || '선적 문서 생성에 실패했습니다.');
        }

        const result = await response.json();

        alert(`선적 문서가 성공적으로 생성되었습니다.\nInvoice 번호: ${result.invoiceNumber}`);

        // 목록 페이지로 이동하거나 상세 페이지로 이동
        window.location.href = `/shipments/${result.id}`;

    } catch (error) {
        console.error('제출 오류:', error);
        alert('오류: ' + error.message);
    }
}