# 🚗 RodaLivre — Prompt Mestre de Desenvolvimento
> Documento de Especificação Técnica Completa + UML para IA Desenvolvedora
> Versão: 1.0 | Nível: Produção / Portfólio Profissional

---

## 📌 CONTEXTO GERAL DO PROJETO

Você irá desenvolver o **RodaLivre**, uma plataforma profissional de aluguel de automóveis para portfólio. O sistema deve ser visualmente comparável a empresas líderes do setor como **Localiza, Unidas, Rentcars, Hertz e Movida**. Toda a stack está definida abaixo e deve ser seguida **sem desvios**.

**Este projeto é fictício para portfólio:** dados de usuários, reservas, frotas e relatórios devem ser simulados/mockados com dados realistas. As **fotos dos veículos devem ser reais** (via URLs públicas de imagens de qualidade — Unsplash, marcas oficiais, etc.).

---

## 🏗️ STACK TECNOLÓGICA

### Backend
- **Linguagem:** Java 21 (LTS)
- **Framework:** Spring Boot 3.x
- **Segurança:** Spring Security + JWT (Bearer Token)
- **ORM:** Hibernate / Spring Data JPA
- **Banco de dados:** PostgreSQL 15+
- **Migrações:** Flyway
- **Cache:** Redis (sessões, rate limiting)
- **Documentação de API:** Swagger/OpenAPI 3.0
- **Build:** Maven
- **Containerização:** Docker + Docker Compose
- **Testes:** JUnit 5 + Mockito + Testcontainers

### Frontend
- **Stack:** HTML5, CSS3 (vanilla), JavaScript ES6+
- **Sem frameworks JS (React/Vue/Angular)** — pure vanilla JS modular
- **Design System:** CSS Custom Properties (variáveis), Flexbox, Grid
- **Ícones:** Lucide Icons (via CDN)
- **Fontes:** Google Fonts
- **Charts (dashboard):** Chart.js (via CDN)
- **HTTP Client:** Fetch API nativo

---

## 🔐 SEGURANÇA — OWASP TOP 10 + DIRETRIZES ADICIONAIS

> **REGRA ABSOLUTA: O backend JAMAIS confia no frontend.**

### OWASP Top 10 — Implementação Obrigatória

| # | Vulnerabilidade | Implementação Obrigatória |
|---|---|---|
| A01 | Broken Access Control | RBAC estrito no backend. Toda requisição valida papel/permissão server-side. Frontend recebe apenas o que tem permissão de ver. |
| A02 | Cryptographic Failures | Senhas: BCrypt (strength 12). HTTPS obrigatório. Dados sensíveis (CPF, cartão) criptografados em repouso (AES-256). |
| A03 | Injection | Prepared Statements em todas as queries. Hibernate parametrizado. Bean Validation em todos os DTOs. |
| A04 | Insecure Design | Arquitetura de Defense in Depth. Princípio do menor privilégio. Separação total de responsabilidades. |
| A05 | Security Misconfiguration | Headers HTTP de segurança obrigatórios (ver abaixo). CORS restrito. Actuator endpoints protegidos. |
| A06 | Vulnerable Components | Dependências auditadas com OWASP Dependency-Check Maven Plugin. |
| A07 | Auth Failures | JWT com expiração curta (15min access token, 7d refresh token). Blacklist de tokens revogados no Redis. Brute-force protection. |
| A08 | Software Integrity | Validação de integridade de uploads. Content-Type validation server-side. |
| A09 | Logging & Monitoring | Audit log completo de todas as operações sensíveis. Structured logging (Logback JSON). |
| A10 | SSRF | Whitelist de URLs externas permitidas. Validação de inputs de URL. |

### Headers HTTP de Segurança Obrigatórios
```
Strict-Transport-Security: max-age=31536000; includeSubDomains
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
Content-Security-Policy: default-src 'self'; script-src 'self' cdn.jsdelivr.net fonts.googleapis.com; style-src 'self' 'unsafe-inline' fonts.googleapis.com; img-src 'self' data: https:; font-src 'self' fonts.gstatic.com
Referrer-Policy: strict-origin-when-cross-origin
Permissions-Policy: geolocation=(), microphone=(), camera=()
```

### Proteções Adicionais
- **Rate Limiting:** 100 req/min por IP (Redis). Endpoints de auth: 5 tentativas/15min.
- **Input Sanitization:** Sanitização server-side de todos os campos de texto livre (Jsoup para HTML).
- **CSRF:** Tokens CSRF para endpoints que modificam estado (Spring Security CSRF filter).
- **SQL Injection:** Zero queries nativas — 100% JPA/JPQL parametrizado.
- **Mass Assignment:** DTOs separados de Entities. `@JsonIgnoreProperties` explícito.
- **Sensitive Data Exposure:** CPF mascarado nos logs. Cartão nunca armazenado (apenas últimos 4 dígitos).
- **Session Fixation:** Regeneração de sessão após login.
- **Clickjacking:** `X-Frame-Options: DENY`.
- **Audit Trail:** Tabela `audit_log` com `user_id`, `action`, `entity`, `old_value`, `new_value`, `ip`, `timestamp`.

---

## 📐 UML — DIAGRAMAS COMPLETOS

