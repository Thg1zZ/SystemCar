# PROMPT DE SEGURANÇA — SYSTEMCAR (AutoLocadora)
## Baseado em: OWASP Top 10 (2021) · OWASP ASVS 4.0 · NIST SP 800-53 · CWE/SANS Top 25 · LGPD · ISO/IEC 27001

---

> **Instrução de uso:** Utilize este prompt como checklist obrigatório em cada Pull Request, revisão de código, sprint de segurança e auditoria periódica. Cada seção mapeia uma ameaça real para a stack Java 21 + Spring Boot 3 + Vanilla JS + Render/Vercel.

---

## 1. AUTENTICAÇÃO E GERENCIAMENTO DE SESSÃO
*Cobre: OWASP A07 — Falhas de Identificação e Autenticação · ASVS Capítulo 2 · NIST 800-53 IA*

### JWT (JSON Web Tokens)
- [ ] O segredo de assinatura JWT tem no mínimo **256 bits** gerado por CSPRNG (não use strings hardcoded como `"secret"` ou `"systemcar"`).
- [ ] O segredo é carregado **exclusivamente via variável de ambiente** (`JWT_SECRET`), nunca em `application.properties` versionado.
- [ ] Algoritmo de assinatura configurado como **HS256 ou RS256**. Algoritmo `none` explicitamente rejeitado no `JwtDecoder`.
- [ ] `exp` (expiração) definido: Access Token ≤ 15 min · Refresh Token ≤ 7 dias com rotação.
- [ ] Refresh Tokens armazenados **hasheados (SHA-256)** no banco; invalidados após uso único (rotation).
- [ ] Logout invalida o Refresh Token no servidor (lista de revogação ou deleção do registro).
- [ ] Claims mínimas: `sub` (userId), `role`, `iat`, `exp`, `jti` (UUID único por token para auditoria).
- [ ] O backend **revalida** role/permissão em cada request; nunca confia apenas no claim do token.
- [ ] Tokens **não são armazenados em `localStorage`** no frontend; use `httpOnly` cookie ou memória de estado.

### Senhas e Credenciais
- [ ] Senhas hasheadas com **BCrypt** (cost ≥ 12) ou **Argon2id** (Spring Security 6+ suporta nativamente).
- [ ] Política mínima: 12 caracteres, 1 maiúscula, 1 número, 1 especial. Validado no backend (nunca só no frontend).
- [ ] Rate limiting em `/api/auth/login`: máximo 5 tentativas por IP/usuário em 10 minutos → bloqueio temporário com `429 Too Many Requests`.
- [ ] Mensagens de erro genéricas: `"Credenciais inválidas"` (não diferenciar "usuário não existe" de "senha errada").
- [ ] Suporte a **MFA/TOTP** (desejável) para perfis ADMIN e OPERADOR.
- [ ] Endpoint de reset de senha via token temporário (UUID v4, expira em 15 min, uso único, enviado por e-mail).

---

## 2. CONTROLE DE ACESSO (AUTORIZAÇÃO)
*Cobre: OWASP A01 — Broken Access Control · ASVS Capítulo 4 · CWE-285, CWE-639*

### Roles e Permissões
- [ ] Modelo **RBAC** definido e documentado: `ROLE_CLIENTE`, `ROLE_OPERADOR`, `ROLE_ADMIN`.
- [ ] Todos os endpoints anotados com `@PreAuthorize("hasRole('...')")` ou configurados via `SecurityFilterChain`.
- [ ] **Nenhum endpoint** é `permitAll()` exceto login, registro e rota pública de catálogo.
- [ ] Validação de **propriedade do recurso**: cliente só acessa **seus próprios** aluguéis (`aluguel.cliente.id == jwtUserId`). Isso deve ser verificado na camada de Service, não só na query.

### IDOR (Insecure Direct Object Reference) — CWE-639
- [ ] IDs expostos na API são **UUIDs** (não inteiros sequenciais `1, 2, 3`). Isso impede enumeração.
- [ ] Antes de retornar qualquer recurso por ID, o Service verifica: `recurso.pertenceAo(usuarioLogado) || usuarioLogado.isAdmin()`.
- [ ] Nunca retornar lista completa de clientes para `ROLE_CLIENTE`.

### Separação Admin/Operador/Cliente
- [ ] Rotas `/api/admin/**` restritas a `ROLE_ADMIN`.
- [ ] Rotas `/api/operador/**` restritas a `ROLE_ADMIN` e `ROLE_OPERADOR`.
- [ ] Rotas `/api/cliente/**` restritas ao próprio cliente autenticado.
- [ ] Aprovação de aluguel, aplicação de multas e registro de manutenção exigem `ROLE_OPERADOR` ou `ROLE_ADMIN`.

