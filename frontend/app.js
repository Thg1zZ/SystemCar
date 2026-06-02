/**
 * RodaLivre - Vanilla JS Application Logic
 */

let API_BASE_URL = 'https://systemcar-backend.onrender.com/api/v1';

// Função assíncrona para detectar backend local de forma dinâmica e resiliente
async function detectApiUrl() {
    if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
        try {
            const controller = new AbortController();
            const timeoutId = setTimeout(() => controller.abort(), 800); // 800ms de limite
            const response = await fetch('http://localhost:8080/api/v1/branches', { signal: controller.signal });
            clearTimeout(timeoutId);
            if (response.ok) {
                API_BASE_URL = 'http://localhost:8080/api/v1';
                console.log('Backend local ativo. Usando API local:', API_BASE_URL);
                return;
            }
        } catch (e) {
            console.log('Backend local offline. Usando fallback de produção:', API_BASE_URL);
        }
    } else {
        console.log('Acesso em produção. Usando API na nuvem:', API_BASE_URL);
    }
}

let cachedBranches = [];
let currentUserToken = localStorage.getItem('jwt_token') || null;
let currentUserInfo = null;

document.addEventListener('DOMContentLoaded', async () => {
    // 0. Detectar a API correta de forma assíncrona e resiliente
    await detectApiUrl();

    // Carregar informações do localStorage
    if (localStorage.getItem('user_info')) {
        try {
            currentUserInfo = JSON.parse(localStorage.getItem('user_info'));
        } catch(e) {
            currentUserInfo = null;
        }
    }

    // 1. Initializations
    initSplashScreen();
    initHeaderScroll();
    initInfoModals(); // Inicializa ajuda, termos, privacidade e sobre nós
    initAuth();       // Inicializa login, cadastro e visualização do cabeçalho
    initBooking();    // Inicializa fechamento de modal e submit de reservas
    initAdminDashboard(); // Inicializa controle do painel de administração
    loadBranches();
    loadVehicles();

    // 2. Event Listeners
    setupSearchForm();
});

function initSplashScreen() {
    const splash = document.getElementById('splash-screen');
    if (!splash) return;

    // O tempo coincide com a finalização das animações (4.5 segundos)
    setTimeout(() => {
        splash.classList.add('fade-out');
        setTimeout(() => {
            splash.style.display = 'none';
            showPortfolioModal(); // Dispara o modal de consentimento legal do portfólio
        }, 800); // tempo correspondente à transição de fade CSS
    }, 4500);
}

/**
 * Handles language switching and acceptance click events in the Portfolio Consent Modal
 */
function showPortfolioModal() {
    const modal = document.getElementById('portfolio-modal');
    if (!modal) return;
    
    // Mostra o modal de bloqueio de tela com efeito scale-in
    modal.classList.add('show');
    
    const btnPt = document.getElementById('btn-lang-pt');
    const btnEn = document.getElementById('btn-lang-en');
    const btnEs = document.getElementById('btn-lang-es');
    const btnZh = document.getElementById('btn-lang-zh');
    
    const ptContent = document.getElementById('lang-content-pt');
    const enContent = document.getElementById('lang-content-en');
    const esContent = document.getElementById('lang-content-es');
    const zhContent = document.getElementById('lang-content-zh');
    
    const allBtns = [btnPt, btnEn, btnEs, btnZh];
    const allContents = [ptContent, enContent, esContent, zhContent];
    
    const switchLanguage = (activeBtn, activeContent) => {
        allBtns.forEach(btn => btn.classList.remove('active'));
        allContents.forEach(content => content.style.display = 'none');
        activeBtn.classList.add('active');
        activeContent.style.display = 'block';
    };
    
    // Toggles de idiomas em tempo real
    btnPt.addEventListener('click', () => switchLanguage(btnPt, ptContent));
    btnEn.addEventListener('click', () => switchLanguage(btnEn, enContent));
    btnEs.addEventListener('click', () => switchLanguage(btnEs, esContent));
    btnZh.addEventListener('click', () => switchLanguage(btnZh, zhContent));
    
    // Botões de consentimento/aceite para liberação do site
    const acceptTerms = () => {
        modal.classList.remove('show');
        setTimeout(() => {
            modal.style.display = 'none';
            // Ativa o badge persistente no canto inferior esquerdo
            const badge = document.querySelector('.portfolio-badge');
            if (badge) {
                badge.style.display = 'flex';
                setTimeout(() => {
                    badge.style.opacity = '1';
                }, 50);
            }
        }, 500); // tempo correspondente à transição de opacidade do modal
    };
    
    document.getElementById('btn-accept-portfolio').addEventListener('click', acceptTerms);
    document.getElementById('btn-accept-portfolio-en').addEventListener('click', acceptTerms);
    document.getElementById('btn-accept-portfolio-es').addEventListener('click', acceptTerms);
    document.getElementById('btn-accept-portfolio-zh').addEventListener('click', acceptTerms);
}

/**
 * Handles opening and closing of informational document modals
 */