### 1. DIAGRAMA DE CASOS DE USO

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           SISTEMA RODALIVRELIVRE                            │
│                                                                             │
│  ┌──────────┐                                                               │
│  │  VISITANTE│──────────────► (Visualizar Página Principal)                 │
│  └──────────┘                                                               │
│        │         ──────────► (Buscar Veículos por Localização/Data)         │
│        │         ──────────► (Filtrar Veículos por Categoria)               │
│        │         ──────────► (Ver Detalhes do Veículo)                      │
│        │         ──────────► (Cadastrar-se)                                 │
│        │         ──────────► (Fazer Login)                                  │
│                                                                             │
│  ┌──────────┐                                                               │
│  │  CLIENTE │──────────────► (Fazer Reserva)                                │
│  └──────────┘    ──────────► (Selecionar Data Retirada/Devolução)           │
│   (extends       ──────────► (Escolher Adicionais: Seguro, GPS, etc.)       │
│    Visitante)    ──────────► (Realizar Pagamento)                           │
│                  ──────────► (Cancelar Reserva)                             │
│                  ──────────► (Ver Histórico de Aluguéis)                    │
│                  ──────────► (Avaliar Veículo)                              │
│                  ──────────► (Editar Perfil)                                │
│                  ──────────► (Visualizar Fatura)                            │
│                                                                             │
│  ┌──────────┐                                                               │
│  │ OPERADOR │──────────────► (Acessar Dashboard Operacional)                │
│  └──────────┘    ──────────► (Gerenciar Veículos)                           │
│   (extends       ──────────► (Registrar Manutenção de Veículo)              │
│    Cliente)      ──────────► (Gerenciar Reservas)                           │
│                  ──────────► (Visualizar Clientes)                          │
│                  ──────────► (Gerar Relatórios Mensais)                     │
│                  ──────────► (Registrar Entrega/Devolução)                  │
│                  ──────────► (Aplicar Multas/Juros)                         │
│                                                                             │
│  ┌──────────┐                                                               │
│  │  ADMIN   │──────────────► (Tudo do Operador +)                           │
│  └──────────┘    ──────────► (Gerenciar Operadores/Usuários)                │
│                  ──────────► (Configurar Preços e Categorias)               │
│                  ──────────► (Ver Relatório Financeiro Completo)            │
│                  ──────────► (Gerenciar Permissões)                         │
│                  ──────────► (Ver Audit Log do Sistema)                     │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

### 2. DIAGRAMA DE CLASSES (Backend — Domain Model)

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              DOMAIN MODEL                                       │
│                                                                                 │
│  ┌─────────────────────────────┐         ┌────────────────────────────────┐    │
│  │           User              │         │           Vehicle              │    │
│  ├─────────────────────────────┤         ├────────────────────────────────┤    │
│  │ - id: UUID                  │         │ - id: UUID                     │    │
│  │ - fullName: String          │         │ - brand: String                │    │
│  │ - email: String (unique)    │         │ - model: String                │    │
│  │ - passwordHash: String      │         │ - year: Integer                │    │
│  │ - cpf: String (encrypted)   │         │ - licensePlate: String(unique) │    │
│  │ - phone: String             │         │ - category: VehicleCategory    │    │
│  │ - birthDate: LocalDate      │         │ - status: VehicleStatus        │    │
│  │ - cnh: String               │         │ - dailyRate: BigDecimal        │    │
│  │ - role: UserRole            │         │ - mileage: Integer             │    │
│  │ - fidelityPoints: Integer   │         │ - color: String                │    │
│  │ - active: Boolean           │         │ - seats: Integer               │    │
│  │ - createdAt: LocalDateTime  │         │ - transmission: Transmission   │    │
│  │ - updatedAt: LocalDateTime  │         │ - fuelType: FuelType           │    │
│  └─────────────────────────────┘         │ - imageUrls: List<String>      │    │
│            │  1                          │ - description: String          │    │
│            │                            │ - features: List<String>       │    │
│            │ N                          │ - locationBranch: Branch       │    │
│            ▼                            │ - createdAt: LocalDateTime     │    │
│  ┌─────────────────────────────┐         └────────────────────────────────┘    │
│  │          Rental             │                      │ 1                      │
│  ├─────────────────────────────┤                      │                        │
│  │ - id: UUID                  │◄─────────────────────┘                        │
│  │ - user: User                │                      N                        │
│  │ - vehicle: Vehicle          │                                               │
│  │ - pickupDate: LocalDateTime │         ┌────────────────────────────────┐    │
│  │ - returnDate: LocalDateTime │         │       MaintenanceRecord        │    │
│  │ - actualReturnDate:         │         ├────────────────────────────────┤    │
│  │     LocalDateTime           │         │ - id: UUID                     │    │
│  │ - pickupLocation: Branch    │         │ - vehicle: Vehicle             │    │
│  │ - returnLocation: Branch    │         │ - startDate: LocalDate         │    │
│  │ - status: RentalStatus      │         │ - estimatedEndDate: LocalDate  │    │
│  │ - baseCost: BigDecimal      │         │ - actualEndDate: LocalDate     │    │
│  │ - additionalsCost:          │         │ - description: String          │    │
│  │     BigDecimal              │         │ - cost: BigDecimal             │    │
│  │ - lateFee: BigDecimal       │         │ - type: MaintenanceType        │    │
│  │ - totalCost: BigDecimal     │         │ - workshop: String             │    │
│  │ - paymentMethod: PayMethod  │         │ - registeredBy: User           │    │
│  │ - additionals:              │         │ - notes: String                │    │
│  │     List<RentalAdditional>  │         └────────────────────────────────┘    │
│  │ - observations: String      │                                               │
│  │ - createdAt: LocalDateTime  │         ┌────────────────────────────────┐    │
│  └─────────────────────────────┘         │           Branch               │    │
│            │ 1                           ├────────────────────────────────┤    │
│            │                            │ - id: UUID                     │    │
│            │ N                          │ - name: String                  │    │
│            ▼                            │ - address: Address             │    │
│  ┌──────────────────────────┐            │ - phone: String                │    │
│  │       RentalAdditional   │            │ - active: Boolean              │    │
│  ├──────────────────────────┤            └────────────────────────────────┘    │
│  │ - id: UUID               │                                                  │
│  │ - rental: Rental         │            ┌────────────────────────────────┐    │
│  │ - type: AdditionalType   │            │         Review                 │    │
│  │ - cost: BigDecimal       │            ├────────────────────────────────┤    │
│  └──────────────────────────┘            │ - id: UUID                     │    │
│                                          │ - rental: Rental               │    │
│  ┌──────────────────────────┐            │ - rating: Integer (1-5)        │    │
│  │         Payment          │            │ - comment: String              │    │
│  ├──────────────────────────┤            │ - createdAt: LocalDateTime     │    │
│  │ - id: UUID               │            └────────────────────────────────┘    │
│  │ - rental: Rental         │                                                  │
│  │ - amount: BigDecimal     │            ┌────────────────────────────────┐    │
│  │ - method: PayMethod      │            │          AuditLog              │    │
│  │ - status: PaymentStatus  │            ├────────────────────────────────┤    │
│  │ - transactionId: String  │            │ - id: UUID                     │    │
│  │ - paidAt: LocalDateTime  │            │ - userId: UUID                 │    │
│  └──────────────────────────┘            │ - action: String               │    │
│                                          │ - entity: String               │    │
│                                          │ - entityId: UUID               │    │
│                                          │ - oldValue: String (JSON)      │    │
│                                          │ - newValue: String (JSON)      │    │
│                                          │ - ipAddress: String            │    │
│                                          │ - timestamp: LocalDateTime     │    │
│                                          └────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────────────────────────┘