---

## 3. INJEÇÃO (SQL, COMMAND, EXPRESSION)
*Cobre: OWASP A03 — Injection · CWE-89, CWE-564 · ASVS 5.3*

### Prevenção de SQL Injection
- [ ] **Zero SQL nativo concatenado com input do usuário**. Toda query usa JPQL com parâmetros nomeados ou `CriteriaBuilder`.
- [ ] Se usar `@Query` nativa, parâmetros sempre via `?1` ou `:nome`, nunca interpolação de string.
- [ ] DAO/Repository nunca recebe `String rawSql` como parâmetro vindo de Controller.
- [ ] Usuário do banco (ex: `systemcar_app`) tem permissões mínimas: apenas `SELECT, INSERT, UPDATE, DELETE` nas tabelas necessárias. **Sem `DROP`, `CREATE`, `GRANT`**.

### Prevenção de Expression Injection (SpEL)
- [ ] `@PreAuthorize` e `@PostFilter` não interpolam valores de request diretamente: `@PreAuthorize("hasRole('ADMIN') and #id == authentication.principal.id")` é seguro; concatenação de string não é.

### Prevenção de Command Injection — CWE-78
- [ ] Se houver qualquer `Runtime.exec()` ou `ProcessBuilder`, os argumentos são passados como **array** (não como string única) e nunca derivam de input do usuário.

---

## 4. CRIPTOGRAFIA E PROTEÇÃO DE DADOS
*Cobre: OWASP A02 — Cryptographic Failures · LGPD Art. 46 · ASVS Capítulo 6 · NIST 800-53 SC*

### Criptografia em Repouso (AES-256)
- [ ] CPF e CNH criptografados com **AES-256-GCM** (não AES-CBC sem autenticação).
- [ ] IV (Initialization Vector) **único por registro**, armazenado junto ao ciphertext (ex: `iv:ciphertext` em Base64).
- [ ] Chave AES carregada via variável de ambiente (`AES_KEY`), **nunca em código ou properties versionado**.
- [ ] Implementação usa `javax.crypto.Cipher` corretamente; não reinventa a criptografia manualmente.
- [ ] Considere usar **AWS KMS, HashiCorp Vault ou Google Secret Manager** para gestão de chaves em produção (Render suporta variáveis de ambiente secretas).
- [ ] Dados de log **nunca** contêm CPF, CNH, número de cartão ou senha, nem mesmo parcialmente.

### Criptografia em Trânsito
- [ ] Backend no Render usa **TLS 1.2+ exclusivamente** (configurado pela plataforma; verificar que HTTP puro está redirecionado para HTTPS).
- [ ] Frontend na Vercel serve apenas HTTPS (padrão da plataforma).
- [ ] Certificados válidos; configure **HSTS** no backend: `Strict-Transport-Security: max-age=31536000; includeSubDomains`.

### LGPD (Lei Geral de Proteção de Dados)
- [ ] Campos sensíveis mapeados no inventário de dados: CPF, CNH, e-mail, telefone, endereço.
- [ ] Finalidade de coleta documentada e mínima (princípio da necessidade).
- [ ] Endpoint `/api/cliente/meus-dados` implementado (direito de acesso, Art. 18 LGPD).
- [ ] Endpoint `/api/cliente/excluir-conta` implementado com anonimização (direito ao esquecimento), preservando registros de auditoria financeira por obrigação legal.
- [ ] Consentimento registrado no momento do cadastro com timestamp e versão da política de privacidade.
- [ ] DPO (Encarregado) nomeado e contato publicado na política de privacidade.

---

## 5. CONFIGURAÇÃO DE SEGURANÇA
*Cobre: OWASP A05 — Security Misconfiguration · ASVS 14 · CWE-16*

### Spring Boot 3 / Spring Security 6
- [ ] `spring.h2.console.enabled=false` em produção (`application-prod.properties`).
- [ ] `spring.jpa.show-sql=false` em produção.
- [ ] `management.endpoints.web.exposure.include=health` (Actuator expõe apenas `/health`; nunca `env`, `beans`, `mappings` em produção).
- [ ] Perfis de ambiente separados: `application-dev.properties` (H2, logs verbosos) vs `application-prod.properties` (PostgreSQL, logs estruturados).
- [ ] CORS configurado explicitamente: `allowedOrigins` lista apenas `https://systemcar.vercel.app` (nunca `*` em produção).
- [ ] `X-Content-Type-Options: nosniff` · `X-Frame-Options: DENY` · `Referrer-Policy: strict-origin-when-cross-origin` adicionados via `SecurityFilterChain`.
- [ ] Stack traces **nunca** retornados ao cliente. Exceptions mapeadas para respostas genéricas via `@ControllerAdvice`.