function initInfoModals() {
    const setupModal = (linkId, modalId, closeId) => {
        const link = document.getElementById(linkId);
        const modal = document.getElementById(modalId);
        const close = document.getElementById(closeId);
        
        if (!link || !modal || !close) return;
        
        link.addEventListener('click', (e) => {
            e.preventDefault();
            modal.classList.add('show');
        });
        
        close.addEventListener('click', () => {
            modal.classList.remove('show');
        });
        
        // Fecha ao clicar no fundo escuro
        modal.addEventListener('click', (e) => {
            if (e.target === modal) {
                modal.classList.remove('show');
            }
        });
    };
    
    setupModal('link-help', 'help-modal', 'btn-close-help');
    setupModal('link-terms', 'terms-modal', 'btn-close-terms');
    setupModal('link-privacy', 'privacy-modal', 'btn-close-privacy');
    setupModal('link-about', 'about-modal', 'btn-close-about');
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
        cachedBranches = branches;

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
                 
                // Mapeia imagens reais (Unsplash) ou ilustrações locais de forma ultra-segura
                let vehicleImg = 'img/compact.png';
                if (vehicle.imageUrls) {
                    try {
                        const parsed = JSON.parse(vehicle.imageUrls);
                        if (Array.isArray(parsed) && parsed.length > 0 && parsed[0].startsWith('http')) {
                            vehicleImg = parsed[0];
                        }
                    } catch (e) {
                        if (typeof vehicle.imageUrls === 'string' && vehicle.imageUrls.startsWith('http')) {
                            vehicleImg = vehicle.imageUrls;
                        }
                    }
                }
                
                // Caso não possua imagens reais válidas, utiliza os assets locais organizados por categoria
                if (vehicleImg === 'img/compact.png' && vehicle.category && typeof vehicle.category === 'string') {
                    const cat = vehicle.category.toUpperCase();
                    if (cat.includes('SUV') || cat.includes('TRUCK')) {
                        vehicleImg = 'img/suv.png';
                    } else if (cat.includes('LUXURY') || cat.includes('SPORTS')) {
                        vehicleImg = 'img/luxo.png';
                    } else if (cat.includes('INTERMEDIATE') || cat.includes('FULL_SIZE') || cat.includes('SEDAN') || cat.includes('VAN')) {
                        vehicleImg = 'img/sedan.png';
                    }
                }
                img.src = vehicleImg;
                img.alt = vehicle.model || 'Carro';

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

                // Ouvinte de clique direto no botão Reservar
                btn.addEventListener('click', () => {
                    openBookingModal(vehicle);
                });

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
        setupFleetFilters(); // Inicializa os filtros dinâmicos na tela

    } catch (error) {
        console.error('Erro de conexão com API ao carregar frota:', error);
        loader.style.display = 'none';
        grid.innerHTML = '<p class="error-msg">Erro ao carregar veículos. Tente novamente mais tarde.</p>';
        grid.style.display = 'block';
    }
}

/**
 * Handle Fleet Category Filtering dynamically
 */
function setupFleetFilters() {
    const filters = document.querySelectorAll('.filter-btn');
    filters.forEach(btn => {
        // Remove listeners antigos para evitar duplicações caso recarregue a API
        const newBtn = btn.cloneNode(true);
        btn.parentNode.replaceChild(newBtn, btn);
        
        newBtn.addEventListener('click', () => {
            document.querySelectorAll('.filter-btn').forEach(f => f.classList.remove('active'));
            newBtn.classList.add('active');
            
            const filterValue = newBtn.getAttribute('data-filter').toLowerCase();
            const cards = document.querySelectorAll('.vehicle-card');
            
            cards.forEach(card => {
                const categoryBadge = card.querySelector('.badge').textContent.toLowerCase();
                if (filterValue === 'all') {
                    card.style.display = 'block';
                } else if (filterValue === 'suv' && categoryBadge.includes('suv')) {
                    card.style.display = 'block';
                } else if (filterValue === 'sedan' && (categoryBadge.includes('sedan') || categoryBadge.includes('intermediate') || categoryBadge.includes('full_size'))) {
                    card.style.display = 'block';
                } else if (filterValue === 'luxo' && (categoryBadge.includes('luxury') || categoryBadge.includes('sports') || categoryBadge.includes('luxo'))) {
                    card.style.display = 'block';
                } else {
                    card.style.display = 'none';
                }
            });
        });
    });
}

/**
 * Initialize Authentication Forms, fast-switch navigation links and DOM Listeners
 */