ENUMS:
- UserRole: CLIENT, OPERATOR, ADMIN
- VehicleCategory: ECONOMY, INTERMEDIATE, FULL_SIZE, SUV, LUXURY, SPORTS, VAN, TRUCK
- VehicleStatus: AVAILABLE, RENTED, MAINTENANCE, RETIRED
- RentalStatus: PENDING, CONFIRMED, ACTIVE, COMPLETED, CANCELLED, OVERDUE
- Transmission: MANUAL, AUTOMATIC, CVT
- FuelType: GASOLINE, ETHANOL, FLEX, DIESEL, ELECTRIC, HYBRID
- AdditionalType: FULL_INSURANCE, GPS, CHILD_SEAT, ADDITIONAL_DRIVER, ROADSIDE_ASSISTANCE
- PaymentMethod: CREDIT_CARD, DEBIT_CARD, PIX, BANK_TRANSFER
- PaymentStatus: PENDING, APPROVED, FAILED, REFUNDED
- MaintenanceType: PREVENTIVE, CORRECTIVE, ACCIDENT, REVISION
```

---

### 3. DIAGRAMA DE SEQUÊNCIA — Fluxo de Reserva

```
Cliente         Frontend        API Gateway      RentalController    VehicleService    RentalService    PaymentService    Database
   │                │                │                  │                  │                │                 │               │
   │──busca veíc───►│                │                  │                  │                │                 │               │
   │                │──GET /vehicles─►│                  │                  │                │                 │               │
   │                │                │──── validar ─────►│                  │                │                 │               │
   │                │                │                  │──checkAvailabil──►│                │                 │               │
   │                │                │                  │                  │──────── query ─────────────────────────────────►│
   │                │                │                  │                  │◄──────── result────────────────────────────────│
   │                │                │                  │◄─── VehicleDTO ──│                │                 │               │
   │                │◄──── 200 OK ───│                  │                  │                │                 │               │
   │◄── lista ─────│                │                  │                  │                │                 │               │
   │                │                │                  │                  │                │                 │               │
   │──seleciona────►│                │                  │                  │                │                 │               │
   │──confirma─────►│                │                  │                  │                │                 │               │
   │                │──POST /rentals─►│                  │                  │                │                 │               │
   │                │ (+ JWT token)  │── auth check ───►│                  │                │                 │               │
   │                │                │                  │── validateUser ──────────────────►│                 │               │
   │                │                │                  │── checkVehicle ──►│                │                 │               │
   │                │                │                  │                  │── lock vehicle ──────────────────────────────►│
   │                │                │                  │── createRental ──────────────────►│                 │               │
   │                │                │                  │                  │                │──── save ─────────────────────►│
   │                │                │                  │                  │                │◄─── rentalId ──────────────────│
   │                │                │                  │── processPayment──────────────────────────────────►│               │
   │                │                │                  │                  │                │                 │── authorize ─►│
   │                │                │                  │                  │                │                 │◄── approved ──│
   │                │                │                  │                  │                │◄── paymentId ───│               │
   │                │                │                  │                  │                │── updateStatus ─────────────►│
   │                │◄──── 201 ──────│                  │                  │                │                 │               │
   │◄── confirmação│                │                  │                  │                │                 │               │
```

---

### 4. DIAGRAMA DE SEQUÊNCIA — Login + JWT

```
Cliente         Frontend         AuthController      UserService       Redis         Database
   │                │                  │                  │               │               │
   │──POST /login──►│                  │                  │               │               │
   │                │──POST /auth/login►│                  │               │               │
   │                │ {email, password}│                  │               │               │
   │                │                  │── findByEmail ──►│               │               │
   │                │                  │                  │────── query ─────────────────►│
   │                │                  │                  │◄───── user ───────────────────│
   │                │                  │── validatePwd ──►│               │               │
   │                │                  │── checkBrute ─────────────────────────────────►│(redis)
   │                │                  │◄── ok ────────────────────────────────────────│
   │                │                  │── genAccessToken│               │               │
   │                │                  │── genRefresh ───│               │               │
   │                │                  │── storeRefresh ──────────────►│(Redis 7d)       │
   │                │                  │── logAudit ─────►│               │               │
   │                │◄── 200 ─────────│                  │               │               │
   │                │ {accessToken,    │                  │               │               │
   │                │  refreshToken,   │                  │               │               │
   │                │  expiresIn}      │                  │               │               │
   │◄── dashboard ─│                  │                  │               │               │
```

---

### 5. DIAGRAMA DE ENTIDADE-RELACIONAMENTO (ER)

```sql
-- TABELAS PRINCIPAIS

users
├── id UUID PK
├── full_name VARCHAR(150) NOT NULL
├── email VARCHAR(255) UNIQUE NOT NULL
├── password_hash VARCHAR(255) NOT NULL
├── cpf VARCHAR(255) NOT NULL (encrypted AES-256)
├── phone VARCHAR(20)
├── birth_date DATE NOT NULL
├── cnh VARCHAR(20) NOT NULL
├── role ENUM('CLIENT','OPERATOR','ADMIN') DEFAULT 'CLIENT'
├── fidelity_points INT DEFAULT 0
├── active BOOLEAN DEFAULT TRUE
├── created_at TIMESTAMP DEFAULT NOW()
└── updated_at TIMESTAMP

branches
├── id UUID PK
├── name VARCHAR(100) NOT NULL
├── street VARCHAR(255)
├── city VARCHAR(100)
├── state VARCHAR(2)
├── zip_code VARCHAR(10)
├── latitude DECIMAL(10,8)
├── longitude DECIMAL(11,8)
├── phone VARCHAR(20)
└── active BOOLEAN DEFAULT TRUE

