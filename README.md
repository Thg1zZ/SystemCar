# 🚗 AutoLocadora — Sistema de Gestão em Java

Sistema completo de gestão de locadora de veículos desenvolvido em Java 21 puro,
com arquitetura em camadas, orientação a objetos e interface de linha de comando (CLI).

---

## 📦 Estrutura do Projeto

```
locadora/
├── src/main/java/locadora/
│   ├── Main.java                        ← Ponto de entrada
│   ├── model/
│   │   ├── Veiculo.java                 ← Entidade veículo
│   │   ├── Cliente.java                 ← Entidade cliente (com fidelidade)
│   │   ├── Aluguel.java                 ← Contrato de aluguel
│   │   ├── Funcionario.java             ← Operador do sistema
│   │   ├── StatusVeiculo.java           ← Enum: DISPONIVEL, ALUGADO, MANUTENCAO…
│   │   ├── StatusAluguel.java           ← Enum: ATIVO, CONCLUIDO, CANCELADO…
│   │   └── CategoriaVeiculo.java        ← Enum: ECONOMICO, SUV, LUXO, ELETRICO…
│   ├── service/
│   │   ├── VeiculoService.java          ← Regras de negócio de veículos
│   │   ├── ClienteService.java          ← Regras de negócio de clientes
│   │   ├── AluguelService.java          ← Abertura, devolução, cancelamento
│   │   └── RelatorioService.java        ← Métricas, receita, ranking
│   ├── repository/
│   │   └── Repository.java              ← DAO genérico in-memory (simulação JPA)
│   ├── exception/
│   │   ├── LocadoraException.java       ← Exceção base do domínio
│   │   └── VeiculoIndisponivelException ← Exceção específica
│   ├── util/
│   │   ├── Formatador.java              ← Formatação de moeda, data, placa, CPF
│   │   ├── Validador.java               ← Validação de CPF, placa, campos
│   │   └── DataSeeder.java              ← Dados de demonstração
│   └── ui/
│       └── MenuPrincipal.java           ← Interface CLI completa
└── locadora.jar                         ← JAR executável
```

---

## ▶️ Como Executar

### Pré-requisito
- Java 17+ instalado (`java -version`)

### Via JAR (mais fácil)
```bash
java -jar locadora.jar
```

### Compilar do zero
```bash
# Compilar
find src -name "*.java" > sources.txt
javac -encoding UTF-8 -d out @sources.txt

# Executar
java -cp out locadora.Main

# Empacotar em JAR
jar cfm locadora.jar manifest.txt -C out .
java -jar locadora.jar
```

---

## 🏗️ Arquitetura — Camadas

```
┌──────────────────────────────────────────┐
│            UI (MenuPrincipal)            │  ← entrada/saída via Scanner
├──────────────────────────────────────────┤
│    Service Layer  (regras de negócio)    │  ← validações, cálculos, orquestração
├──────────────────────────────────────────┤
│    Repository (DAO genérico in-memory)   │  ← CRUD, busca, filtros
├──────────────────────────────────────────┤
│    Model  (entidades do domínio)         │  ← Veiculo, Cliente, Aluguel…
└──────────────────────────────────────────┘
```

> **Em produção**, substitua `Repository<T>` por `JpaRepository<T>` (Spring Data)
> ou `EntityManager` (Jakarta EE). As camadas Service e Model permanecem inalteradas.

---

## ✅ Funcionalidades

### Veículos
- Cadastro com marca, modelo, ano, placa, cor, categoria, diária, km
- Opcionais: ar-condicionado, direção hidráulica, câmbio automático
- Validação de placa (padrão clássico e Mercosul)
- Envio/retorno de manutenção com histórico
- Busca por placa, listagem por categoria e status

### Clientes
- Cadastro com CPF/CNPJ, CNH com validade, telefone, e-mail, endereço
- Validação de dígito verificador do CPF
- Programa de fidelidade automático: Bronze → Prata → Ouro → Platina
- Descontos automáticos por nível (5%, 10%, 15%)
- Controle de inadimplência
- Bloqueio de aluguel para CNH vencida ou inadimplente

### Aluguéis
- Abertura com validação de disponibilidade e permissão do cliente
- Geração de contrato completo (texto formatado)
- 3 tipos de seguro: Básico (5%), Intermediário (10%), Completo (20%)
- Registro de devolução com: data efetiva, KM final, valor de danos
- Cálculo automático de multa por atraso (diária × 1,5 por dia)
- Cancelamento com motivo
- Atualização automática da quilometragem do veículo

### Relatórios
- Relatório gerencial completo
- Receita por período
- Distribuição da frota por status e categoria
- Taxa de ocupação da frota
- Top N veículos mais alugados
- Clientes por categoria de fidelidade
- Cliente mais ativo

---

## 🔧 Regras de Negócio Implementadas

| Regra | Implementação |
|-------|--------------|
| Veículo alugado não pode ir para manutenção | `VeiculoService.enviarParaManutencao()` |
| CNH vencida bloqueia aluguel | `Cliente.cnhValida()` |
| Inadimplente não pode alugar | `Cliente.podeAlugar()` |
| Multa de atraso = diária × 1,5 × dias atraso | `Aluguel.calcularMultaAtraso()` |
| Desconto automático por fidelidade | `Cliente.getDesconto()` |
| Placa única por veículo | `VeiculoService.cadastrar()` |
| CPF único por cliente | `ClienteService.cadastrar()` |
| KM final ≥ KM inicial | `AluguelService.registrarDevolucao()` |

---

## 💡 Extensões Sugeridas

- **Persistência**: Substituir `Repository` por H2/MySQL com JPA/Hibernate
- **API REST**: Adicionar Spring Boot com controllers
- **Autenticação**: Implementar login por `Funcionario` com BCrypt
- **Reservas antecipadas**: Status `RESERVADO` já existe no enum
- **Multa por km excedido**: Adicionar km máximo contratado no `Aluguel`
- **Notificações**: Envio de e-mail na devolução/atraso (JavaMail)
- **Interface gráfica**: JavaFX ou Swing sobre os mesmos Services

---

## 📋 Dados de Demonstração (pré-carregados)

Ao iniciar, o sistema carrega automaticamente:
- **12 veículos** (todas as categorias, incluindo elétrico)
- **7 clientes** com CNH válida
- **6 aluguéis** (3 ativos, 3 concluídos, 1 com dano)

---

*Desenvolvido com Java 21 · Sem dependências externas · Pronto para compilar e executar*