function initAuth() {
    const loginModal = document.getElementById('login-modal');
    const registerModal = document.getElementById('register-modal');
    const closeLogin = document.getElementById('btn-close-login');
    const closeRegister = document.getElementById('btn-close-register');
    
    const openLogin = () => {
        loginModal.classList.add('show');
        document.getElementById('login-error').style.display = 'none';
    };
    
    const openRegister = () => {
        registerModal.classList.add('show');
        document.getElementById('register-error').style.display = 'none';
    };
    
    // Configura botões de fechar dos modais
    if (closeLogin) closeLogin.addEventListener('click', () => loginModal.classList.remove('show'));
    if (closeRegister) closeRegister.addEventListener('click', () => registerModal.classList.remove('show'));
    
    // Fechar ao clicar no fundo escuro
    if (loginModal) {
        loginModal.addEventListener('click', (e) => {
            if (e.target === loginModal) loginModal.classList.remove('show');
        });
    }
    if (registerModal) {
        registerModal.addEventListener('click', (e) => {
            if (e.target === registerModal) registerModal.classList.remove('show');
        });
    }

    // Troca rápida de modais ("Não tem conta? Cadastre-se" e "Já possui conta? Entrar")
    const goRegister = document.getElementById('link-go-register');
    const goLogin = document.getElementById('link-go-login');
    
    if (goRegister) {
        goRegister.addEventListener('click', (e) => {
            e.preventDefault();
            loginModal.classList.remove('show');
            openRegister();
        });
    }
    if (goLogin) {
        goLogin.addEventListener('click', (e) => {
            e.preventDefault();
            registerModal.classList.remove('show');
            openLogin();
        });
    }

    // Formulário de Login
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const email = document.getElementById('login-email').value;
            const password = document.getElementById('login-password').value;
            const errorDiv = document.getElementById('login-error');
            const submitBtn = loginForm.querySelector('button[type="submit"]');
            
            errorDiv.style.display = 'none';
            submitBtn.disabled = true;
            submitBtn.textContent = 'Autenticando...';
            
            try {
                const response = await fetch(`${API_BASE_URL}/auth/login`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, password })
                });
                
                if (!response.ok) {
                    const errText = await response.text();
                    let errMsg = 'E-mail ou senha incorretos.';
                    try {
                        const errJson = JSON.parse(errText);
                        if (errJson.message) errMsg = errJson.message;
                    } catch(e) {}
                    throw new Error(errMsg);
                }
                
                const data = await response.json();
                localStorage.setItem('jwt_token', data.token);
                localStorage.setItem('user_info', JSON.stringify({
                    id: data.id,
                    email: data.email,
                    fullName: data.fullName,
                    roles: data.roles
                }));
                
                loginModal.classList.remove('show');
                loginForm.reset();
                
                currentUserToken = data.token;
                currentUserInfo = data;
                updateAuthHeader();
                
            } catch(err) {
                console.error('Erro no login:', err);
                errorDiv.textContent = err.message || 'Erro de conexão. Tente novamente.';
                errorDiv.style.display = 'block';
            } finally {
                submitBtn.disabled = false;
                submitBtn.textContent = 'Acessar';
            }
        });
    }

    // Formulário de Cadastro
    const registerForm = document.getElementById('register-form');
    if (registerForm) {
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const fullName = document.getElementById('reg-name').value;
            const email = document.getElementById('reg-email').value;
            const password = document.getElementById('reg-password').value;
            const birthDate = document.getElementById('reg-birth-date').value;
            const cpf = document.getElementById('reg-cpf').value;
            const cnh = document.getElementById('reg-cnh').value;
            const cnhExpirationDate = document.getElementById('reg-cnh-expiration').value;
            const errorDiv = document.getElementById('register-error');
            const submitBtn = registerForm.querySelector('button[type="submit"]');
            
            errorDiv.style.display = 'none';
            submitBtn.disabled = true;
            submitBtn.textContent = 'Cadastrando...';
            
            try {
                if (password.length < 6) {
                    throw new Error('A senha deve ter pelo menos 6 caracteres.');
                }
                if (cnh.replace(/\D/g, '').length !== 11) {
                    throw new Error('A CNH deve ter exatamente 11 dígitos.');
                }
                
                const response = await fetch(`${API_BASE_URL}/auth/register`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        fullName,
                        email,
                        password,
                        cpf,
                        cnh,
                        cnhExpirationDate,
                        birthDate
                    })
                });
                
                if (!response.ok) {
                    const errText = await response.text();
                    let errMsg = 'Erro ao realizar cadastro.';
                    try {
                        const errJson = JSON.parse(errText);
                        if (errJson.message) errMsg = errJson.message;
                        else if (typeof errJson === 'string') errMsg = errJson;
                    } catch(e) {
                        if (errText) errMsg = errText;
                    }
                    throw new Error(errMsg);
                }
                
                alert('Conta criada com sucesso! Faça login para continuar.');
                registerModal.classList.remove('show');
                registerForm.reset();
                
                openLogin();
                
            } catch(err) {
                console.error('Erro no cadastro:', err);
                errorDiv.textContent = err.message || 'Erro de conexão. Tente novamente.';
                errorDiv.style.display = 'block';
            } finally {
                submitBtn.disabled = false;
                submitBtn.textContent = 'Cadastrar';
            }
        });
    }

    updateAuthHeader();
}

/**
 * Handle Premium authentication state visibility toggles in the header
 */
function updateAuthHeader() {
    const authContainer = document.querySelector('.auth-buttons');
    if (!authContainer) return;

    const token = localStorage.getItem('jwt_token');
    const userInfoStr = localStorage.getItem('user_info');
    
    if (token && userInfoStr) {
        try {
            const userInfo = JSON.parse(userInfoStr);
            const firstName = userInfo.fullName.split(' ')[0];
            
            // Verifica se o usuário tem permissão de Admin ou Operador
            const isAdmin = userInfo.roles && (
                userInfo.roles.includes('ROLE_ADMIN') || 
                userInfo.roles.includes('ROLE_OPERATOR') ||
                userInfo.roles.includes('ADMIN') ||
                userInfo.roles.includes('OPERATOR')
            );
            
            if (isAdmin) {
                authContainer.innerHTML = `
                    <span class="user-greeting" style="color: var(--text-main); font-weight: 500; margin-right: 1.2rem; font-size: 0.95rem;">
                        Olá, <strong style="color: var(--primary); font-weight: 700;">${firstName} (Admin)</strong>
                    </span>
                    <button class="btn btn-primary" id="btn-admin-dashboard" style="padding: 0.5rem 1rem; font-size: 0.9rem; margin-right: 0.8rem; display: inline-flex; align-items: center; gap: 0.4rem;">
                        <i data-lucide="layout-dashboard" style="width: 16px; height: 16px;"></i> Painel Admin
                    </button>
                    <button class="btn btn-outline" id="btn-logout" style="padding: 0.5rem 1rem; font-size: 0.9rem;">Sair</button>
                `;
                
                // Abre o painel
                document.getElementById('btn-admin-dashboard').addEventListener('click', () => {
                    openAdminDashboard();
                });
            } else {
                authContainer.innerHTML = `
                    <span class="user-greeting" style="color: var(--text-main); font-weight: 500; margin-right: 1.2rem; font-size: 0.95rem;">
                        Olá, <strong style="color: var(--primary); font-weight: 700;">${firstName}</strong>
                    </span>
                    <button class="btn btn-outline" id="btn-logout" style="padding: 0.5rem 1rem; font-size: 0.9rem;">Sair</button>
                `;
            }
            
            // Logout click listener
            document.getElementById('btn-logout').addEventListener('click', () => {
                localStorage.removeItem('jwt_token');
                localStorage.removeItem('user_info');
                currentUserToken = null;
                currentUserInfo = null;
                updateAuthHeader();
            });
            
            lucide.createIcons();
            
        } catch(e) {
            localStorage.removeItem('jwt_token');
            localStorage.removeItem('user_info');
            renderGuestHeader(authContainer);
        }
    } else {
        renderGuestHeader(authContainer);
    }
}

/**
 * Redraw header guest state buttons and rebind click events
 */