vehicles
├── id UUID PK
├── brand VARCHAR(50) NOT NULL
├── model VARCHAR(100) NOT NULL
├── year INT NOT NULL
├── license_plate VARCHAR(10) UNIQUE NOT NULL
├── category ENUM(...)
├── status ENUM('AVAILABLE','RENTED','MAINTENANCE','RETIRED')
├── daily_rate DECIMAL(10,2) NOT NULL
├── mileage INT DEFAULT 0
├── color VARCHAR(50)
├── seats INT
├── transmission ENUM(...)
├── fuel_type ENUM(...)
├── description TEXT
├── features JSON
├── image_urls JSON
├── branch_id UUID FK → branches
├── created_at TIMESTAMP DEFAULT NOW()
└── updated_at TIMESTAMP

rentals
├── id UUID PK
├── user_id UUID FK → users NOT NULL
├── vehicle_id UUID FK → vehicles NOT NULL
├── pickup_date TIMESTAMP NOT NULL
├── return_date TIMESTAMP NOT NULL
├── actual_return_date TIMESTAMP
├── pickup_branch_id UUID FK → branches
├── return_branch_id UUID FK → branches
├── status ENUM(...)
├── base_cost DECIMAL(10,2)
├── additionals_cost DECIMAL(10,2) DEFAULT 0
├── late_fee DECIMAL(10,2) DEFAULT 0
├── discount DECIMAL(10,2) DEFAULT 0
├── total_cost DECIMAL(10,2)
├── observations TEXT
├── created_at TIMESTAMP DEFAULT NOW()
└── updated_at TIMESTAMP

rental_additionals
├── id UUID PK
├── rental_id UUID FK → rentals NOT NULL
├── type ENUM(...)
└── cost DECIMAL(10,2)

payments
├── id UUID PK
├── rental_id UUID FK → rentals NOT NULL
├── amount DECIMAL(10,2) NOT NULL
├── method ENUM(...)
├── status ENUM(...)
├── transaction_id VARCHAR(255)
├── card_last_digits VARCHAR(4)
└── paid_at TIMESTAMP

maintenance_records
├── id UUID PK
├── vehicle_id UUID FK → vehicles NOT NULL
├── start_date DATE NOT NULL
├── estimated_end_date DATE
├── actual_end_date DATE
├── type ENUM(...)
├── description TEXT NOT NULL
├── cost DECIMAL(10,2) DEFAULT 0
├── workshop VARCHAR(150)
├── registered_by UUID FK → users
└── notes TEXT

reviews
├── id UUID PK
├── rental_id UUID FK → rentals UNIQUE NOT NULL
├── rating INT CHECK(rating BETWEEN 1 AND 5)
├── comment TEXT
└── created_at TIMESTAMP DEFAULT NOW()

