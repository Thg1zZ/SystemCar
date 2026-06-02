/**
 * RodaLivre - Vanilla JS Application Logic
 */

let API_BASE_URL = 'https://systemcar-backend.onrender.com/api/v1';
let currentLanguage = localStorage.getItem('portfolio_lang') || 'pt';

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
    initClientProfile();  // Inicializa o controle do perfil do cliente
    loadBranches();
    loadVehicles();
    updateLanguageTexts(); // Atualiza avisos com a linguagem persistida

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
    
    const switchLanguage = (activeBtn, activeContent, langCode) => {
        allBtns.forEach(btn => btn.classList.remove('active'));
        allContents.forEach(content => content.style.display = 'none');
        activeBtn.classList.add('active');
        activeContent.style.display = 'block';
        
        // Atualiza e persiste o idioma escolhido
        currentLanguage = langCode;
        localStorage.setItem('portfolio_lang', langCode);
        updateLanguageTexts();
    };
    
    // Toggles de idiomas em tempo real
    btnPt.addEventListener('click', () => switchLanguage(btnPt, ptContent, 'pt'));
    btnEn.addEventListener('click', () => switchLanguage(btnEn, enContent, 'en'));
    btnEs.addEventListener('click', () => switchLanguage(btnEs, esContent, 'es'));
    btnZh.addEventListener('click', () => switchLanguage(btnZh, zhContent, 'zh'));
    
    // Inicia na linguagem previamente salva
    if (currentLanguage === 'en') {
        switchLanguage(btnEn, enContent, 'en');
    } else if (currentLanguage === 'es') {
        switchLanguage(btnEs, esContent, 'es');
    } else if (currentLanguage === 'zh') {
        switchLanguage(btnZh, zhContent, 'zh');
    } else {
        switchLanguage(btnPt, ptContent, 'pt');
    }
    
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
                    <button class="btn btn-primary" id="btn-client-profile" style="padding: 0.5rem 1rem; font-size: 0.9rem; margin-right: 0.8rem; display: inline-flex; align-items: center; gap: 0.4rem;">
                        <i data-lucide="user" style="width: 16px; height: 16px;"></i> Minha Conta
                    </button>
                    <button class="btn btn-outline" id="btn-logout" style="padding: 0.5rem 1rem; font-size: 0.9rem;">Sair</button>
                `;
                
                document.getElementById('btn-client-profile').addEventListener('click', () => {
                    openClientProfile();
                });
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
    
    let isDatesValid = false;
    if (heroPickupDate && heroReturnDate) {
        const hPickup = new Date(heroPickupDate);
        const hReturn = new Date(heroReturnDate);
        const now = new Date();
        // Permite a mesma data se as horas não passarem no check de dia inteiro, mas a regra exige > pickup e >= now
        if (hReturn > hPickup && hPickup >= now) {
            isDatesValid = true;
        }
    }

    if (isDatesValid) {
        document.getElementById('booking-pickup-date').value = heroPickupDate;
        document.getElementById('booking-return-date').value = heroReturnDate;
    } else {
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        tomorrow.setHours(9, 0, 0, 0);
        document.getElementById('booking-pickup-date').value = tomorrow.toISOString().slice(0, 16);
        
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

/**
 * Abre um sub-modal do painel administrativo de forma correta.
 * Remove o backdrop-filter do overlay principal (que criava um stacking
 * context bloqueando o z-index dos sub-modais) e exibe o modal alvo.
 * @param {HTMLElement} modal - O elemento do sub-modal a ser exibido
 */
function openAdminSubModal(modal) {
    if (!modal) return;
    const adminOverlay = document.getElementById('admin-dashboard-modal');
    if (adminOverlay) adminOverlay.classList.add('sub-modal-open');
    modal.classList.add('show');
}

/**
 * Fecha um sub-modal do painel administrativo e restaura o backdrop-filter
 * do overlay principal.
 * @param {HTMLElement} modal - O elemento do sub-modal a ser fechado
 */
function closeAdminSubModal(modal) {
    if (!modal) return;
    modal.classList.remove('show');
    // Aguarda o fim da transição de saída para restaurar o backdrop
    setTimeout(() => {
        const adminOverlay = document.getElementById('admin-dashboard-modal');
        const anySubModalOpen = document.querySelector(
            '#vehicle-form-modal.show, #maintenance-form-modal.show, #return-form-modal.show, #receipt-modal.show'
        );
        if (adminOverlay && !anySubModalOpen) {
            adminOverlay.classList.remove('sub-modal-open');
        }
    }, 420); // Tempo da transição CSS (0.4s)
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
                } else if (tabId === 'tab-customers') {
                    loadAdminDashboardCustomers();
                } else if (tabId === 'tab-maintenance') {
                    loadAdminMaintenanceTab();
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
    
    if (closeVeh) closeVeh.addEventListener('click', () => closeAdminSubModal(vehModal));
    if (addVehBtn) {
        addVehBtn.addEventListener('click', () => {
            vehForm.reset();
            document.getElementById('admin-vehicle-id').value = '';
            document.getElementById('vehicle-modal-title').textContent = 'Cadastrar Veículo';
            openAdminSubModal(vehModal);
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
                    closeAdminSubModal(vehModal);
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
    
    if (closeMaint) closeMaint.addEventListener('click', () => closeAdminSubModal(maintModal));
    
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
                    closeAdminSubModal(maintModal);
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
    
    if (closeRet) closeRet.addEventListener('click', () => closeAdminSubModal(retModal));
    
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
                    closeAdminSubModal(retModal);
                    
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
    
    const hideReceipt = () => closeAdminSubModal(receiptModal);
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
    
    openAdminSubModal(modal);
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
        
        if (!response.ok) {
            throw new Error(`Erro na API: ${response.status}`);
        }
        
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
    } catch(e) {
        console.warn('Usando fallback local para métricas de portfólio:', e);
        // Fallback dinâmico resiliente com dados fictícios elegantes e realistas
        document.getElementById('kpi-active-rentals').textContent = "3";
        document.getElementById('kpi-total-customers').textContent = "9";
        document.getElementById('kpi-total-vehicles').textContent = "9 / 12";
        document.getElementById('kpi-revenue').textContent = "R$ 2.940,00";
        
        // Distribuição visual fictícia harmônica
        document.getElementById('progress-available').style.width = "75%";
        document.getElementById('progress-rented').style.width = "17%";
        document.getElementById('progress-maintenance').style.width = "8%";
        
        document.getElementById('count-available').textContent = "9";
        document.getElementById('count-rented').textContent = "2";
        document.getElementById('count-maintenance').textContent = "1";
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
                statusBadge.className = 'badge-inline';
                
                if (v.status === 'AVAILABLE') {
                    statusBadge.className += ' badge-primary';
                    statusBadge.style.backgroundColor = 'var(--success)';
                    statusBadge.style.color = 'white';
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
                    openAdminSubModal(document.getElementById('vehicle-form-modal'));
                });
                
                const btnMaint = document.createElement('button');
                btnMaint.className = 'btn-admin-action btn-maint';
                btnMaint.innerHTML = '<i data-lucide="wrench" style="width: 12px; height: 12px; vertical-align: middle;"></i> 🔧';
                btnMaint.title = 'Registrar Manutenção';
                btnMaint.addEventListener('click', () => {
                    document.getElementById('maintenance-vehicle-id').value = v.id;
                    document.getElementById('maint-notes').value = '';
                    openAdminSubModal(document.getElementById('maintenance-form-modal'));
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
        
        if (!response.ok) {
            throw new Error(`Erro na API: ${response.status}`);
        }
        
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
    } catch(e) {
        console.warn('Usando aluguéis fictícios de fallback para portfólio:', e);
        const tbody = document.querySelector('#admin-rentals-table tbody');
        if (tbody) {
            tbody.innerHTML = `
                <tr>
                    <td style="font-weight: 600;">João S.</td>
                    <td>Chevrolet Onix</td>
                    <td>01/06/2026 09:00</td>
                    <td>06/06/2026 18:00</td>
                    <td><span class="badge-inline badge-primary">Ativo</span></td>
                    <td><div class="admin-action-btn-group"><button class="btn-admin-action" onclick="alert('Ambiente Fictício: Ação não disponível no modo de fallback offline.')">Devolver</button></div></td>
                </tr>
                <tr>
                    <td style="font-weight: 600;">Maria S.</td>
                    <td>Toyota Corolla</td>
                    <td>02/06/2026 10:00</td>
                    <td>07/06/2026 10:00</td>
                    <td><span class="badge-inline badge-primary">Ativo</span></td>
                    <td><div class="admin-action-btn-group"><button class="btn-admin-action" onclick="alert('Ambiente Fictício: Ação não disponível no modo de fallback offline.')">Devolver</button></div></td>
                </tr>
                <tr>
                    <td style="font-weight: 600;">Pedro S.</td>
                    <td>Jeep Compass</td>
                    <td>20/05/2026 14:00</td>
                    <td>25/05/2026 14:00</td>
                    <td><span class="badge-inline badge-primary" style="background-color: var(--success); color: white;">Concluído</span></td>
                    <td><span style="color: var(--text-light); font-size: 0.8rem;">Sem Ações</span></td>
                </tr>
            `;
        }
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
    
    openAdminSubModal(modal);
}

function loadAdminDashboardCustomers() {
    const customersTbody = document.querySelector('#admin-customers-table tbody');
    customersTbody.innerHTML = `
        <tr>
            <td style="font-weight: 600;">Thiago G. S.</td>
            <td>***.248.109-**</td>
            <td>***.***.209</td>
            <td><span class="badge-inline badge-primary" style="background-color: var(--primary); font-weight: 700;">DIAMANTE</span></td>
            <td style="font-weight: 700; color: var(--primary);">2.400 pts</td>
        </tr>
        <tr>
            <td style="font-weight: 600;">Maria C. F.</td>
            <td>***.382.901-**</td>
            <td>***.***.612</td>
            <td><span class="badge-inline badge-primary" style="background-color: var(--warning); font-weight: 700;">OURO</span></td>
            <td style="font-weight: 700; color: var(--warning);">1.200 pts</td>
        </tr>
        <tr>
            <td style="font-weight: 600;">Bruno A. R.</td>
            <td>***.892.112-**</td>
            <td>***.***.293</td>
            <td><span class="badge-inline badge-primary" style="background-color: #3b82f6; font-weight: 700;">PRATA</span></td>
            <td style="font-weight: 700; color: #3b82f6;">600 pts</td>
        </tr>
        <tr>
            <td style="font-weight: 600;">Ana J. O.</td>
            <td>***.501.374-**</td>
            <td>***.***.281</td>
            <td><span class="badge-inline badge-primary" style="background-color: var(--text-light); font-weight: 700;">BRONZE</span></td>
            <td style="font-weight: 700; color: var(--text-light);">100 pts</td>
        </tr>
    `;
}

/**
 * Popula a aba de Veículos em Manutenção com dados fictícios premium.
 * Exibe KPIs, cards visuais por tipo de manutenção e tabela de detalhamento.
 */
function loadAdminMaintenanceTab() {
    // Dados fictícios de manutenção (portfólio)
    const maintenanceData = [
        {
            id: 'M-0041',
            brand: 'Volkswagen', model: 'Tiguan',
            plate: 'BRZ-3E21', category: 'SUV',
            type: 'corrective', typeLabel: 'Corretiva',
            shop: 'Auto Center São Luís',
            entryDate: '26/05/2026',
            exitForecast: '05/06/2026',
            daysIn: 7, totalDays: 10,
            cost: 'R$ 2.400,00',
            urgency: 'high', urgencyLabel: 'Urgente',
            description: 'Falha no sistema de freios ABS e troca de pastilhas'
        },
        {
            id: 'M-0042',
            brand: 'Honda', model: 'HR-V',
            plate: 'QKR-7F08', category: 'SUV',
            type: 'preventive', typeLabel: 'Preventiva',
            shop: 'Oficina Mega Motors',
            entryDate: '29/05/2026',
            exitForecast: '04/06/2026',
            daysIn: 4, totalDays: 6,
            cost: 'R$ 980,00',
            urgency: 'low', urgencyLabel: 'Baixa',
            description: 'Revisão geral dos 60.000 km + alinhamento'
        },
        {
            id: 'M-0043',
            brand: 'Toyota', model: 'Corolla',
            plate: 'MNP-4A55', category: 'Sedan',
            type: 'tires', typeLabel: 'Pneus',
            shop: 'Borracharia Confiamaç',
            entryDate: '01/06/2026',
            exitForecast: '03/06/2026',
            daysIn: 1, totalDays: 2,
            cost: 'R$ 1.620,00',
            urgency: 'medium', urgencyLabel: 'Média',
            description: 'Troca de 4 pneus + balanceamento e rodizímio'
        },
        {
            id: 'M-0044',
            brand: 'Jeep', model: 'Compass',
            plate: 'GTX-2C90', category: 'SUV',
            type: 'oil', typeLabel: 'Troca de Óleo',
            shop: 'Auto Center São Luís',
            entryDate: '02/06/2026',
            exitForecast: '02/06/2026',
            daysIn: 0, totalDays: 1,
            cost: 'R$ 340,00',
            urgency: 'low', urgencyLabel: 'Baixa',
            description: 'Troca de óleo 5W-30 sintético + filtros'
        },
        {
            id: 'M-0045',
            brand: 'Chevrolet', model: 'Tracker',
            plate: 'VWQ-9K14', category: 'SUV',
            type: 'corrective', typeLabel: 'Corretiva',
            shop: 'Concessão AutoMax',
            entryDate: '25/05/2026',
            exitForecast: '08/06/2026',
            daysIn: 8, totalDays: 14,
            cost: 'R$ 2.460,00',
            urgency: 'high', urgencyLabel: 'Urgente',
            description: 'Substituição da caixa de câmbio automática'
        }
    ];

    // -- Popula Cards --
    const grid = document.getElementById('maint-cards-grid');
    if (!grid) return;
    grid.innerHTML = '';

    maintenanceData.forEach(m => {
        const progress = m.totalDays > 0 ? Math.round((m.daysIn / m.totalDays) * 100) : 100;
        const card = document.createElement('div');
        card.className = `maint-card type-${m.type}`;
        card.innerHTML = `
            <div class="maint-card-header">
                <div class="maint-car-info">
                    <h4>${m.brand} ${m.model}</h4>
                    <p>${m.plate} • ${m.category}</p>
                </div>
                <span class="maint-type-badge">${m.typeLabel}</span>
            </div>
            <div class="maint-card-body">
                <div class="maint-info-row">
                    <i data-lucide="building-2"></i>
                    <span><strong>Oficina:</strong> ${m.shop}</span>
                </div>
                <div class="maint-info-row">
                    <i data-lucide="clipboard-list"></i>
                    <span>${m.description}</span>
                </div>
                <div class="maint-info-row">
                    <i data-lucide="calendar"></i>
                    <span><strong>Entrada:</strong> ${m.entryDate} &nbsp;→&nbsp; <strong>Saída:</strong> ${m.exitForecast}</span>
                </div>
            </div>
            <div class="maint-progress-wrapper">
                <div class="maint-progress-label">
                    <span>Progresso estimado</span>
                    <span><strong>${progress}%</strong> (${m.daysIn}/${m.totalDays} dias)</span>
                </div>
                <div class="maint-progress-bar-track">
                    <div class="maint-progress-bar-fill" style="width: 0%" data-target="${progress}"></div>
                </div>
            </div>
            <div class="maint-card-footer">
                <span class="maint-cost">${m.cost}</span>
                <span class="maint-urgency urgency-${m.urgency}">${m.urgencyLabel}</span>
            </div>
        `;
        grid.appendChild(card);
    });

    // Anima barras de progresso após render
    lucide.createIcons();
    requestAnimationFrame(() => {
        setTimeout(() => {
            grid.querySelectorAll('.maint-progress-bar-fill[data-target]').forEach(bar => {
                bar.style.width = bar.getAttribute('data-target') + '%';
            });
        }, 80);
    });

    // -- Popula Tabela de Detalhamento --
    const tbody = document.getElementById('maint-table-body');
    if (tbody) {
        tbody.innerHTML = '';
        maintenanceData.forEach(m => {
            const progress = m.totalDays > 0 ? Math.round((m.daysIn / m.totalDays) * 100) : 100;
            const tr = document.createElement('tr');
            const typeColors = {
                corrective: 'color:#DC2626; background:#FEE2E2',
                preventive: 'color:#2563EB; background:#EFF6FF',
                tires:      'color:#7C3AED; background:#F3E8FF',
                oil:        'color:#D97706; background:#FEF3C7'
            };
            const typeStyle = typeColors[m.type] || '';
            tr.innerHTML = `
                <td>
                    <div style="font-weight: 600; color: var(--secondary);">${m.brand} ${m.model}</div>
                    <div style="font-size: 0.75rem; color: var(--text-light);">${m.plate}</div>
                </td>
                <td><span class="badge-inline" style="${typeStyle}; font-size: 0.72rem; font-weight: 700; padding: 0.2rem 0.55rem; border-radius: 999px;">${m.typeLabel}</span></td>
                <td style="font-size: 0.85rem;">${m.shop}</td>
                <td style="font-size: 0.85rem; white-space: nowrap;">${m.entryDate}</td>
                <td style="font-size: 0.85rem; white-space: nowrap; font-weight: 600;">${m.exitForecast}</td>
                <td style="font-weight: 700; color: var(--secondary);">${m.cost}</td>
                <td>
                    <div style="display: flex; align-items: center; gap: 0.5rem;">
                        <div class="table-progress-track">
                            <div class="table-progress-fill" style="width: 0%" data-target="${progress}"></div>
                        </div>
                        <span style="font-size: 0.75rem; font-weight: 700; color: var(--text-light); white-space: nowrap;">${progress}%</span>
                    </div>
                </td>
            `;
            tbody.appendChild(tr);
        });

        // Anima barras da tabela
        requestAnimationFrame(() => {
            setTimeout(() => {
                tbody.querySelectorAll('.table-progress-fill[data-target]').forEach(bar => {
                    bar.style.width = bar.getAttribute('data-target') + '%';
                });
            }, 120);
        });
    }

    // Atualiza timestamp
    const now = new Date();
    const timeStr = now.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
    const el = document.getElementById('maint-last-update');
    if (el) el.textContent = `Atualizado às ${timeStr}`;
}

// Dicionário de Traduções para Avisos de Portfólio Fictício
const portfolioTranslations = {
    pt: {
        badge: '<strong>Ambiente de Portfólio</strong><span>Sistema fictício para fins demonstrativos.</span>',
        banner: '⚠️ AMBIENTE DE PORTFÓLIO: Todos os dados cadastrados, aluguéis e devoluções são 100% FICTÍCIOS. Nomes e CNHs mascarados com segurança.',
        receiptWarning: '⚠️ <strong>ATENÇÃO:</strong> Esta transação foi efetuada em ambiente simulado de testes (in-memory). Nenhum valor financeiro real foi cobrado.'
    },
    en: {
        badge: '<strong>Portfolio Environment</strong><span>Fictitious system for demonstration purposes.</span>',
        banner: '⚠️ PORTFOLIO ENVIRONMENT: All registered data, rentals, and returns are 100% FICTITIOUS. Names and CNHs securely masked.',
        receiptWarning: '⚠️ <strong>WARNING:</strong> This transaction was performed in a simulated test environment (in-memory). No real financial value was charged.'
    },
    es: {
        badge: '<strong>Entorno de Portafolio</strong><span>Sistema ficticio para fines demostrativos.</span>',
        banner: '⚠️ ENTORNO DE PORTAFOLIO: Todos los datos registrados, alquileres y devoluciones son 100% FICTICIOS. Nombres y CNHs enmascarados con seguridad.',
        receiptWarning: '⚠️ <strong>ATENCIÓN:</strong> Esta transacción se realizó en un entorno de prueba simulado (in-memory). No se cobró ningún valor financiero real.'
    },
    zh: {
        badge: '<strong>作品集演示环境</strong><span>用于设计 & 开发演示的虚构系统。</span>',
        banner: '⚠️ 作品集演示环境：所有注册数据、租赁和退还记录均为 100% 虚构。姓名和驾驶执照已安全脱敏。',
        receiptWarning: '⚠️ <strong>注意：</strong> 本次交易在模拟测试环境（内存驻留）中完成。未收取任何实际资金。'
    }
};

function updateLanguageTexts() {
    const lang = currentLanguage || 'pt';
    const t = portfolioTranslations[lang] || portfolioTranslations.pt;
    
    // 1. Atualiza Badge Flutuante
    const badgeText = document.querySelector('.portfolio-badge .badge-text');
    if (badgeText) {
        badgeText.innerHTML = t.badge;
    }
    
    // 2. Atualiza Banner de Portfólio do Painel Admin
    const adminBanner = document.querySelector('.admin-portfolio-banner');
    if (adminBanner) {
        adminBanner.textContent = t.banner;
    }
    
    // 3. Atualiza Alerta de Transação Fictícia no modal de Recibo de Devolução
    const receiptWarning = document.querySelector('#receipt-modal p[style*="font-size: 0.78rem"]');
    if (receiptWarning) {
        receiptWarning.innerHTML = t.receiptWarning;
    }
}

/**
 * ==========================================================================
 * Client Profile & User Management Module
 * ==========================================================================
 */

function initClientProfile() {
    const profileModal = document.getElementById('client-profile-modal');
    const closeBtn = document.getElementById('btn-close-profile-modal');
    
    if (closeBtn && profileModal) {
        closeBtn.addEventListener('click', () => {
            profileModal.classList.remove('show');
        });
        
        profileModal.addEventListener('click', (e) => {
            if (e.target === profileModal) {
                profileModal.classList.remove('show');
            }
        });
    }
    
    // Configuração do formulário de senha
    const passForm = document.getElementById('profile-password-form');
    if (passForm) {
        passForm.addEventListener('submit', handlePasswordChange);
    }
    
    // Configuração do upload de avatar
    const avatarInput = document.getElementById('profile-avatar-input');
    if (avatarInput) {
        avatarInput.addEventListener('change', handleAvatarUpload);
    }
    
    // Setup das abas do perfil
    setupProfileTabs();
}

function setupProfileTabs() {
    const tabButtons = document.querySelectorAll('.profile-tab-btn');
    tabButtons.forEach(btn => {
        btn.addEventListener('click', () => {
            tabButtons.forEach(b => {
                b.classList.remove('active');
                b.style.borderBottomColor = 'transparent';
                b.style.fontWeight = '500';
                b.style.color = 'var(--text-light)';
            });
            
            btn.classList.add('active');
            btn.style.borderBottomColor = 'var(--primary)';
            btn.style.fontWeight = '600';
            btn.style.color = 'var(--secondary)';
            
            const tabId = btn.getAttribute('data-tab');
            document.querySelectorAll('.profile-tab-pane').forEach(pane => {
                pane.style.display = 'none';
            });
            
            const activePane = document.getElementById(tabId);
            if (activePane) {
                activePane.style.display = 'block';
            }
            
            // Ações específicas de carregamento por aba
            if (tabId === 'tab-profile-rentals') {
                loadClientRentals();
            }
        });
    });
}

async function openClientProfile() {
    const token = localStorage.getItem('jwt_token');
    if (!token) return;
    
    const profileModal = document.getElementById('client-profile-modal');
    if (!profileModal) return;
    
    // Reset para primeira aba (Meu Perfil)
    const firstTabBtn = document.querySelector('.profile-tab-btn[data-tab="tab-profile-info"]');
    if (firstTabBtn) firstTabBtn.click();
    
    profileModal.classList.add('show');
    
    // Carregar dados cadastrais
    try {
        const response = await fetch(`${API_BASE_URL}/users/me`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        
        if (!response.ok) throw new Error('Erro ao carregar dados do perfil');
        
        const data = await response.json();
        
        // Povoar campos no HTML
        const nameDiv = document.getElementById('profile-name');
        const emailDiv = document.getElementById('profile-email');
        const phoneDiv = document.getElementById('profile-phone');
        const cpfDiv = document.getElementById('profile-cpf');
        const cnhDiv = document.getElementById('profile-cnh');
        const fidelityPointsDiv = document.getElementById('profile-fidelity-points');
        const fidelityLevelDiv = document.getElementById('profile-fidelity-level');
        const avatarImg = document.getElementById('profile-avatar-img');
        
        if (nameDiv) nameDiv.textContent = data.fullName;
        if (emailDiv) emailDiv.textContent = data.email;
        if (phoneDiv) phoneDiv.textContent = data.phone || 'Não informado';
        if (cpfDiv) cpfDiv.textContent = maskCpf(data.cpf);
        if (cnhDiv) cnhDiv.textContent = maskCnh(data.cnh);
        if (fidelityPointsDiv) fidelityPointsDiv.textContent = `${data.fidelityPoints} pts`;
        if (fidelityLevelDiv) {
            fidelityLevelDiv.textContent = data.fidelityLevel;
            // Cor especial baseada na fidelidade
            if (data.fidelityLevel === 'GOLD') fidelityLevelDiv.style.color = '#D97706';
            else if (data.fidelityLevel === 'DIAMOND') fidelityLevelDiv.style.color = '#7C3AED';
            else if (data.fidelityLevel === 'SILVER') fidelityLevelDiv.style.color = '#4B5563';
            else fidelityLevelDiv.style.color = 'var(--secondary)';
        }
        
        if (avatarImg) {
            if (data.avatar && data.avatar.startsWith('data:image')) {
                avatarImg.src = data.avatar;
            } else {
                avatarImg.src = 'img/compact.png'; // Fallback
            }
        }
        
        lucide.createIcons();
    } catch (e) {
        console.error(e);
        alert('Falha ao obter perfil do usuário.');
    }
}

async function loadClientRentals() {
    const token = localStorage.getItem('jwt_token');
    if (!token) return;
    
    const loader = document.getElementById('profile-rentals-loader');
    const emptyDiv = document.getElementById('profile-rentals-empty');
    const container = document.getElementById('profile-rentals-container');
    const tbody = document.getElementById('profile-rentals-tbody');
    
    if (loader) loader.style.display = 'flex';
    if (emptyDiv) emptyDiv.style.display = 'none';
    if (container) container.style.display = 'none';
    if (tbody) tbody.innerHTML = '';
    
    try {
        const response = await fetch(`${API_BASE_URL}/rentals/me`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        
        if (!response.ok) throw new Error('Falha ao carregar histórico de aluguéis');
        
        const rentals = await response.json();
        
        if (loader) loader.style.display = 'none';
        
        if (!rentals || rentals.length === 0) {
            if (emptyDiv) emptyDiv.style.display = 'block';
            return;
        }
        
        rentals.forEach(rental => {
            const tr = document.createElement('tr');
            
            // Formatando datas
            const pickup = new Date(rental.pickupDate).toLocaleDateString('pt-BR');
            const returnD = new Date(rental.returnDate).toLocaleDateString('pt-BR');
            
            // Mapeando badge de status
            let statusText = 'Pendente';
            let statusClass = 'pending';
            if (rental.status === 'ACTIVE' || rental.status === 'CONFIRMED') {
                statusText = 'Ativo';
                statusClass = 'active';
            } else if (rental.status === 'COMPLETED') {
                statusText = 'Concluído';
                statusClass = 'completed';
            }
            
            tr.innerHTML = `
                <td style="padding: 0.75rem;"><strong>${rental.vehicleBrand} ${rental.vehicleModel}</strong><br><span style="font-size: 0.75rem; color: var(--text-light); font-weight: 550;">${rental.vehicleCategory}</span></td>
                <td style="padding: 0.75rem;">Retirada: ${pickup}<br>Devolução: ${returnD}</td>
                <td style="padding: 0.75rem; font-weight: 700; color: var(--secondary);">R$ ${rental.totalCost.toFixed(2)}</td>
                <td style="padding: 0.75rem;"><span class="status-badge ${statusClass}">${statusText}</span></td>
            `;
            
            tbody.appendChild(tr);
        });
        
        if (container) container.style.display = 'block';
        
    } catch (e) {
        console.error(e);
        if (loader) loader.style.display = 'none';
        if (tbody) tbody.innerHTML = `<tr><td colspan="4" class="error-msg" style="text-align: center; padding: 1.5rem 0;">Erro ao conectar com o servidor.</td></tr>`;
        if (container) container.style.display = 'block';
    }
}

async function handlePasswordChange(e) {
    e.preventDefault();
    const token = localStorage.getItem('jwt_token');
    if (!token) return;
    
    const oldPassword = document.getElementById('profile-password-old').value;
    const newPassword = document.getElementById('profile-password-new').value;
    const confirmPassword = document.getElementById('profile-password-confirm').value;
    
    const errorDiv = document.getElementById('profile-password-error');
    const successDiv = document.getElementById('profile-password-success');
    const submitBtn = e.target.querySelector('button[type="submit"]');
    
    if (errorDiv) errorDiv.style.display = 'none';
    if (successDiv) successDiv.style.display = 'none';
    
    if (newPassword !== confirmPassword) {
        if (errorDiv) {
            errorDiv.textContent = 'As novas senhas não coincidem!';
            errorDiv.style.display = 'block';
        }
        return;
    }
    
    if (newPassword.length < 6) {
        if (errorDiv) {
            errorDiv.textContent = 'A nova senha deve possuir no mínimo 6 caracteres!';
            errorDiv.style.display = 'block';
        }
        return;
    }
    
    submitBtn.disabled = true;
    submitBtn.textContent = 'Processando...';
    
    try {
        const response = await fetch(`${API_BASE_URL}/users/me/password`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ oldPassword, newPassword })
        });
        
        const resText = await response.text();
        
        if (!response.ok) {
            throw new Error(resText || 'Falha ao atualizar senha. Verifique se a senha atual está correta.');
        }
        
        if (successDiv) {
            successDiv.textContent = 'Sua senha foi alterada com sucesso!';
            successDiv.style.display = 'block';
        }
        e.target.reset();
        
    } catch (err) {
        console.error(err);
        if (errorDiv) {
            errorDiv.textContent = err.message || 'Ocorreu um erro ao atualizar a senha.';
            errorDiv.style.display = 'block';
        }
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Alterar Senha';
    }
}

async function handleAvatarUpload(e) {
    const file = e.target.files[0];
    if (!file) return;
    
    const token = localStorage.getItem('jwt_token');
    if (!token) return;
    
    // Validação de tamanho local do browser (max 500KB)
    if (file.size > 512000) {
        alert('Erro: A foto excede o tamanho máximo de 500KB.');
        return;
    }
    
    const avatarImg = document.getElementById('profile-avatar-img');
    const originalSrc = avatarImg ? avatarImg.src : '';
    
    // Loader temporário no avatar
    if (avatarImg) avatarImg.style.opacity = '0.5';
    
    const reader = new FileReader();
    reader.onloadend = async () => {
        const base64String = reader.result;
        
        try {
            const response = await fetch(`${API_BASE_URL}/users/me/avatar`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({ avatar: base64String })
            });
            
            if (!response.ok) throw new Error('Erro ao atualizar foto de perfil no servidor');
            
            // Sucesso! Atualiza visualmente na hora
            if (avatarImg) {
                avatarImg.src = base64String;
                avatarImg.style.opacity = '1';
            }
            alert('Foto de perfil atualizada com sucesso!');
        } catch (error) {
            console.error(error);
            alert('Erro de conexão ou tamanho ao enviar foto de perfil.');
            if (avatarImg) {
                avatarImg.src = originalSrc;
                avatarImg.style.opacity = '1';
            }
        }
    };
    reader.readAsDataURL(file);
}

// Helpers extras para CPF
function maskCpf(cpf) {
    if (!cpf) return '';
    const clean = cpf.replace(/\D/g, '');
    if (clean.length !== 11) return cpf;
    return `***.***.${clean.substring(6, 9)}-**`;
}