### PostgreSQL (Produção)
- [ ] Usuário da aplicação **não é superusuário**. Crie um role dedicado: `CREATE ROLE systemcar_app LOGIN PASSWORD '...'`.
- [ ] Conexão ao banco via **SSL** (Render PostgreSQL suporta; configure `spring.datasource.url` com `?sslmode=require`).
- [ ] Backups automáticos habilitados na plataforma Render.
- [ ] `pg_hba.conf` restrito ao IP do servidor de aplicação (se gerenciando instância própria).

### Frontend (Vanilla JS / Vercel)
- [ ] **CSP (Content Security Policy)** configurada no `vercel.json`:
  ```
  Content-Security-Policy: default-src 'self'; script-src 'self'; connect-src https://systemcar-api.onrender.com; style-src 'self' 'unsafe-inline'; img-src 'self' data:;
  ```
- [ ] `X-Frame-Options: DENY` no Vercel headers.
- [ ] Nenhuma chave secreta ou token hardcoded nos arquivos JS/HTML.

---

## 6. COMPONENTES VULNERÁVEIS E DEPENDÊNCIAS
*Cobre: OWASP A06 — Vulnerable and Outdated Components · CWE-937 · NIST 800-53 SI-2*

- [ ] **Maven Dependency Check** configurado no `pom.xml` (plugin `org.owasp:dependency-check-maven`). Executar antes de cada release.
- [ ] `mvn versions:display-dependency-updates` rodado mensalmente para identificar atualizações.
- [ ] **Dependabot** ou **Renovate** habilitado no repositório GitHub para PRs automáticos de atualização.
- [ ] `npm audit` (ou `pnpm audit`) rodado no projeto frontend antes de cada deploy.
- [ ] Spring Boot 3.x atualizado para a minor mais recente da mesma série (patches de segurança).
- [ ] **SBOM (Software Bill of Materials)** gerado (plugin CycloneDX para Maven) para rastreabilidade.
- [ ] Java 21 — usar distribuição LTS com patches de segurança (Eclipse Temurin ou Azul Zulu no Render).

---

## 7. FALHAS DE INTEGRIDADE (SUPPLY CHAIN)
*Cobre: OWASP A08 — Software and Data Integrity Failures · CWE-494 · NIST 800-53 SA-12*

- [ ] **Checksums verificados** para artefatos baixados em CI/CD (Docker images, JARs de terceiros).
- [ ] Pipeline CI/CD (GitHub Actions ou similar) não usa `@latest` em actions; pins de versão com SHA256: `uses: actions/checkout@v4.1.1` (use SHA para máxima segurança).
- [ ] Secrets do CI/CD armazenados em **GitHub Secrets / Render Environment Variables**, nunca em `.env` commitado.
- [ ] `.gitignore` inclui: `.env`, `*.key`, `application-prod.properties`, `application-secrets.properties`.
- [ ] Deploy via artifact versionado (JAR assinado ou imagem Docker com digest imutável).

---

## 8. LOGGING, MONITORAMENTO E AUDITORIA
*Cobre: OWASP A09 — Security Logging and Monitoring Failures · NIST 800-53 AU · ASVS Capítulo 7*

### Logs de Segurança (Obrigatórios)
- [ ] Login bem-sucedido: `userId, role, timestamp, ip, userAgent`.
- [ ] Tentativa de login falha: `email_tentado (hash), timestamp, ip` (não logar senha nem email em claro).
- [ ] Acesso negado (403): `userId, endpoint, método, timestamp`.
- [ ] Operações sensíveis de admin: aprovação de aluguel, aplicação de multa, criação/deleção de usuário.
- [ ] Alteração de dados criptografados (CPF/CNH): registro de quem alterou e quando (sem logar o valor).

### Formato e Armazenamento
- [ ] Logs em formato **estruturado JSON** (Logback + `logstash-logback-encoder`), não texto livre.
- [ ] Logs de aplicação separados de logs de auditoria (arquivos ou tabelas diferentes).
- [ ] Tabela de auditoria no banco com campos: `id, userId, acao, entidade, entidadeId, valorAnterior (hash), valorNovo (hash), timestamp, ip`.
- [ ] Logs **imutáveis**: usuários da aplicação não têm permissão `DELETE` na tabela de auditoria.
- [ ] Retenção de logs de auditoria: mínimo 5 anos (conformidade fiscal e LGPD).
- [ ] Alertas configurados para: 5+ logins falhos do mesmo IP em 1 minuto, acesso a `/api/admin` fora do horário comercial.