audit_log
├── id UUID PK
├── user_id UUID (nullable, system events)
├── action VARCHAR(100) NOT NULL
├── entity VARCHAR(100) NOT NULL
├── entity_id UUID
├── old_value TEXT (JSON)
├── new_value TEXT (JSON)
├── ip_address VARCHAR(45)
└── timestamp TIMESTAMP DEFAULT NOW()
```

---

### 6. DIAGRAMA DE FLUXO — Páginas do Site

```
┌──────────────────────────────────────────────────────────────────┐
│                        FLUXO PÚBLICO                             │
│                                                                  │
│  [HOME PAGE]                                                     │
│  ├── Hero: busca por localização + datas                         │
│  ├── Categorias em destaque (ícones clicáveis)                   │
│  ├── Veículos em destaque (carrossel)                            │
│  ├── Como funciona (3 passos)                                    │
│  ├── Depoimentos de clientes (fictícios)                         │
│  └── Rodapé com filiais                                          │
│           │                                                      │
│           ▼                                                      │
│  [PÁGINA DE BUSCA/CATÁLOGO]                                      │
│  ├── Filtros: categoria, transmissão, combustível, preço         │
│  ├── Grid de veículos disponíveis                                │
│  ├── Ordenação: menor preço, mais popular, avaliação             │
│  └── Paginação                                                   │
│           │                                                      │
│           ▼                                                      │
│  [DETALHES DO VEÍCULO]                                           │
│  ├── Galeria de fotos (reais)                                    │
│  ├── Especificações técnicas                                     │
│  ├── Lista de recursos incluídos                                 │
│  ├── Avaliações de clientes                                      │
│  ├── Resumo de preço por período                                 │
│  └── Botão: "Reservar Agora"                                     │
│           │                                                      │
│           ▼ (requer login)                                       │
│  [CHECKOUT / RESERVA]                                            │
│  ├── Step 1: Confirmar datas e locais                            │
│  ├── Step 2: Adicionais (seguro, GPS, etc.)                      │
│  ├── Step 3: Dados do condutor                                   │
│  ├── Step 4: Pagamento                                           │
│  └── Step 5: Confirmação com código de reserva                  │
│                                                                  │
│  [LOGIN / CADASTRO]                                              │
│  ├── Formulário de login (email + senha)                         │
│  ├── Link "Esqueci minha senha"                                  │
│  └── Formulário de cadastro (dados completos + CNH)              │
│                                                                  │
│  [ÁREA DO CLIENTE]                                               │
│  ├── Minhas Reservas (ativas, históricas)                        │
│  ├── Meu Perfil (editar dados)                                   │
│  ├── Pontos de Fidelidade                                        │
│  └── Minhas Avaliações                                           │
└──────────────────────────────────────────────────────────────────┘
```

---

### 7. DIAGRAMA DO DASHBOARD OPERACIONAL

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                     DASHBOARD OPERACIONAL (/admin)                           │
│             [Acesso: OPERATOR e ADMIN apenas — validado no backend]          │
│                                                                              │
│  SIDEBAR:                                                                    │
│  ├── 📊 Dashboard (home)                                                     │
│  ├── 🚙 Veículos                                                             │
│  ├── 👥 Clientes                                                             │
│  ├── 📋 Aluguéis                                                             │
│  ├── 📈 Relatórios                                                           │
│  ├── 💰 Financeiro                                                           │
│  └── ⚙️ Configurações (somente ADMIN)                                        │
│                                                                              │
│  ══════════════════════════════════════════════════════════════════════════  │
│                                                                              │
│  📊 PÁGINA: Dashboard (Visão Geral)                                          │
│  ├── KPI Cards:                                                              │
│  │   ├── Total de aluguéis ativos                                           │
│  │   ├── Receita do mês                                                     │
│  │   ├── Veículos disponíveis / total                                       │
│  │   └── Novos clientes no mês                                              │
│  ├── Gráfico: Top 10 Veículos Mais Alugados (Chart.js bar)                  │
│  ├── Gráfico: Receita dos últimos 6 meses (Chart.js line)                   │
│  ├── Tabela: Clientes por Nível de Fidelidade (Bronze/Prata/Ouro/Diamante)  │
│  └── Widget: Frota por Status (disponível/alugado/manutenção) — doughnut    │
│                                                                              │
│  ══════════════════════════════════════════════════════════════════════════  │
│                                                                              │
│  🚙 PÁGINA: Veículos                                                         │
│  ├── Tabela com todos os veículos (paginada)                                │
│  │   Colunas: Foto | Veículo | Placa | Categoria | Status | Diária | Ações  │
│  ├── Filtros: status, categoria, localização, disponibilidade                │
│  ├── Botão: Adicionar Novo Veículo                                           │
│  ├── Ações por veículo:                                                     │
│  │   ├── ✏️ Editar (dados, fotos, valor da diária)                           │
│  │   ├── 🔧 Registrar Manutenção                                             │
│  │   │   ├── Tipo de manutenção                                             │
│  │   │   ├── Data início / previsão término / término real                 │
│  │   │   ├── Oficina responsável                                            │
│  │   │   ├── Custo da manutenção                                            │
│  │   │   └── Notas/observações                                              │
│  │   ├── 📋 Histórico de Manutenções (lista completa + custos acumulados)   │
│  │   ├── 📊 Histórico de Aluguéis desse veículo                             │
│  │   └── 🚫 Desativar veículo                                                │
│                                                                              │
│  ══════════════════════════════════════════════════════════════════════════  │
│                                                                              │
│  👥 PÁGINA: Clientes                                                         │
│  ├── Tabela de todos os clientes                                            │
│  │   Colunas: Nome | Email | CPF(mascarado) | Telefone | Nível | Total Aluguéis │
│  ├── Filtros: nível de fidelidade, período de cadastro, status               │
│  ├── Clique no cliente → Drawer/Modal com:                                  │
│  │   ├── Dados completos do cliente                                         │
│  │   ├── Histórico de todos os aluguéis                                     │
│  │   ├── Total gasto na plataforma                                          │
│  │   ├── Pontos de fidelidade e nível                                       │
│  │   ├── Avaliações feitas                                                  │
│  │   └── Ocorrências (multas, atrasos)                                      │
│  └── Exportar lista (CSV)                                                   │
│                                                                              │
│  ══════════════════════════════════════════════════════════════════════════  │
│                                                                              │
│  📋 PÁGINA: Aluguéis                                                         │
│  ├── Tabs: Todos | Ativos | Pendentes | Em Atraso | Concluídos | Cancelados  │
│  ├── Tabela:                                                                │
│  │   Colunas: ID | Cliente | Veículo | Retirada | Devolução | Status | Total │
│  ├── Filtros: período, status, veículo, cliente                             │
│  ├── Ações por aluguel:                                                     │
│  │   ├── 👁️ Ver detalhes completos                                           │
│  │   ├── ✅ Confirmar retirada (check-out)                                   │
│  │   ├── 🔄 Registrar devolução (check-in)                                  │
│  │   ├── ⚠️ Aplicar juros por atraso (1% ao dia, configurável)              │
│  │   ├── 💳 Ver/registrar pagamento                                          │
│  │   └── ❌ Cancelar reserva                                                 │
│  └── Badge vermelho para aluguéis em atraso                                 │
│                                                                              │
│  ══════════════════════════════════════════════════════════════════════════  │
│                                                                              │
│  📈 PÁGINA: Relatórios Mensais                                               │
│  ├── Seletor de mês/ano                                                     │
│  ├── Seções do relatório:                                                   │
│  │   ├── Resumo executivo (total aluguéis, receita, cancelamentos)          │
│  │   ├── Top veículos mais alugados no período                              │
│  │   ├── Gráfico de ocupação da frota por semana                            │
│  │   ├── Novos clientes captados                                            │
│  │   ├── Taxa de atraso nas devoluções                                      │
│  │   ├── Receita gerada por juros de atraso                                 │
│  │   └── Avaliações recebidas (média geral)                                 │
│  └── Botão: Exportar PDF                                                    │
│                                                                              │
│  ══════════════════════════════════════════════════════════════════════════  │
│                                                                              │
│  💰 PÁGINA: Relatório Financeiro                                             │
│  ├── Cards resumo:                                                          │
│  │   ├── Receita bruta (período selecionado)                                │
│  │   ├── Total gasto com manutenções                                        │
│  │   ├── Lucro líquido estimado                                             │
│  │   └── Receita de multas/juros                                            │
│  ├── Gráfico: Receitas vs Despesas (mensal, últimos 12 meses)               │
│  ├── Tabela de transações:                                                  │
│  │   ├── Receitas: pagamentos de aluguéis (por ID, cliente, valor, data)   │
│  │   └── Despesas: manutenções (por veículo, tipo, custo, data)             │
│  ├── Breakdown por categoria de veículo                                     │
│  └── Exportar relatório (CSV/PDF)                                           │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## 🗂️ ESTRUTURA DO PROJETO

### Backend — Estrutura de Pacotes (Spring Boot)

```
rodalivrelivre-backend/
├── src/main/java/com/rodalivrelivre/
│   ├── config/
│   │   ├── SecurityConfig.java          # Spring Security + JWT filter chain
│   │   ├── JwtConfig.java               # JWT properties
│   │   ├── CorsConfig.java              # CORS permitido apenas para frontend URL
│   │   ├── RedisConfig.java             # Cache e sessões
│   │   ├── RateLimitConfig.java         # Bucket4j / Redis rate limiter
│   │   └── OpenApiConfig.java           # Swagger configuração
│   │
│   ├── domain/
│   │   ├── model/                       # Entidades JPA
│   │   │   ├── User.java
│   │   │   ├── Vehicle.java
│   │   │   ├── Rental.java
│   │   │   ├── RentalAdditional.java
│   │   │   ├── Payment.java
│   │   │   ├── MaintenanceRecord.java
│   │   │   ├── Branch.java
│   │   │   ├── Review.java
│   │   │   └── AuditLog.java
│   │   │
│   │   ├── enums/                       # Todos os enums do sistema
│   │   │   ├── UserRole.java
│   │   │   ├── VehicleCategory.java
│   │   │   ├── VehicleStatus.java
│   │   │   ├── RentalStatus.java
│   │   │   └── ...
│   │   │
│   │   └── repository/                  # Spring Data JPA Repositories
│   │       ├── UserRepository.java
│   │       ├── VehicleRepository.java
│   │       ├── RentalRepository.java
│   │       └── ...
│   │
│   ├── application/
│   │   ├── service/                     # Lógica de negócio
│   │   │   ├── AuthService.java
│   │   │   ├── UserService.java
│   │   │   ├── VehicleService.java
│   │   │   ├── RentalService.java
│   │   │   ├── PaymentService.java
│   │   │   ├── MaintenanceService.java
│   │   │   ├── ReportService.java
│   │   │   └── AuditService.java
│   │   │
│   │   └── dto/                         # DTOs separados por operação
│   │       ├── request/
│   │       │   ├── LoginRequest.java
│   │       │   ├── RegisterRequest.java
│   │       │   ├── CreateRentalRequest.java
│   │       │   ├── CreateVehicleRequest.java
│   │       │   └── ...
│   │       └── response/
│   │           ├── AuthResponse.java
│   │           ├── VehicleResponse.java
│   │           ├── RentalResponse.java
│   │           ├── DashboardMetricsResponse.java
│   │           └── ...
│   │
│   ├── api/
│   │   └── controller/                  # REST Controllers
│   │       ├── AuthController.java      # /api/v1/auth
│   │       ├── VehicleController.java   # /api/v1/vehicles
│   │       ├── RentalController.java    # /api/v1/rentals
│   │       ├── UserController.java      # /api/v1/users
│   │       ├── MaintenanceController.java
│   │       ├── ReportController.java    # /api/v1/reports
│   │       ├── DashboardController.java # /api/v1/dashboard
│   │       └── BranchController.java
│   │
│   ├── security/
│   │   ├── JwtTokenProvider.java        # Geração e validação de JWT
│   │   ├── JwtAuthenticationFilter.java # Filtro que intercepta requests
│   │   ├── UserDetailsServiceImpl.java  # Carrega usuário do banco
│   │   ├── CustomAccessDeniedHandler.java
│   │   └── BruteForceProtectionService.java
│   │
│   └── infrastructure/
│       ├── exception/
│       │   ├── GlobalExceptionHandler.java  # @ControllerAdvice
│       │   ├── BusinessException.java
│       │   ├── ResourceNotFoundException.java
│       │   └── UnauthorizedException.java
│       │
│       └── util/
│           ├── EncryptionUtil.java          # AES-256 para CPF
│           ├── SanitizationUtil.java        # Jsoup sanitizer
│           └── DateTimeUtil.java
│
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── db/migration/                    # Flyway migrations
│       ├── V1__create_users.sql
│       ├── V2__create_vehicles.sql
│       ├── V3__create_rentals.sql
│       ├── V4__create_maintenance.sql
│       ├── V5__create_payments.sql
│       ├── V6__create_reviews.sql
│       ├── V7__create_audit_log.sql
│       └── V8__seed_mock_data.sql       # Dados fictícios para portfólio
│
└── docker-compose.yml
```

### Frontend — Estrutura de Arquivos

```
rodalivrelivre-frontend/
├── index.html                           # Página principal (Home)
├── pages/
│   ├── vehicles.html                    # Catálogo de veículos
│   ├── vehicle-detail.html             # Detalhes do veículo
│   ├── checkout.html                   # Fluxo de reserva (steps)
│   ├── login.html                      # Login
│   ├── register.html                   # Cadastro
│   ├── account.html                    # Área do cliente
│   └── admin/
│       ├── index.html                  # Dashboard
│       ├── vehicles.html              # Gestão de veículos
│       ├── clients.html               # Gestão de clientes
│       ├── rentals.html               # Gestão de aluguéis
│       ├── reports.html               # Relatórios mensais
│       └── financial.html             # Relatório financeiro
│
├── css/
│   ├── variables.css                   # Design tokens (cores, fontes, espaçamentos)
│   ├── reset.css                       # CSS reset / normalize
│   ├── base.css                        # Estilos globais, tipografia
│   ├── components.css                  # Botões, cards, inputs, modais, badges
│   ├── layout.css                      # Header, footer, sidebar, grid
│   ├── pages/
│   │   ├── home.css
│   │   ├── vehicles.css
│   │   ├── checkout.css
│   │   └── admin.css
│   └── utils.css                       # Utilitários (spacing, display, etc.)
│
├── js/
│   ├── config.js                       # API_BASE_URL, constantes
│   ├── api.js                          # Fetch wrapper com interceptors (auth, errors)
│   ├── auth.js                         # Gerenciamento de JWT (storage seguro)
│   ├── utils.js                        # Helpers gerais
│   ├── components/
│   │   ├── header.js                   # Header dinâmico (logado / não logado)
│   │   ├── modal.js                    # Sistema de modais
│   │   ├── toast.js                    # Notificações toast
│   │   ├── pagination.js               # Componente de paginação
│   │   └── vehicle-card.js             # Card de veículo reutilizável
│   └── pages/
│       ├── home.js
│       ├── vehicles.js
│       ├── vehicle-detail.js
│       ├── checkout.js
│       ├── auth.js
│       ├── account.js
│       └── admin/
│           ├── dashboard.js
│           ├── vehicles.js
│           ├── clients.js
│           ├── rentals.js
│           ├── reports.js
│           └── financial.js
│
├── assets/
│   ├── logo/
│   │   ├── rodalivrelivre-logo.svg
│   │   └── rodalivrelivre-logo-white.svg
│   └── icons/
│
└── favicon.ico
```

---

## 🔌 API REST — ENDPOINTS COMPLETOS

```
AUTH
POST   /api/v1/auth/login              # Login → JWT
POST   /api/v1/auth/register           # Cadastro público
POST   /api/v1/auth/refresh            # Renovar access token
POST   /api/v1/auth/logout             # Invalidar token (Redis blacklist)
POST   /api/v1/auth/forgot-password    # Solicitar reset de senha
POST   /api/v1/auth/reset-password     # Confirmar novo senha