function renderGuestHeader(container) {
    container.innerHTML = `
        <button class="btn btn-outline" id="btn-login">Entrar</button>
        <button class="btn btn-primary" id="btn-register">Cadastrar</button>
    `;
    
    const btnLogin = document.getElementById('btn-login');
    const btnRegister = document.getElementById('btn-register');
    const loginModal = document.getElementById('login-modal');
    const registerModal = document.getElementById('register-modal');
    
    if (btnLogin) btnLogin.addEventListener('click', () => {
        loginModal.classList.add('show');
        document.getElementById('login-error').style.display = 'none';
    });
    if (btnRegister) btnRegister.addEventListener('click', () => {
        registerModal.classList.add('show');
        document.getElementById('register-error').style.display = 'none';
    });
}

/**
 * Initialize vehicle booking interactions
 */
function initBooking() {
    const bookingModal = document.getElementById('booking-modal');
    const closeBooking = document.getElementById('btn-close-booking');
    const closeSuccess = document.getElementById('btn-close-success');
    const bookingForm = document.getElementById('booking-form');
    
    if (closeBooking) {
        closeBooking.addEventListener('click', () => {
            bookingModal.classList.remove('show');
        });
    }
    
    if (closeSuccess) {
        closeSuccess.addEventListener('click', () => {
            bookingModal.classList.remove('show');
        });
    }
    
    if (bookingModal) {
        bookingModal.addEventListener('click', (e) => {
            if (e.target === bookingModal) {
                bookingModal.classList.remove('show');
            }
        });
    }
    
    if (bookingForm) {
        bookingForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            
            const token = localStorage.getItem('jwt_token');
            if (!token) {
                alert('Você precisa estar logado para efetuar reservas.');
                bookingModal.classList.remove('show');
                return;
            }
            
            const errorDiv = document.getElementById('booking-error');
            const submitBtn = bookingForm.querySelector('button[type="submit"]');
            
            const vehicleId = document.getElementById('booking-vehicle-id').value;
            const pickupBranchId = document.getElementById('booking-pickup').value;
            const returnBranchId = document.getElementById('booking-return').value;
            const pickupDateVal = document.getElementById('booking-pickup-date').value;
            const returnDateVal = document.getElementById('booking-return-date').value;
            
            errorDiv.style.display = 'none';
            submitBtn.disabled = true;
            submitBtn.textContent = 'Finalizando Pedido...';
            
            try {
                const pickup = new Date(pickupDateVal);
                const ret = new Date(returnDateVal);
                
                if (ret <= pickup) {
                    throw new Error('A data de devolução deve ser posterior à data de retirada!');
                }
                
                const now = new Date();
                if (pickup < now) {
                    throw new Error('A data de retirada deve estar no futuro!');
                }
                
                // Formata datas para o LocalDateTime esperado pelo Spring Boot
                const pickupDateStr = pickupDateVal.replace(' ', 'T');
                const returnDateStr = returnDateVal.replace(' ', 'T');
                
                const payload = {
                    vehicleId: vehicleId,
                    pickupBranchId: pickupBranchId,
                    returnBranchId: returnBranchId,
                    pickupDate: pickupDateStr,
                    returnDate: returnDateStr,
                    additionals: []
                };
                
                const response = await fetch(`${API_BASE_URL}/rentals`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}`
                    },
                    body: JSON.stringify(payload)
                });
                
                if (!response.ok) {
                    const errText = await response.text();
                    let errMsg = 'Falha ao finalizar reserva.';
                    try {
                        const errJson = JSON.parse(errText);
                        if (errJson.message) errMsg = errJson.message;
                        else if (typeof errJson === 'string') errMsg = errJson;
                    } catch(e) {
                        if (errText) errMsg = errText;
                    }
                    throw new Error(errMsg);
                }
                
                document.getElementById('booking-modal-content').style.display = 'none';
                document.getElementById('booking-success-content').style.display = 'block';
                bookingForm.reset();
                
            } catch(err) {
                console.error('Erro ao efetuar reserva:', err);
                errorDiv.textContent = err.message || 'Erro ao processar reserva. Tente novamente.';
                errorDiv.style.display = 'block';
            } finally {
                submitBtn.disabled = false;
                submitBtn.textContent = 'Finalizar Pedido';
            }
        });
    }
}

/**
 * Handle details injection and modal toggle upon vehicle card click
 */
function openBookingModal(vehicle) {
    const token = localStorage.getItem('jwt_token');
    if (!token) {
        const loginModal = document.getElementById('login-modal');
        if (loginModal) {
            loginModal.classList.add('show');
            const errorDiv = document.getElementById('login-error');
            errorDiv.textContent = 'Para prosseguir com a reserva, faça o login ou crie uma conta.';
            errorDiv.style.background = 'rgba(255, 90, 31, 0.1)';
            errorDiv.style.color = 'var(--primary)';
            errorDiv.style.display = 'block';
        }
        return;
    }
    
    const bookingModal = document.getElementById('booking-modal');
    if (!bookingModal) return;
    
    bookingModal.classList.add('show');
    document.getElementById('booking-modal-content').style.display = 'block';
    document.getElementById('booking-success-content').style.display = 'none';
    document.getElementById('booking-error').style.display = 'none';
    
    let vehicleImg = 'img/compact.png';
    if (vehicle.imageUrls) {
        try {
            const parsed = JSON.parse(vehicle.imageUrls);
            if (Array.isArray(parsed) && parsed.length > 0 && parsed[0].startsWith('http')) {
                vehicleImg = parsed[0];
            }
        } catch (e) {
            if (typeof vehicle.imageUrls === 'string' && vehicle.imageUrls.startsWith('http')) {
                vehicleImg = vehicle.imageUrls;
            }
        }
    }
    if (vehicleImg === 'img/compact.png' && vehicle.category && typeof vehicle.category === 'string') {
        const cat = vehicle.category.toUpperCase();
        if (cat.includes('SUV') || cat.includes('TRUCK')) {
            vehicleImg = 'img/suv.png';
        } else if (cat.includes('LUXURY') || cat.includes('SPORTS')) {
            vehicleImg = 'img/luxo.png';
        } else if (cat.includes('INTERMEDIATE') || cat.includes('FULL_SIZE') || cat.includes('SEDAN') || cat.includes('VAN')) {
            vehicleImg = 'img/sedan.png';
        }
    }
    
    document.getElementById('booking-car-img').src = vehicleImg;
    document.getElementById('booking-car-img').alt = vehicle.model || 'Carro';
    document.getElementById('booking-car-name').textContent = `${vehicle.brand} ${vehicle.model} ${vehicle.year}`;
    document.getElementById('booking-car-rate').textContent = `R$ ${vehicle.dailyRate}/dia`;
    document.getElementById('booking-vehicle-id').value = vehicle.id;
    
    const pickupSelect = document.getElementById('booking-pickup');
    const returnSelect = document.getElementById('booking-return');
    
    pickupSelect.innerHTML = '';
    returnSelect.innerHTML = '';
    
    if (cachedBranches && cachedBranches.length > 0) {
        cachedBranches.forEach(branch => {
            const opt1 = document.createElement('option');
            opt1.value = branch.id;
            opt1.textContent = `${branch.name} - ${branch.city}/${branch.state}`;
            pickupSelect.appendChild(opt1);
            
            const opt2 = document.createElement('option');
            opt2.value = branch.id;
            opt2.textContent = `${branch.name} - ${branch.city}/${branch.state}`;
            returnSelect.appendChild(opt2);
        });
    } else {
        pickupSelect.innerHTML = '<option value="" disabled>Nenhuma agência disponível</option>';
        returnSelect.innerHTML = '<option value="" disabled>Nenhuma agência disponível</option>';
    }
    
    const heroPickupDate = document.getElementById('pickup-date').value;
    const heroReturnDate = document.getElementById('return-date').value;
    const heroPickupBranch = document.getElementById('pickup-location').value;
    
    if (heroPickupDate) {
        document.getElementById('booking-pickup-date').value = heroPickupDate;
    } else {
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        tomorrow.setHours(9, 0, 0, 0);
        document.getElementById('booking-pickup-date').value = tomorrow.toISOString().slice(0, 16);
    }
    
    if (heroReturnDate) {
        document.getElementById('booking-return-date').value = heroReturnDate;
    } else {
        const afterTomorrow = new Date();
        afterTomorrow.setDate(afterTomorrow.getDate() + 2);
        afterTomorrow.setHours(9, 0, 0, 0);
        document.getElementById('booking-return-date').value = afterTomorrow.toISOString().slice(0, 16);
    }
    
    if (heroPickupBranch && cachedBranches.some(b => b.id === heroPickupBranch)) {
        pickupSelect.value = heroPickupBranch;
        returnSelect.value = heroPickupBranch;
    }
}

/**
 * Admin Dashboard System
 */
/**
 * Admin Dashboard System & Simulated Operations
 */

// Helpers de Privacidade e Máscara para Ambiente Fictício
function maskCnh(cnh) {
    if (!cnh) return '***.***.***';
    const clean = cnh.replace(/\D/g, '');
    if (clean.length < 5) return '***.***.***-**';
    return `***.***.${clean.slice(-5, -2)}-${clean.slice(-2)}`;
}

function maskCpf(cpf) {
    if (!cpf) return '***.***.***-**';
    const clean = cpf.replace(/\D/g, '');
    if (clean.length < 5) return '***.***.***-**';
    return `***.***.${clean.slice(-5, -2)}-${clean.slice(-2)}`;
}

function abbreviateName(fullName) {
    if (!fullName) return 'Cliente Fictício';
    const parts = fullName.trim().split(' ');
    if (parts.length === 1) return parts[0];
    if (parts.length === 2) return `${parts[0]} ${parts[1][0]}.`;
    return `${parts[0]} ${parts[1][0]}. ${parts[parts.length - 1][0]}.`;
}

function initAdminDashboard() {
    const closeBtn = document.getElementById('btn-close-admin-dashboard');
    const modal = document.getElementById('admin-dashboard-modal');
    
    if (closeBtn && modal) {
        closeBtn.addEventListener('click', () => modal.classList.remove('show'));
        modal.addEventListener('click', (e) => {
            if (e.target === modal) modal.classList.remove('show');
        });
    }
    
    // Navegação de abas
    const navItems = document.querySelectorAll('.admin-nav-item');
    const tabContents = document.querySelectorAll('.admin-tab-content');
    
    navItems.forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            navItems.forEach(n => n.classList.remove('active'));
            tabContents.forEach(t => t.classList.remove('active'));
            
            item.classList.add('active');
            const tabId = item.getAttribute('data-tab');
            const targetTab = document.getElementById(tabId);
            if (targetTab) {
                targetTab.classList.add('active');
                if (tabId === 'tab-rentals') {
                    loadAdminDashboardRentals();
                } else if (tabId === 'tab-fleet') {
                    loadAdminDashboardFleet();
                }
            }
        });
    });

    // Inicialização das Modais de Formulários
    setupAdminForms();
}

function setupAdminForms() {
    // 1. Modais de Veículos
    const vehModal = document.getElementById('vehicle-form-modal');
    const closeVeh = document.getElementById('btn-close-vehicle-modal');
    const addVehBtn = document.getElementById('btn-admin-add-vehicle');
    const vehForm = document.getElementById('vehicle-form');
    
    if (closeVeh) closeVeh.addEventListener('click', () => vehModal.classList.remove('show'));
    if (addVehBtn) {
        addVehBtn.addEventListener('click', () => {
            vehForm.reset();
            document.getElementById('admin-vehicle-id').value = '';
            document.getElementById('vehicle-modal-title').textContent = 'Cadastrar Veículo';
            vehModal.classList.add('show');
        });
    }
    
    if (vehForm) {
        vehForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const token = localStorage.getItem('jwt_token');
            if (!token) return;
            
            const id = document.getElementById('admin-vehicle-id').value;
            const brand = document.getElementById('admin-veh-brand').value;
            const model = document.getElementById('admin-veh-model').value;
            const year = parseInt(document.getElementById('admin-veh-year').value);
            const licensePlate = document.getElementById('admin-veh-plate').value;
            const category = document.getElementById('admin-veh-category').value;
            const transmission = document.getElementById('admin-veh-transmission').value;
            const fuelType = document.getElementById('admin-veh-fuel').value;
            const seats = parseInt(document.getElementById('admin-veh-seats').value);
            const dailyRate = parseFloat(document.getElementById('admin-veh-rate').value);
            
            const payload = { brand, model, year, licensePlate, category, transmission, fuelType, seats, dailyRate };
            
            try {
                let response;
                if (id) {
                    response = await fetch(`${API_BASE_URL}/vehicles/${id}`, {
                        method: 'PUT',
                        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
                        body: JSON.stringify(payload)
                    });
                } else {
                    response = await fetch(`${API_BASE_URL}/vehicles`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
                        body: JSON.stringify(payload)
                    });
                }
                
                if (response.ok) {
                    alert('Veículo salvo com sucesso!');
                    vehModal.classList.remove('show');
                    loadAdminDashboardFleet();
                    loadVehicles(); // Atualiza frota principal
                    loadAdminDashboardData(); // Atualiza KPIs
                } else {
                    const txt = await response.text();
                    alert('Erro ao salvar veículo: ' + txt);
                }
            } catch(err) {
                console.error(err);
                alert('Erro na chamada da API.');
            }
        });
    }

    // 2. Modal de Manutenções
    const maintModal = document.getElementById('maintenance-form-modal');
    const closeMaint = document.getElementById('btn-close-maintenance-modal');
    const maintForm = document.getElementById('maintenance-form');
    
    if (closeMaint) closeMaint.addEventListener('click', () => maintModal.classList.remove('show'));
    
    if (maintForm) {
        maintForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const token = localStorage.getItem('jwt_token');
            if (!token) return;
            
            const vehicleId = document.getElementById('maintenance-vehicle-id').value;
            const type = document.getElementById('maint-type').value;
            const provider = document.getElementById('maint-provider').value;
            const cost = parseFloat(document.getElementById('maint-cost').value);
            const notes = document.getElementById('maint-notes').value;
            
            const payload = { vehicleId, type, provider, cost, notes };
            
            try {
                const response = await fetch(`${API_BASE_URL}/maintenance`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
                    body: JSON.stringify(payload)
                });
                
                if (response.ok) {
                    alert('Manutenção registrada! Status do veículo atualizado.');
                    maintModal.classList.remove('show');
                    loadAdminDashboardFleet();
                    loadVehicles();
                    loadAdminDashboardData();
                } else {
                    const txt = await response.text();
                    alert('Erro ao iniciar manutenção: ' + txt);
                }
            } catch(err) {
                console.error(err);
            }
        });
    }

    // 3. Modal de Devoluções
    const retModal = document.getElementById('return-form-modal');
    const closeRet = document.getElementById('btn-close-return-modal');
    const retForm = document.getElementById('return-form');
    
    if (closeRet) closeRet.addEventListener('click', () => retModal.classList.remove('show'));
    
    if (retForm) {
        retForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const token = localStorage.getItem('jwt_token');
            if (!token) return;
            
            const id = document.getElementById('return-rental-id').value;
            const kilometers = parseInt(document.getElementById('return-km').value);
            const returnBranchId = document.getElementById('return-branch').value;
            
            const payload = { kilometers, returnBranchId };
            
            try {
                const response = await fetch(`${API_BASE_URL}/rentals/${id}/return`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
                    body: JSON.stringify(payload)
                });
                
                if (response.ok) {
                    const data = await response.json();
                    retModal.classList.remove('show');
                    
                    // Exibir Recibo Premium Fictício de Proteção
                    showReceipt(data);
                    
                    loadAdminDashboardRentals();
                    loadAdminDashboardData();
                    loadVehicles();
                } else {
                    const txt = await response.text();
                    alert('Erro ao finalizar devolução: ' + txt);
                }
            } catch(err) {
                console.error(err);
            }
        });
    }

    // 4. Fechamento de Recibo
    const receiptModal = document.getElementById('receipt-modal');
    const closeReceipt = document.getElementById('btn-close-receipt');
    const closeReceiptBtn = document.getElementById('btn-close-receipt-btn');
    
    const hideReceipt = () => receiptModal.classList.remove('show');
    if (closeReceipt) closeReceipt.addEventListener('click', hideReceipt);
    if (closeReceiptBtn) closeReceiptBtn.addEventListener('click', hideReceipt);
}

function showReceipt(rental) {
    const modal = document.getElementById('receipt-modal');
    if (!modal) return;
    
    // Popula campos com privacidade estrita
    document.getElementById('receipt-id').textContent = rental.id.substring(0, 8).toUpperCase();
    document.getElementById('receipt-client').textContent = rental.user ? abbreviateName(rental.user.fullName) : 'Cliente Anonimizado';
    document.getElementById('receipt-car').textContent = rental.vehicle ? `${rental.vehicle.brand} ${rental.vehicle.model}` : 'Veículo';
    document.getElementById('receipt-km-start').textContent = `${rental.vehicle ? rental.vehicle.kilometers - 120 : 0} km`; // Simulação
    document.getElementById('receipt-km-end').textContent = `${rental.vehicle ? rental.vehicle.kilometers : 0} km`;
    
    // Cálculo financeiro simulado/real vindo do backend
    const days = Math.max(1, Math.round((new Date(rental.returnDate) - new Date(rental.pickupDate)) / (1000 * 60 * 60 * 24)));
    document.getElementById('receipt-days').textContent = days;
    
    const rate = rental.vehicle ? rental.vehicle.dailyRate : 0;
    const subtotal = rate * days;
    document.getElementById('receipt-subtotal').textContent = subtotal.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    
    const fines = rental.fines || 0;
    document.getElementById('receipt-fines').textContent = fines.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    
    const total = subtotal + fines;
    document.getElementById('receipt-total').textContent = total.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    
    modal.classList.add('show');
}

function openAdminDashboard() {
    const modal = document.getElementById('admin-dashboard-modal');
    if (!modal) return;
    
    modal.classList.add('show');
    
    // Reseta abas para a primeira
    document.querySelectorAll('.admin-nav-item').forEach(n => n.classList.remove('active'));
    document.querySelectorAll('.admin-tab-content').forEach(t => t.classList.remove('active'));
    document.querySelector('.admin-nav-item[data-tab="tab-overview"]').classList.add('active');
    document.getElementById('tab-overview').classList.add('active');
    
    // Carrega dados dinâmicos do Dashboard
    loadAdminDashboardData();
}

async function loadAdminDashboardData() {
    const token = localStorage.getItem('jwt_token');
    if (!token) return;
    
    // 1. Carrega métricas da API oficial
    try {
        const response = await fetch(`${API_BASE_URL}/dashboard/metrics`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const metrics = await response.json();
            
            // Renderiza KPIs
            document.getElementById('kpi-active-rentals').textContent = metrics.activeRentals;
            document.getElementById('kpi-total-customers').textContent = metrics.totalCustomers;
            document.getElementById('kpi-total-vehicles').textContent = `${metrics.availableVehicles + metrics.rentedVehicles + metrics.vehiclesInMaintenance} / ${metrics.totalVehicles}`;
            
            const revenue = metrics.currentMonthRevenue ? parseFloat(metrics.currentMonthRevenue).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' }) : 'R$ 0,00';
            document.getElementById('kpi-revenue').textContent = revenue;
            
            // Renderiza distribuição da frota
            const total = metrics.totalVehicles || 1;
            const pctAvailable = Math.round((metrics.availableVehicles / total) * 100);
            const pctRented = Math.round((metrics.rentedVehicles / total) * 100);
            const pctMaintenance = Math.round((metrics.vehiclesInMaintenance / total) * 100);
            
            document.getElementById('progress-available').style.width = `${pctAvailable}%`;
            document.getElementById('progress-rented').style.width = `${pctRented}%`;
            document.getElementById('progress-maintenance').style.width = `${pctMaintenance}%`;
            
            document.getElementById('count-available').textContent = metrics.availableVehicles;
            document.getElementById('count-rented').textContent = metrics.rentedVehicles;
            document.getElementById('count-maintenance').textContent = metrics.vehiclesInMaintenance;
        }
    } catch(e) {
        console.error('Erro ao carregar métricas do dashboard:', e);
    }
    
    // 2. Inicializa as duas tabelas principais da primeira aba
    loadAdminDashboardFleet();
    loadAdminDashboardCustomers();
}

async function loadAdminDashboardFleet() {
    try {
        const response = await fetch(`${API_BASE_URL}/vehicles`);
        if (response.ok) {
            const vehicles = await response.json();
            const tbody = document.querySelector('#admin-vehicles-table tbody');
            tbody.innerHTML = '';
            
            vehicles.forEach(v => {
                const tr = document.createElement('tr');
                
                const tdName = document.createElement('td');
                tdName.style.fontWeight = '600';
                tdName.textContent = `${v.brand} ${v.model}`;
                
                const tdPlate = document.createElement('td');
                tdPlate.textContent = v.licensePlate || 'N/A';
                
                const tdCategory = document.createElement('td');
                tdCategory.textContent = v.category;
                
                const tdStatus = document.createElement('td');
                const statusBadge = document.createElement('span');
                statusBadge.className = 'badge';
                
                if (v.status === 'AVAILABLE') {
                    statusBadge.className += ' badge-primary';
                    statusBadge.style.backgroundColor = 'var(--success)';
                    statusBadge.textContent = 'Disponível';
                } else if (v.status === 'RENTED') {
                    statusBadge.className += ' badge-primary';
                    statusBadge.textContent = 'Alugado';
                } else {
                    statusBadge.className += ' badge-primary';
                    statusBadge.style.backgroundColor = 'var(--danger)';
                    statusBadge.textContent = 'Manutenção';
                }
                tdStatus.appendChild(statusBadge);
                
                const tdRate = document.createElement('td');
                tdRate.textContent = parseFloat(v.dailyRate).toLocaleString('pt-BR', { minimumFractionDigits: 2 });
                
                const tdActions = document.createElement('td');
                const btnGroup = document.createElement('div');
                btnGroup.className = 'admin-action-btn-group';
                
                const btnEdit = document.createElement('button');
                btnEdit.className = 'btn-admin-action';
                btnEdit.innerHTML = '<i data-lucide="edit-3" style="width: 12px; height: 12px; vertical-align: middle;"></i> ✏️';
                btnEdit.title = 'Editar Diária e Dados';
                btnEdit.addEventListener('click', () => {
                    document.getElementById('admin-vehicle-id').value = v.id;
                    document.getElementById('admin-veh-brand').value = v.brand;
                    document.getElementById('admin-veh-model').value = v.model;
                    document.getElementById('admin-veh-year').value = v.year;
                    document.getElementById('admin-veh-plate').value = v.licensePlate;
                    document.getElementById('admin-veh-category').value = v.category;
                    document.getElementById('admin-veh-transmission').value = v.transmission;
                    document.getElementById('admin-veh-fuel').value = v.fuelType;
                    document.getElementById('admin-veh-seats').value = v.seats;
                    document.getElementById('admin-veh-rate').value = v.dailyRate;
                    document.getElementById('vehicle-modal-title').textContent = 'Editar Veículo';
                    document.getElementById('vehicle-form-modal').classList.add('show');
                });
                
                const btnMaint = document.createElement('button');
                btnMaint.className = 'btn-admin-action btn-maint';
                btnMaint.innerHTML = '<i data-lucide="tool" style="width: 12px; height: 12px; vertical-align: middle;"></i> 🔧';
                btnMaint.title = 'Registrar Manutenção';
                btnMaint.addEventListener('click', () => {
                    document.getElementById('maintenance-vehicle-id').value = v.id;
                    document.getElementById('maint-notes').value = '';
                    document.getElementById('maintenance-form-modal').classList.add('show');
                });
                
                btnGroup.appendChild(btnEdit);
                btnGroup.appendChild(btnMaint);
                tdActions.appendChild(btnGroup);
                
                tr.appendChild(tdName);
                tr.appendChild(tdPlate);
                tr.appendChild(tdCategory);
                tr.appendChild(tdStatus);
                tr.appendChild(tdRate);
                tr.appendChild(tdActions);
                
                tbody.appendChild(tr);
            });
            lucide.createIcons();
        }
    } catch(e) {
        console.error('Erro ao carregar tabela de veículos no dashboard:', e);
    }
}

async function loadAdminDashboardRentals() {
    const token = localStorage.getItem('jwt_token');
    if (!token) return;
    
    try {
        const response = await fetch(`${API_BASE_URL}/rentals`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        
        if (response.ok) {
            const rentals = await response.json();
            const tbody = document.querySelector('#admin-rentals-table tbody');
            tbody.innerHTML = '';
            
            if (rentals && rentals.length > 0) {
                rentals.forEach(r => {
                    const tr = document.createElement('tr');
                    
                    const tdClient = document.createElement('td');
                    tdClient.style.fontWeight = '600';
                    tdClient.textContent = r.user ? abbreviateName(r.user.fullName) : 'Cliente Fictício';
                    
                    const tdVehicle = document.createElement('td');
                    tdVehicle.textContent = r.vehicle ? `${r.vehicle.brand} ${r.vehicle.model}` : 'Veículo';
                    
                    const tdPickup = document.createElement('td');
                    tdPickup.textContent = new Date(r.pickupDate).toLocaleString('pt-BR');
                    
                    const tdReturn = document.createElement('td');
                    tdReturn.textContent = new Date(r.returnDate).toLocaleString('pt-BR');
                    
                    const tdStatus = document.createElement('td');
                    const statusBadge = document.createElement('span');
                    statusBadge.className = 'badge';
                    
                    if (r.status === 'ACTIVE') {
                        statusBadge.className += ' badge-primary';
                        statusBadge.textContent = 'Ativo';
                    } else if (r.status === 'COMPLETED') {
                        statusBadge.className += ' badge-primary';
                        statusBadge.style.backgroundColor = 'var(--success)';
                        statusBadge.textContent = 'Concluído';
                    } else {
                        statusBadge.className += ' badge-primary';
                        statusBadge.style.backgroundColor = 'var(--text-light)';
                        statusBadge.textContent = 'Cancelado';
                    }
                    tdStatus.appendChild(statusBadge);
                    
                    const tdActions = document.createElement('td');
                    if (r.status === 'ACTIVE') {
                        const btnReturn = document.createElement('button');
                        btnReturn.className = 'btn-admin-action';
                        btnReturn.innerHTML = 'Devolver';
                        btnReturn.addEventListener('click', () => {
                            openReturnModal(r);
                        });
                        tdActions.appendChild(btnReturn);
                    } else {
                        tdActions.textContent = 'Sem Ações';
                        tdActions.style.color = 'var(--text-light)';
                    }
                    
                    tr.appendChild(tdClient);
                    tr.appendChild(tdVehicle);
                    tr.appendChild(tdPickup);
                    tr.appendChild(tdReturn);
                    tr.appendChild(tdStatus);
                    tr.appendChild(tdActions);
                    
                    tbody.appendChild(tr);
                });
            } else {
                tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; color: var(--text-light);">Nenhum aluguel registrado.</td></tr>';
            }
        }
    } catch(e) {
        console.error('Erro ao carregar aluguéis no dashboard:', e);
    }
}

function openReturnModal(rental) {
    const modal = document.getElementById('return-form-modal');
    if (!modal) return;
    
    document.getElementById('return-rental-id').value = rental.id;
    document.getElementById('return-km').value = rental.vehicle ? rental.vehicle.kilometers : 0;
    
    const branchSelect = document.getElementById('return-branch');
    branchSelect.innerHTML = '';
    
    if (cachedBranches && cachedBranches.length > 0) {
        cachedBranches.forEach(branch => {
            const opt = document.createElement('option');
            opt.value = branch.id;
            opt.textContent = `${branch.name} - ${branch.city}/${branch.state}`;
            branchSelect.appendChild(opt);
        });
    }
    
    modal.classList.add('show');
}

function loadAdminDashboardCustomers() {
    const customersTbody = document.querySelector('#admin-customers-table tbody');
    customersTbody.innerHTML = `
        <tr>
            <td style="font-weight: 600;">Thiago G. S.</td>
            <td>***.248.109-**</td>
            <td>***.***.209</td>
            <td><span class="badge badge-primary" style="background-color: var(--primary); font-weight: 700;">DIAMANTE</span></td>
            <td style="font-weight: 700; color: var(--primary);">2.400 pts</td>
        </tr>
        <tr>
            <td style="font-weight: 600;">Maria C. F.</td>
            <td>***.382.901-**</td>
            <td>***.***.612</td>
            <td><span class="badge badge-primary" style="background-color: var(--warning); font-weight: 700;">OURO</span></td>
            <td style="font-weight: 700; color: var(--warning);">1.200 pts</td>
        </tr>
        <tr>
            <td style="font-weight: 600;">Bruno A. R.</td>
            <td>***.892.112-**</td>
            <td>***.***.293</td>
            <td><span class="badge badge-primary" style="background-color: #3b82f6; font-weight: 700;">PRATA</span></td>
            <td style="font-weight: 700; color: #3b82f6;">600 pts</td>
        </tr>
        <tr>
            <td style="font-weight: 600;">Ana J. O.</td>
            <td>***.501.374-**</td>
            <td>***.***.281</td>
            <td><span class="badge badge-primary" style="background-color: var(--text-light); font-weight: 700;">BRONZE</span></td>
            <td style="font-weight: 700; color: var(--text-light);">100 pts</td>
        </tr>
    `;
}