---

## 9. FALSIFICAÇÃO DE REQUISIÇÃO (CSRF E SSRF)
*Cobre: OWASP A10 — Server-Side Request Forgery · CWE-352, CWE-918 · ASVS 4.2*

### CSRF
- [ ] Se usar cookies para sessão/JWT: token CSRF implementado (Spring Security `CsrfTokenRepository`).
- [ ] Se API stateless com Bearer token em header Authorization: CSRF naturalmente mitigado (navegador não envia headers customizados cross-origin sem CORS pré-flight).
- [ ] Frontend sempre envia JWT em `Authorization: Bearer <token>`, nunca em cookie automático.

### SSRF — CWE-918
- [ ] Se houver endpoints que fazem chamadas HTTP a URLs externas (ex: validação de endereço, webhook): a URL **não pode ser fornecida pelo usuário** sem whitelist explícita.
- [ ] Whitelist de domínios externos permitidos para chamadas server-side.
- [ ] Metadado de nuvem (`169.254.169.254`) explicitamente bloqueado em firewalls/egress rules no Render.

---

## 10. XSS (CROSS-SITE SCRIPTING)
*Cobre: OWASP (incluído em A03 na versão 2021) · CWE-79 · ASVS 5.3.3*

### Frontend (Vanilla JS)
- [ ] **Nunca usar `innerHTML` com dados vindos da API**. Usar `textContent` ou `createElement` + `appendChild`.
- [ ] Dados de veículos, nomes de clientes e mensagens de erro sempre renderizados via `textContent`.
- [ ] Parâmetros de URL (query strings) sanitizados antes de exibir na página.
- [ ] CSP configurado no Vercel (ver seção 5) como camada adicional de defesa.

### Backend
- [ ] Validação de entrada com **Bean Validation** (`@NotBlank`, `@Size`, `@Pattern`) em todos os DTOs.
- [ ] Resposta JSON com `Content-Type: application/json` sempre (nunca `text/html` para endpoints de API).
- [ ] Biblioteca **OWASP Java HTML Sanitizer** usada se qualquer campo aceitar texto rico.

---

## 11. VALIDAÇÃO E SANITIZAÇÃO DE ENTRADA
*Cobre: ASVS 5.1 e 5.2 · CWE-20 · NIST 800-53 SI-10*

- [ ] **Todo DTO** recebido pelo Controller tem anotações Bean Validation e `@Valid` no parâmetro.
- [ ] CPF validado via algoritmo de dígitos verificadores (não apenas formato).
- [ ] CNH validada por formato e tipo de habilitação aceito.
- [ ] Datas de aluguel: `dataInicio < dataFim`, datas não no passado para novas reservas.
- [ ] Valores monetários (diária, multa): não negativos, máximo razoável definido.
- [ ] Upload de arquivos (se houver): validar MIME type real (magic bytes), não só extensão. Limitar tamanho. Armazenar fora do webroot.
- [ ] Campos de texto livre: tamanho máximo definido (`@Size(max = 500)`).
- [ ] **Backend nunca confia em validações do frontend** — toda regra crítica reimplementada na camada de Service.

---

## 12. GERENCIAMENTO DE ERROS E INFORMAÇÕES SENSÍVEIS
*Cobre: CWE-209 · ASVS 7.4 · OWASP Testing Guide*

- [ ] `@ControllerAdvice` global captura todas as exceções não tratadas e retorna `{"error": "Ocorreu um erro interno."}` com status 500.
- [ ] Exceções de negócio mapeadas para respostas semânticas: `404 Not Found`, `400 Bad Request`, `403 Forbidden`, `409 Conflict`.
- [ ] Mensagens de erro **não revelam**: tipo de banco, versão do Spring, nome de classes internas, stack trace, estrutura de tabelas.
- [ ] IDs internos de banco (Long sequencial) não expostos em mensagens de erro. Use o UUID do recurso.

---

## 13. SEGURANÇA DA API REST
*Cobre: OWASP API Security Top 10 · ASVS 13*