VEHICLES (público: GET; protegido: POST/PUT/DELETE → OPERATOR+)
GET    /api/v1/vehicles                # Listar (filtros: categoria, status, datas, localização)
GET    /api/v1/vehicles/{id}           # Detalhes
POST   /api/v1/vehicles                # Criar [OPERATOR/ADMIN]
PUT    /api/v1/vehicles/{id}           # Editar [OPERATOR/ADMIN]
DELETE /api/v1/vehicles/{id}           # Desativar [ADMIN]
GET    /api/v1/vehicles/{id}/rentals   # Histórico de aluguéis [OPERATOR/ADMIN]
GET    /api/v1/vehicles/{id}/maintenance # Histórico de manutenções [OPERATOR/ADMIN]

RENTALS
GET    /api/v1/rentals                 # Listar todos [OPERATOR/ADMIN]
GET    /api/v1/rentals/{id}            # Detalhes
POST   /api/v1/rentals                 # Criar reserva [CLIENT+]
PUT    /api/v1/rentals/{id}/checkout   # Confirmar retirada [OPERATOR/ADMIN]
PUT    /api/v1/rentals/{id}/checkin    # Registrar devolução [OPERATOR/ADMIN]
PUT    /api/v1/rentals/{id}/cancel     # Cancelar [CLIENT dono ou OPERATOR/ADMIN]
PUT    /api/v1/rentals/{id}/late-fee   # Aplicar juros [OPERATOR/ADMIN]
GET    /api/v1/rentals/my              # Aluguéis do usuário logado [CLIENT]

