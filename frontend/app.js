/**
 * RodaLivre - Vanilla JS Application Logic
 */

const API_BASE_URL = 'https://systemcar-backend.onrender.com/api/v1';

document.addEventListener('DOMContentLoaded', () => {
    // 1. Initializations
    initSplashScreen();
    initHeaderScroll();
    loadBranches();
    loadVehicles();

    // 2. Event Listeners
    setupSearchForm();
});

/**
 * Controls the fade-out of the Premium Splash Screen
 */
function initSplashScreen() {
    const splash = document.getElementById('splash-screen');
    if (!splash) return;

    // O tempo coincide com a finalização das animações (4.5 segundos)
    setTimeout(() => {
        splash.classList.add('fade-out');
        setTimeout(() => {
            splash.style.display = 'none';
        }, 800); // tempo correspondente à transição de fade CSS
    }, 4500);
}

/**
 * Adds glass effect to header on scroll
 */
function initHeaderScroll() {
    const header = document.getElementById('main-header');
    window.addEventListener('scroll', () => {
        if (window.scrollY > 20) {
            header.style.boxShadow = '0 4px 20px rgba(0, 0, 0, 0.08)';
        } else {
            header.style.boxShadow = 'none';
        }
    });
}

/**
 * Fetch Branches from Backend and populate the select
 */
async function loadBranches() {
    const select = document.getElementById('pickup-location');

    try {
        // Fetch API - Tratamento de Loading, Success e Error exigido pelo manual
        const response = await fetch(`${API_BASE_URL}/branches`);

        if (!response.ok) throw new Error('Falha ao carregar agências');

        const branches = await response.json();

        if (branches && branches.length > 0) {
            select.innerHTML = '<option value="" disabled selected>Onde você vai retirar?</option>';
            branches.forEach(branch => {
                const option = document.createElement('option');
                option.value = branch.id;
                // Prevenção XSS via textContent (conforme AGENTS.md)
                option.textContent = `${branch.name} - ${branch.city}/${branch.state}`;
                select.appendChild(option);
            });
        }
    } catch (error) {
        console.error('Erro de conexão com API:', error);
        select.innerHTML = '<option value="" disabled selected>Erro ao carregar locais</option>';
    }
}

/**
 * Handle Search Form Submission
 */
function setupSearchForm() {
    const form = document.getElementById('search-form');

    form.addEventListener('submit', (e) => {
        e.preventDefault();

        const branch = document.getElementById('pickup-location').value;
        const pickupDate = document.getElementById('pickup-date').value;
        const returnDate = document.getElementById('return-date').value;

        if (!branch || !pickupDate || !returnDate) {
            alert('Por favor, preencha todos os campos de busca.');
            return;
        }

        const pickup = new Date(pickupDate);
        const returnD = new Date(returnDate);

        if (returnD <= pickup) {
            alert('Erro: A data de devolução deve ser posterior à data de retirada!');
            return;
        }

        const btn = form.querySelector('button[type="submit"]');
        const originalText = btn.innerHTML;

        btn.innerHTML = '<i data-lucide="loader" class="spin"></i> Buscando...';
        btn.disabled = true;
        lucide.createIcons();

        // Simulate search delay to API
        setTimeout(() => {
            btn.innerHTML = originalText;
            btn.disabled = false;
            lucide.createIcons();

            // Smooth scroll to fleet
            document.getElementById('frota').scrollIntoView({ behavior: 'smooth' });
        }, 800);
    });
}

/**
 * Fetch Vehicles from Backend and display them securely in the UI
 */
async function loadVehicles() {
    const grid = document.getElementById('vehicle-grid');
    const loader = document.getElementById('fleet-loader');

    try {
        loader.style.display = 'flex';
        grid.style.display = 'none';

        const response = await fetch(`${API_BASE_URL}/vehicles`);
        if (!response.ok) throw new Error('Falha ao carregar frota');

        const vehicles = await response.json();

        // Limpar os cards hardcoded demo
        grid.innerHTML = '';

        if (vehicles && vehicles.length > 0) {
            vehicles.forEach(vehicle => {
                const card = document.createElement('div');
                card.className = 'vehicle-card';

                // Div para imagem
                const imgWrapper = document.createElement('div');
                imgWrapper.className = 'vehicle-img-wrapper';

                const img = document.createElement('img');
                // Placeholder se vier sem imageUrls
                img.src = vehicle.imageUrls && vehicle.imageUrls.length > 0 ?
                    vehicle.imageUrls[0] :
                    'img/compact.png';
                img.alt = vehicle.model;

                const badge = document.createElement('span');
                badge.className = 'badge badge-primary';
                badge.textContent = vehicle.category;

                imgWrapper.appendChild(img);
                imgWrapper.appendChild(badge);

                // Div de info
                const info = document.createElement('div');
                info.className = 'vehicle-info';

                const title = document.createElement('h3');
                title.textContent = `${vehicle.brand} ${vehicle.model} ${vehicle.year}`;

                const features = document.createElement('div');
                features.className = 'vehicle-features';

                const feature1 = document.createElement('span');
                feature1.innerHTML = `<i data-lucide="users"></i> ${vehicle.seats} Lugares`;

                const feature2 = document.createElement('span');
                feature2.innerHTML = `<i data-lucide="settings-2"></i> ${vehicle.transmission === 'AUTOMATIC' ? 'Automático' : 'Manual'}`;

                const feature3 = document.createElement('span');
                feature3.innerHTML = `<i data-lucide="fuel"></i> ${vehicle.fuelType}`;

                features.appendChild(feature1);
                features.appendChild(feature2);
                features.appendChild(feature3);

                // Preço e Ação
                const priceAction = document.createElement('div');
                priceAction.className = 'vehicle-price-action';

                const price = document.createElement('div');
                price.className = 'price';

                const currency = document.createElement('span');
                currency.className = 'currency';
                currency.textContent = 'R$';

                const amount = document.createElement('span');
                amount.className = 'amount';
                amount.textContent = vehicle.dailyRate;

                const period = document.createElement('span');
                period.className = 'period';
                period.textContent = '/dia';

                price.appendChild(currency);
                price.appendChild(amount);
                price.appendChild(period);

                const btn = document.createElement('button');
                btn.className = 'btn btn-primary';
                btn.textContent = 'Reservar';

                priceAction.appendChild(price);
                priceAction.appendChild(btn);

                info.appendChild(title);
                info.appendChild(features);
                priceAction.appendChild(price);
                priceAction.appendChild(btn);

                info.appendChild(title);
                info.appendChild(features);
                info.appendChild(priceAction);

                card.appendChild(imgWrapper);
                card.appendChild(info);

                grid.appendChild(card);
            });

            // Recriar ícones do Lucide
            lucide.createIcons();
        } else {
            grid.innerHTML = '<p class="error-msg">Nenhum veículo disponível no momento.</p>';
        }

        loader.style.display = 'none';
        grid.style.display = 'grid';

    } catch (error) {
        console.error('Erro de conexão com API ao carregar frota:', error);
        loader.style.display = 'none';
        grid.innerHTML = '<p class="error-msg">Erro ao carregar veículos. Tente novamente mais tarde.</p>';
        grid.style.display = 'block';
    }
}