- [ ] **Rate Limiting** global: ex. 100 req/min por IP (usar Bucket4j + Spring Boot ou configuração no Render).
- [ ] Rate limiting mais restritivo em endpoints sensíveis: `/auth/login` (5/min), `/auth/register` (3/min).
- [ ] Paginação obrigatória em listagens: `page` e `size` com `size` máximo de 100. Nunca retornar tabela completa.
- [ ] Filtros de busca com limite: ex. busca de veículos por nome não pode retornar >100 resultados.
- [ ] Campos retornados pela API seguem princípio do mínimo necessário (nunca retornar hash de senha, IV de criptografia, campos internos do banco).
- [ ] Versioning de API (`/api/v1/`) para facilitar deprecação segura.
- [ ] Documentação OpenAPI/Swagger disponível **apenas em ambiente de desenvolvimento** (desabilitada em produção ou protegida por autenticação).

---

## 14. SEGURANÇA DE INFRAESTRUTURA (RENDER + VERCEL)
*Cobre: NIST 800-53 SC, CM · ISO 27001 A.12*

### Render (Backend)
- [ ] Variáveis de ambiente secretas (`JWT_SECRET`, `AES_KEY`, `DATABASE_URL`) configuradas como **Environment Variables secretas** no painel Render, nunca em arquivos commitados.
- [ ] Health check configurado (`/actuator/health`) para restart automático em falha.
- [ ] Auto-deploy habilitado apenas da branch `main`/`production` (não de branches de feature).
- [ ] Revisar logs do Render regularmente para erros 5xx e padrões anômalos.

### Vercel (Frontend)
- [ ] `vercel.json` com headers de segurança configurados (CSP, HSTS, X-Frame-Options, Permissions-Policy).
- [ ] Preview deployments de PRs não expõem dados reais (usar ambiente de staging com dados fictícios).
- [ ] Variáveis de ambiente do frontend (`VITE_API_URL`) são públicas por natureza; **nunca colocar secrets no frontend**.

---

## 15. TESTES DE SEGURANÇA
*Cobre: ASVS Capítulo 1 · NIST 800-115 · OWASP Testing Guide*

### Testes Automatizados
- [ ] **Testes unitários** de segurança: verificar que endpoints admin retornam 403 para `ROLE_CLIENTE`.
- [ ] Testes de IDOR: verificar que cliente A não acessa recursos do cliente B.
- [ ] Testes de validação: enviar payloads inválidos e verificar `400 Bad Request`.
- [ ] **OWASP ZAP** (Zed Attack Proxy) integrado ao pipeline CI para scan passivo em cada deploy.

### Testes Manuais Periódicos (a cada 6 meses ou grande release)
- [ ] Revisão de código focada em segurança (Security Code Review).
- [ ] Pentest básico com OWASP ZAP ou Burp Suite Free.
- [ ] Verificação de dependências desatualizadas.
- [ ] Revisão de permissões no banco de dados e variáveis de ambiente.

---

## CHECKLIST RÁPIDO — PRÉ-DEPLOY

```
[ ] Nenhum secret ou senha em código ou properties versionado
[ ] H2 Console desabilitado, Actuator restrito
[ ] CORS configurado apenas para domínio de produção
[ ] Logs não contêm CPF, CNH ou senhas
[ ] Todas as rotas com autenticação e autorização correta
[ ] dependency-check executado sem vulnerabilidades críticas
[ ] Testes de segurança passando no CI
[ ] Headers de segurança configurados (CSP, HSTS, X-Frame-Options)
[ ] Variáveis de ambiente de produção configuradas na plataforma
[ ] Backup do banco habilitado
```

---

## REFERÊNCIAS

| Norma | Escopo | Link |
|---|---|---|
| OWASP Top 10 2021 | Top vulnerabilidades web | owasp.org/Top10 |
| OWASP ASVS 4.0 | Padrão de verificação de segurança | owasp.org/ASVS |
| OWASP API Security Top 10 | Segurança específica de APIs REST | owasp.org/API-Security |
| CWE/SANS Top 25 | Fraquezas de software mais perigosas | cwe.mitre.org/top25 |
| LGPD (Lei 13.709/2018) | Proteção de dados pessoais no Brasil | planalto.gov.br |
| NIST SP 800-53 Rev. 5 | Controles de segurança federais (EUA) | csrc.nist.gov |
| ISO/IEC 27001:2022 | Sistema de gestão de segurança da informação | iso.org |
| OWASP Testing Guide v4.2 | Metodologia de testes de segurança | owasp.org/WSTG |

---

*Versão: 1.0 — Stack: Java 21 + Spring Boot 3 + Vanilla JS + PostgreSQL + Render + Vercel*
*Gerado para: SystemCar AutoLocadora*