USERS
GET    /api/v1/users                   # Listar todos [ADMIN]
GET    /api/v1/users/{id}              # Detalhes [ADMIN ou próprio user]
PUT    /api/v1/users/{id}              # Editar perfil [próprio user ou ADMIN]
PUT    /api/v1/users/{id}/role         # Alterar papel [ADMIN]
DELETE /api/v1/users/{id}              # Desativar [ADMIN]

MAINTENANCE
GET    /api/v1/maintenance             # Listar todas [OPERATOR/ADMIN]
POST   /api/v1/maintenance             # Registrar manutenção [OPERATOR/ADMIN]
PUT    /api/v1/maintenance/{id}        # Atualizar manutenção [OPERATOR/ADMIN]

DASHBOARD & REPORTS
GET    /api/v1/dashboard/metrics       # KPIs gerais [OPERATOR/ADMIN]
GET    /api/v1/dashboard/top-vehicles  # Top veículos mais alugados [OPERATOR/ADMIN]
GET    /api/v1/dashboard/fleet-status  # Status da frota [OPERATOR/ADMIN]
GET    /api/v1/dashboard/fidelity      # Clientes por fidelidade [OPERATOR/ADMIN]
GET    /api/v1/reports/monthly         # Relatório mensal (?month=&year=) [OPERATOR/ADMIN]
GET    /api/v1/reports/financial       # Relatório financeiro (?start=&end=) [ADMIN]

BRANCHES
GET    /api/v1/branches                # Listar filiais (público)
POST   /api/v1/branches                # Criar filial [ADMIN]
PUT    /api/v1/branches/{id}           # Editar filial [ADMIN]

REVIEWS
POST   /api/v1/reviews                 # Criar avaliação [CLIENT, pós-aluguel]
GET    /api/v1/reviews/vehicle/{id}    # Avaliações de um veículo (público)
```

---

## 🎨 DESIGN SYSTEM — IDENTIDADE VISUAL RODALIVRELIVRE

```css
/* CSS VARIABLES — Paleta Profissional */
:root {
  /* Cores Primárias */
  --color-primary: #E63946;         /* Vermelho vibrante (ação, CTA) */
  --color-primary-dark: #C1121F;    /* Hover states */
  --color-primary-light: #FF6B6B;   /* Accent suave */

  /* Neutros */
  --color-dark: #0D1117;            /* Background escuro (admin) */
  --color-dark-2: #161B22;          /* Cards no admin */
  --color-dark-3: #21262D;          /* Borders escuro */
  --color-gray-1: #6E7681;          /* Texto secundário */
  --color-gray-2: #C9D1D9;          /* Texto principal claro */
  --color-white: #FFFFFF;

  /* Superfícies (site público — tema claro) */
  --color-surface: #FFFFFF;
  --color-surface-2: #F6F8FA;
  --color-surface-3: #EAEEF2;
  --color-text: #1C1E21;
  --color-text-secondary: #555F6D;
  --color-border: #D0D7DE;

  /* Status */
  --color-success: #2DA44E;
  --color-warning: #F0A500;
  --color-error: #CF222E;
  --color-info: #0969DA;

  /* Tipografia */
  --font-display: 'Syne', sans-serif;       /* Headings, logo */
  --font-body: 'DM Sans', sans-serif;       /* Corpo do texto */
  --font-mono: 'JetBrains Mono', monospace; /* Placas, códigos */

  /* Espaçamentos */
  --space-1: 4px; --space-2: 8px; --space-3: 12px; --space-4: 16px;
  --space-5: 20px; --space-6: 24px; --space-8: 32px; --space-10: 40px;
  --space-12: 48px; --space-16: 64px;

  /* Bordas */
  --radius-sm: 6px; --radius-md: 10px; --radius-lg: 16px; --radius-xl: 24px;
  --radius-full: 9999px;

  /* Sombras */
  --shadow-sm: 0 1px 3px rgba(0,0,0,0.12);
  --shadow-md: 0 4px 12px rgba(0,0,0,0.15);
  --shadow-lg: 0 8px 32px rgba(0,0,0,0.20);
  --shadow-primary: 0 4px 20px rgba(230,57,70,0.35);
}
```

---

## 🧩 COMPONENTES UI OBRIGATÓRIOS

### Site Público
- **Header:** Logo RodaLivre + nav links + botão login/perfil + menu mobile hamburger
- **Hero Section:** Background com veículo em destaque + formulário de busca (origem, datas, tipo)
- **Vehicle Card:** Foto, nome, categoria, transmissão, combustível, rating, diária, botão reservar
- **Search Form:** Date pickers nativos, select de local (filiais), autocomplete de categoria
- **Checkout Stepper:** Progress bar de 5 passos visível
- **Modal de Login:** Aparece ao tentar reservar sem estar logado

### Dashboard Admin
- **Sidebar:** Colapsável, icons + labels, badge de notificação em Aluguéis em atraso
- **KPI Card:** Ícone, título, valor, variação % em relação ao mês anterior, cor por tendência
- **Data Table:** Sort por coluna, busca inline, paginação, export CSV, ações por linha
- **Status Badge:** Pills coloridas (Disponível=verde, Alugado=azul, Manutenção=amarelo, Vencido=vermelho)
- **Maintenance Modal:** Form completo com todos os campos do registro de manutenção
- **Confirm Dialog:** Para ações destrutivas (cancelar, desativar)
- **Toast Notifications:** Sucesso/erro após operações

---

## 🔒 REGRAS DE NEGÓCIO CRÍTICAS

```
1. DISPONIBILIDADE:
   - Veículo em manutenção NÃO pode ser reservado (validação no backend)
   - Conflito de datas verificado server-side (overlap de reservas ativas)
   - Reserva bloqueia veículo imediatamente após confirmação de pagamento

2. TARIFAÇÃO:
   - Valor = (dias * diária) + adicionais escolhidos
   - Dias parciais contam como dia completo
   - Juros de atraso: 1% ao dia sobre o valor total da diária (configurável)
   - Cálculo de juros automático ao registrar devolução após data prevista

3. FIDELIDADE:
   - 1 ponto por real gasto
   - Níveis: Bronze (0-999pts), Prata (1000-4999pts), Ouro (5000-14999pts), Diamante (15000+pts)
   - Nível influencia desconto futuro (configurável pelo admin)

4. CANCELAMENTO:
   - Gratuito até 24h antes da retirada
   - 50% de multa entre 24h e 2h antes
   - Sem reembolso com menos de 2h

5. MANUTENÇÃO:
   - Status automaticamente muda para MAINTENANCE ao criar registro
   - Status volta para AVAILABLE ao registrar data de término real
   - Custo acumulado de manutenções visível por veículo

6. AUTORIZAÇÃO (RBAC — nunca validado só no frontend):
   - Cada endpoint verifica @PreAuthorize no controller
   - Cliente vê apenas seus próprios aluguéis
   - Operador vê tudo mas não pode alterar configurações
   - Admin tem acesso irrestrito
```

---

## 📦 DADOS MOCK — Seed para Portfólio

```
VEÍCULOS (mínimo 20, com fotos reais via URL):
- 4x Economy: Fiat Argo, VW Polo, Chevrolet Onix, Hyundai HB20
- 4x SUV: Jeep Compass, VW T-Cross, Toyota RAV4, Hyundai Creta
- 3x Luxury: BMW Serie 3, Mercedes Classe C, Audi A4
- 3x Sports: Ford Mustang GT, Chevrolet Camaro, Porsche Cayman
- 2x Full Size: Toyota Corolla, Honda Civic
- 2x Van: Fiat Ducato, Mercedes Sprinter
- 2x em manutenção (para demonstrar o fluxo)

CLIENTES (mínimo 15 fictícios):
- Nomes brasileiros comuns
- CPFs gerados via algoritmo válido (fictício)
- Histórico de aluguéis variado

ALUGUÉIS (mínimo 30 fictícios):
- Distribuídos nos últimos 6 meses
- Variedade de status: concluídos, ativos, em atraso, cancelados
- Alguns com juros aplicados

FILIAIS (3 fictícias):
- São Paulo - Centro
- Rio de Janeiro - Ipanema  
- Belo Horizonte - Savassi
```

---

## ✅ CHECKLIST DE ENTREGA

### Backend
- [ ] Todos os endpoints documentados no Swagger (`/swagger-ui.html`)
- [ ] Migrations Flyway versionadas e funcionando
- [ ] Seed de dados mock (V8__seed_mock_data.sql)
- [ ] Docker Compose funcional (app + postgres + redis)
- [ ] Todos os headers de segurança configurados
- [ ] Rate limiting ativo nos endpoints de auth
- [ ] Audit log funcionando para operações sensíveis
- [ ] Testes unitários para services críticos (mínimo 70% coverage)
- [ ] Tratamento global de exceções retornando JSON padronizado
- [ ] JWT com refresh token funcionando
- [ ] CORS configurado apenas para origem do frontend

### Frontend
- [ ] Responsivo (mobile-first, breakpoints: 375px, 768px, 1024px, 1440px)
- [ ] JWT armazenado de forma segura (httpOnly prefence — se SPA, usar memory + refresh)
- [ ] Todas as chamadas à API passam pelo wrapper `api.js` com token injetado
- [ ] Loading states em todas as operações assíncronas
- [ ] Tratamento de erros com mensagens amigáveis ao usuário
- [ ] Formulários com validação client-side (além da server-side)
- [ ] Dashboard com todos os gráficos funcionando (Chart.js)
- [ ] Sidebar colapsável no admin
- [ ] Proteção de rotas admin (redirect para login se não autenticado/autorizado)
- [ ] Fontes Syne + DM Sans carregadas via Google Fonts
- [ ] Favicon e meta tags (Open Graph) configurados

---

## 🚀 INSTRUÇÕES FINAIS PARA A IA DESENVOLVEDORA

1. **Comece pelo backend:** Configure o Spring Boot, segurança e banco de dados primeiro.
2. **Migrations antes de tudo:** Crie todas as tabelas via Flyway antes de escrever services.
3. **Nunca confie no frontend:** Todo dado recebido é validado com Bean Validation no DTO.
4. **DTOs separados de Entities:** Nunca exponha a entidade JPA diretamente na API.
5. **Seed realista:** Os dados mock devem contar uma história — reservas que fazem sentido cronologicamente.
6. **Frontend modular:** Cada página tem seu próprio JS. Componentes compartilhados ficam em `/js/components/`.
7. **Sem console.log em produção:** Use um logger dedicado ou remova antes de "subir".
8. **Fotos dos veículos:** Use URLs do Unsplash ou sites oficiais de marcas (Fiat, VW, Toyota, etc.) — sempre imagens reais de alta qualidade.
9. **Responsividade:** O design precisa funcionar bem em mobile, pois recrutadores verificam no celular.
10. **README.md:** Crie um README completo com instruções de como rodar o projeto (pré-requisitos, comandos, credenciais padrão de teste).

---

*RodaLivre © 2025 — Projeto de Portfólio | Todos os dados são fictícios*
