package com.rodalivre.config;

import com.rodalivre.domain.entity.Branch;
import com.rodalivre.domain.entity.Rental;
import com.rodalivre.domain.entity.User;
import com.rodalivre.domain.entity.Vehicle;
import com.rodalivre.domain.enums.*;
import com.rodalivre.repository.BranchRepository;
import com.rodalivre.repository.RentalRepository;
import com.rodalivre.repository.UserRepository;
import com.rodalivre.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final BranchRepository branchRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed Branches (Agências)
        if (branchRepository.count() == 0) {
            seedBranches();
        }

        // 2. Seed Users (Usuários de teste)
        if (userRepository.count() == 0) {
            seedUsers();
        }

        // 3. Seed Vehicles (Veículos de teste)
        if (vehicleRepository.count() == 0) {
            seedVehicles();
        }

        // 4. Seed Rentals (Aluguéis de teste)
        if (rentalRepository.count() == 0) {
            seedRentals();
        }
    }

    private void seedBranches() {
        Branch b1 = Branch.builder()
                .name("Agência Aeroporto")
                .street("Avenida Santos Dumont, 3000")
                .city("São Paulo")
                .state("SP")
                .zipCode("04626-000")
                .latitude(new BigDecimal("-23.6268"))
                .longitude(new BigDecimal("-46.6565"))
                .phone("(11) 5555-1234")
                .active(true)
                .build();

        Branch b2 = Branch.builder()
                .name("Agência Copacabana")
                .street("Avenida Atlântica, 1702")
                .city("Rio de Janeiro")
                .state("RJ")
                .zipCode("22021-001")
                .latitude(new BigDecimal("-22.9698"))
                .longitude(new BigDecimal("-43.1794"))
                .phone("(21) 5555-5678")
                .active(true)
                .build();

        Branch b3 = Branch.builder()
                .name("Agência Savassi")
                .street("Avenida Cristóvão Colombo, 135")
                .city("Belo Horizonte")
                .state("MG")
                .zipCode("30140-140")
                .latitude(new BigDecimal("-19.9385"))
                .longitude(new BigDecimal("-43.9376"))
                .phone("(31) 5555-9999")
                .active(true)
                .build();

        branchRepository.saveAll(Arrays.asList(b1, b2, b3));
    }

    private void seedUsers() {
        // Admin
        User admin = User.builder()
                .fullName("Thiago Gomes (Admin)")
                .email("admin@rodalivre.com")
                .passwordHash(passwordEncoder.encode("admin123"))
                .cpf("111.111.111-11")
                .phone("(11) 99999-1111")
                .birthDate(LocalDate.of(1995, 5, 15))
                .cnh("12345678901")
                .cnhExpirationDate(LocalDate.now().plusYears(5))
                .inadimplente(false)
                .role(UserRole.ADMIN)
                .fidelityPoints(0)
                .active(true)
                .build();

        // Operador
        User operator = User.builder()
                .fullName("Carlos Souza (Operador)")
                .email("operator@rodalivre.com")
                .passwordHash(passwordEncoder.encode("operator123"))
                .cpf("222.222.222-22")
                .phone("(11) 99999-2222")
                .birthDate(LocalDate.of(1998, 8, 20))
                .cnh("98765432109")
                .cnhExpirationDate(LocalDate.now().plusYears(3))
                .inadimplente(false)
                .role(UserRole.OPERATOR)
                .fidelityPoints(0)
                .active(true)
                .build();

        // ---- 7 Clientes com CNH Válida ----
        User u1 = User.builder()
                .fullName("João Silva")
                .email("joao@email.com")
                .passwordHash(passwordEncoder.encode("senha123"))
                .cpf("111.222.333-44")
                .phone("(11) 99999-0001")
                .birthDate(LocalDate.of(1990, 1, 1))
                .cnh("11111111111")
                .cnhExpirationDate(LocalDate.now().plusYears(4))
                .inadimplente(false)
                .role(UserRole.CLIENT)
                .fidelityPoints(1200) // Ouro
                .active(true)
                .build();

        User u2 = User.builder()
                .fullName("Maria Santos")
                .email("maria@email.com")
                .passwordHash(passwordEncoder.encode("senha123"))
                .cpf("222.333.444-55")
                .phone("(11) 99999-0002")
                .birthDate(LocalDate.of(1992, 2, 2))
                .cnh("22222222222")
                .cnhExpirationDate(LocalDate.now().plusYears(3))
                .inadimplente(false)
                .role(UserRole.CLIENT)
                .fidelityPoints(600) // Prata
                .active(true)
                .build();

        User u3 = User.builder()
                .fullName("Pedro Souza")
                .email("pedro@email.com")
                .passwordHash(passwordEncoder.encode("senha123"))
                .cpf("333.444.555-66")
                .phone("(11) 99999-0003")
                .birthDate(LocalDate.of(1988, 3, 3))
                .cnh("33333333333")
                .cnhExpirationDate(LocalDate.now().plusYears(2))
                .inadimplente(false)
                .role(UserRole.CLIENT)
                .fidelityPoints(2200) // Diamante
                .active(true)
                .build();

        User u4 = User.builder()
                .fullName("Ana Oliveira")
                .email("ana@email.com")
                .passwordHash(passwordEncoder.encode("senha123"))
                .cpf("444.555.666-77")
                .phone("(11) 99999-0004")
                .birthDate(LocalDate.of(1995, 4, 4))
                .cnh("44444444444")
                .cnhExpirationDate(LocalDate.now().plusYears(5))
                .inadimplente(false)
                .role(UserRole.CLIENT)
                .fidelityPoints(0) // Bronze
                .active(true)
                .build();

        User u5 = User.builder()
                .fullName("Lucas Lima")
                .email("lucas@email.com")
                .passwordHash(passwordEncoder.encode("senha123"))
                .cpf("555.666.777-88")
                .phone("(11) 99999-0005")
                .birthDate(LocalDate.of(1993, 5, 5))
                .cnh("55555555555")
                .cnhExpirationDate(LocalDate.now().plusYears(1))
                .inadimplente(false)
                .role(UserRole.CLIENT)
                .fidelityPoints(100) // Bronze
                .active(true)
                .build();

        User u6 = User.builder()
                .fullName("Juliana Costa")
                .email("juliana@email.com")
                .passwordHash(passwordEncoder.encode("senha123"))
                .cpf("666.777.888-99")
                .phone("(11) 99999-0006")
                .birthDate(LocalDate.of(1991, 6, 6))
                .cnh("66666666666")
                .cnhExpirationDate(LocalDate.now().plusYears(3))
                .inadimplente(false)
                .role(UserRole.CLIENT)
                .fidelityPoints(800) // Prata
                .active(true)
                .build();

        User u7 = User.builder()
                .fullName("Roberto Pereira")
                .email("roberto@email.com")
                .passwordHash(passwordEncoder.encode("senha123"))
                .cpf("777.888.999-00")
                .phone("(11) 99999-0007")
                .birthDate(LocalDate.of(1989, 7, 7))
                .cnh("77777777777")
                .cnhExpirationDate(LocalDate.now().plusYears(4))
                .inadimplente(false)
                .role(UserRole.CLIENT)
                .fidelityPoints(500) // Prata
                .active(true)
                .build();

        // Clientes adicionais para testar restrições
        User expiredCnh = User.builder()
                .fullName("Marcos CNH Vencida")
                .email("marcos_vencido@email.com")
                .passwordHash(passwordEncoder.encode("senha123"))
                .cpf("888.888.888-88")
                .phone("(11) 99999-9991")
                .birthDate(LocalDate.of(1985, 3, 25))
                .cnh("88888888888")
                .cnhExpirationDate(LocalDate.now().minusDays(10)) // Vencida
                .inadimplente(false)
                .role(UserRole.CLIENT)
                .active(true)
                .build();

        User clientInadimplente = User.builder()
                .fullName("Aline Inadimplente")
                .email("aline_devedora@email.com")
                .passwordHash(passwordEncoder.encode("senha123"))
                .cpf("999.999.999-99")
                .phone("(11) 99999-9992")
                .birthDate(LocalDate.of(1993, 12, 12))
                .cnh("99999999999")
                .cnhExpirationDate(LocalDate.now().plusYears(2))
                .inadimplente(true) // Inadimplente
                .role(UserRole.CLIENT)
                .active(true)
                .build();

        userRepository.saveAll(Arrays.asList(admin, operator, u1, u2, u3, u4, u5, u6, u7, expiredCnh, clientInadimplente));
    }

    private void seedVehicles() {
        List<Branch> branches = branchRepository.findAll();
        if (branches.isEmpty()) return;

        Branch bAeroporto = branches.get(0);
        Branch bCentro = branches.get(1);

        // ---- 12 VEÍCULOS DE TODAS AS CATEGORIAS ----

        // 1. Chevrolet Onix (Economy)
        Vehicle v1 = Vehicle.builder()
                .brand("Chevrolet")
                .model("Onix 1.0 Turbo")
                .year(2023)
                .licensePlate("ABC1D23")
                .category(VehicleCategory.ECONOMY)
                .status(VehicleStatus.RENTED) // Alugado para um dos aluguéis ativos
                .dailyRate(new BigDecimal("110.00"))
                .mileage(12000)
                .color("Prata")
                .seats(5)
                .transmission(Transmission.MANUAL)
                .fuelType(FuelType.GASOLINE)
                .description("Carro hatch super econômico, ideal para trânsito urbano e viagens curtas.")
                .features("[\"Ar condicionado\", \"Direção elétrica\", \"Vidros elétricos\", \"Som Bluetooth\"]")
                .imageUrls("[\"https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&q=80&w=600\"]")
                .locationBranch(bAeroporto)
                .build();

        // 2. Fiat Argo (Economy)
        Vehicle v2 = Vehicle.builder()
                .brand("Fiat")
                .model("Argo Drive 1.3")
                .year(2023)
                .licensePlate("FIT2A23")
                .category(VehicleCategory.ECONOMY)
                .status(VehicleStatus.AVAILABLE)
                .dailyRate(new BigDecimal("120.00"))
                .mileage(15000)
                .color("Vermelho")
                .seats(5)
                .transmission(Transmission.MANUAL)
                .fuelType(FuelType.FLEX)
                .description("Hatch prático, com excelente consumo e direção muito confortável.")
                .features("[\"Ar condicionado\", \"Direção elétrica\", \"Central multimídia\", \"Sensor de ré\"]")
                .imageUrls("[\"https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&q=80&w=600\"]")
                .locationBranch(bAeroporto)
                .build();

        // 3. Hyundai HB20 (Economy)
        Vehicle v3 = Vehicle.builder()
                .brand("Hyundai")
                .model("HB20 Sense")
                .year(2023)
                .licensePlate("HYU3B30")
                .category(VehicleCategory.ECONOMY)
                .status(VehicleStatus.AVAILABLE)
                .dailyRate(new BigDecimal("115.00"))
                .mileage(14200)
                .color("Branco")
                .seats(5)
                .transmission(Transmission.MANUAL)
                .fuelType(FuelType.FLEX)
                .description("Design moderno com ótima tecnologia e excelente dirigibilidade para o dia a dia.")
                .features("[\"Ar condicionado\", \"Direção elétrica\", \"Controle de estabilidade\", \"Som Bluetooth\"]")
                .imageUrls("[\"https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&q=80&w=600\"]")
                .locationBranch(bCentro)
                .build();

        // 4. Toyota Corolla (Intermediate)
        Vehicle v4 = Vehicle.builder()
                .brand("Toyota")
                .model("Corolla XEi 2.0")
                .year(2022)
                .licensePlate("COR4A22")
                .category(VehicleCategory.INTERMEDIATE)
                .status(VehicleStatus.RENTED) // Alugado para um dos aluguéis ativos
                .dailyRate(new BigDecimal("220.00"))
                .mileage(28000)
                .color("Preto")
                .seats(5)
                .transmission(Transmission.AUTOMATIC)
                .fuelType(FuelType.FLEX)
                .description("Sedã médio de altíssima reputação. Confortável, silencioso e muito seguro.")
                .features("[\"Ar digital\", \"Bancos em couro\", \"Câmbio CVT\", \"Central Multimídia\", \"Piloto automático\"]")
                .imageUrls("[\"https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?auto=format&fit=crop&q=80&w=600\"]")
                .locationBranch(bAeroporto)
                .build();

        // 5. Honda Civic (Intermediate)
        Vehicle v5 = Vehicle.builder()
                .brand("Honda")
                .model("Civic EXL 2.0")
                .year(2021)
                .licensePlate("CIV5X50")
                .category(VehicleCategory.INTERMEDIATE)
                .status(VehicleStatus.AVAILABLE)
                .dailyRate(new BigDecimal("230.00"))
                .mileage(35000)
                .color("Cinza Metálico")
                .seats(5)
                .transmission(Transmission.AUTOMATIC)
                .fuelType(FuelType.GASOLINE)
                .description("Sedã com pegada esportiva, excelente espaço interno e estabilidade excepcional.")
                .features("[\"Ar digital dual-zone\", \"Câmbio automático CVT\", \"Bancos de couro\", \"Faróis em LED\"]")
                .imageUrls("[\"https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?auto=format&fit=crop&q=80&w=600\"]")
                .locationBranch(bCentro)
                .build();

        // 6. Chevrolet Cruze (Full Size)
        Vehicle v6 = Vehicle.builder()
                .brand("Chevrolet")
                .model("Cruze Premier 1.4T")
                .year(2022)
                .licensePlate("CRU6P60")
                .category(VehicleCategory.FULL_SIZE)
                .status(VehicleStatus.AVAILABLE)
                .dailyRate(new BigDecimal("260.00"))
                .mileage(21000)
                .color("Azul Escuro")
                .seats(5)
                .transmission(Transmission.AUTOMATIC)
                .fuelType(FuelType.FLEX)
                .description("Sedã grande turbo com conectividade total, Wi-Fi nativo e muita tecnologia embarcada.")
                .features("[\"Motor Turbo\", \"Wi-Fi a bordo\", \"Alerta de ponto cego\", \"Teto solar\", \"Partida por botão\"]")
                .imageUrls("[\"https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?auto=format&fit=crop&q=80&w=600\"]")
                .locationBranch(bAeroporto)
                .build();

        // 7. Jeep Compass (SUV)
        Vehicle v7 = Vehicle.builder()
                .brand("Jeep")
                .model("Compass TD350 4x4")
                .year(2023)
                .licensePlate("JEP7C35")
                .category(VehicleCategory.SUV)
                .status(VehicleStatus.RENTED) // Alugado para um dos aluguéis ativos
                .dailyRate(new BigDecimal("290.00"))
                .mileage(18500)
                .color("Cinza Escuro")
                .seats(5)
                .transmission(Transmission.AUTOMATIC)
                .fuelType(FuelType.DIESEL)
                .description("SUV robusto com tração 4x4 a diesel. Conforto total na cidade e desempenho off-road.")
                .features("[\"Tração 4x4\", \"Motor Diesel\", \"Painel Digital\", \"Teto solar panorâmico\"]")
                .imageUrls("[\"https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?auto=format&fit=crop&q=80&w=600\"]")
                .locationBranch(bAeroporto)
                .build();

        // 8. VW T-Cross (SUV)
        Vehicle v8 = Vehicle.builder()
                .brand("Volkswagen")
                .model("T-Cross Comfortline")
                .year(2023)
                .licensePlate("TCR8C80")
                .category(VehicleCategory.SUV)
                .status(VehicleStatus.AVAILABLE)
                .dailyRate(new BigDecimal("240.00"))
                .mileage(16000)
                .color("Bronze")
                .seats(5)
                .transmission(Transmission.AUTOMATIC)
                .fuelType(FuelType.FLEX)
                .description("SUV compacto com muito espaço interno, motor turbo econômico e painel digital.")
                .features("[\"Motor Turbo TSI\", \"Painel digital Active Info Display\", \"Câmera de ré\", \"Sensores de ré\"]")
                .imageUrls("[\"https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?auto=format&fit=crop&q=80&w=600\"]")
                .locationBranch(bCentro)
                .build();

        // 9. BYD Seal (Luxury)
        Vehicle v9 = Vehicle.builder()
                .brand("BYD")
                .model("Seal EV Premium")
                .year(2024)
                .licensePlate("BYD9E24")
                .category(VehicleCategory.LUXURY)
                .status(VehicleStatus.AVAILABLE)
                .dailyRate(new BigDecimal("450.00"))
                .mileage(4500)
                .color("Branco Perolizado")
                .seats(5)
                .transmission(Transmission.AUTOMATIC)
                .fuelType(FuelType.ELECTRIC)
                .description("Sedã esportivo 100% elétrico de altíssima performance, conforto de luxo e tecnologia de ponta.")
                .features("[\"100% Elétrico\", \"Aceleração 0-100 em 3.8s\", \"Tela rotativa de 15.6\\\"\", \"Som Dynaudio\"]")
                .imageUrls("[\"https://images.unsplash.com/photo-1617788138017-80ad40651399?auto=format&fit=crop&q=80&w=600\"]")
                .locationBranch(bAeroporto)
                .build();

        // 10. BMW M3 (Sports)
        Vehicle v10 = Vehicle.builder()
                .brand("BMW")
                .model("M3 Competition")
                .year(2023)
                .licensePlate("BMW1M03")
                .category(VehicleCategory.SPORTS)
                .status(VehicleStatus.AVAILABLE)
                .dailyRate(new BigDecimal("850.00"))
                .mileage(6000)
                .color("Azul Portimão")
                .seats(5)
                .transmission(Transmission.AUTOMATIC)
                .fuelType(FuelType.GASOLINE)
                .description("Esportivo de alto calibre com 510cv. Aceleração brutal e estabilidade cirúrgica para puristas.")
                .features("[\"Motor 510cv Bi-Turbo\", \"Câmbio M Steptronic\", \"Bancos concha em fibra de carbono\"]")
                .imageUrls("[\"https://images.unsplash.com/photo-1555215695-3004980ad54e?auto=format&fit=crop&q=80&w=600\"]")
                .locationBranch(bAeroporto)
                .build();

        // 11. Chevrolet Spin (Van)
        Vehicle v11 = Vehicle.builder()
                .brand("Chevrolet")
                .model("Spin Premier 1.8")
                .year(2022)
                .licensePlate("SPI1V70")
                .category(VehicleCategory.VAN)
                .status(VehicleStatus.AVAILABLE)
                .dailyRate(new BigDecimal("200.00"))
                .mileage(31000)
                .color("Prata")
                .seats(7) // 7 lugares
                .transmission(Transmission.AUTOMATIC)
                .fuelType(FuelType.FLEX)
                .description("O maior espaço para bagagens e a versatilidade de 7 lugares para toda a família viajar confortável.")
                .features("[\"7 Lugares\", \"Porta-malas gigante\", \"Câmbio automático\", \"Multimídia MyLink\"]")
                .imageUrls("[\"https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&q=80&w=600\"]")
                .locationBranch(bCentro)
                .build();

        // 12. Toyota Hilux (Truck)
        Vehicle v12 = Vehicle.builder()
                .brand("Toyota")
                .model("Hilux SRX 2.8 Diesel")
                .year(2023)
                .licensePlate("HIL1T80")
                .category(VehicleCategory.TRUCK)
                .status(VehicleStatus.AVAILABLE)
                .dailyRate(new BigDecimal("380.00"))
                .mileage(22000)
                .color("Branco Polar")
                .seats(5)
                .transmission(Transmission.AUTOMATIC)
                .fuelType(FuelType.DIESEL)
                .description("Picape forte a diesel com excelente capacidade de carga, luxo interno e tração 4x4 robusta.")
                .features("[\"Tração 4x4 c/ reduzida\", \"Motor Diesel 204cv\", \"Som JBL\", \"Alerta de colisão\"]")
                .imageUrls("[\"https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?auto=format&fit=crop&q=80&w=600\"]")
                .locationBranch(bAeroporto)
                .build();

        vehicleRepository.saveAll(Arrays.asList(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12));
    }

    private void seedRentals() {
        List<User> clients = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.CLIENT)
                .toList();

        List<Vehicle> vehicles = vehicleRepository.findAll();
        List<Branch> branches = branchRepository.findAll();

        if (clients.size() < 7 || vehicles.size() < 12 || branches.size() < 2) return;

        Branch bAeroporto = branches.get(0);
        Branch bCentro = branches.get(1);

        // ---- 6 ALUGUÉIS DE TESTE ----

        // 1. Aluguel Ativo (Onix alugado para João Silva)
        Rental r1 = Rental.builder()
                .user(clients.get(0)) // João Silva
                .vehicle(vehicles.get(0)) // Chevrolet Onix
                .pickupDate(LocalDateTime.now().minusDays(2))
                .returnDate(LocalDateTime.now().plusDays(3))
                .pickupLocation(bAeroporto)
                .returnLocation(bAeroporto)
                .status(RentalStatus.ACTIVE)
                .initialMileage(12000)
                .baseCost(new BigDecimal("550.00")) // 5 diárias * 110
                .totalCost(new BigDecimal("550.00"))
                .build();

        // 2. Aluguel Ativo (Corolla alugado para Maria Santos)
        Rental r2 = Rental.builder()
                .user(clients.get(1)) // Maria Santos
                .vehicle(vehicles.get(3)) // Toyota Corolla
                .pickupDate(LocalDateTime.now().minusDays(1))
                .returnDate(LocalDateTime.now().plusDays(4))
                .pickupLocation(bAeroporto)
                .returnLocation(bCentro)
                .status(RentalStatus.ACTIVE)
                .initialMileage(28000)
                .baseCost(new BigDecimal("1100.00")) // 5 diárias * 220
                .totalCost(new BigDecimal("1100.00"))
                .build();

        // 3. Aluguel Ativo (Compass alugado para Pedro Souza)
        Rental r3 = Rental.builder()
                .user(clients.get(2)) // Pedro Souza
                .vehicle(vehicles.get(6)) // Jeep Compass
                .pickupDate(LocalDateTime.now().minusDays(3))
                .returnDate(LocalDateTime.now().plusDays(2))
                .pickupLocation(bAeroporto)
                .returnLocation(bAeroporto)
                .status(RentalStatus.ACTIVE)
                .initialMileage(18500)
                .baseCost(new BigDecimal("1450.00")) // 5 diárias * 290
                .totalCost(new BigDecimal("1450.00"))
                .build();

        // 4. Aluguel Concluído sem atraso/danos (Argo alugado para Ana Oliveira)
        Rental r4 = Rental.builder()
                .user(clients.get(3)) // Ana Oliveira
                .vehicle(vehicles.get(1)) // Fiat Argo
                .pickupDate(LocalDateTime.now().minusDays(10))
                .returnDate(LocalDateTime.now().minusDays(5))
                .actualReturnDate(LocalDateTime.now().minusDays(5))
                .pickupLocation(bCentro)
                .returnLocation(bCentro)
                .status(RentalStatus.COMPLETED)
                .initialMileage(14500)
                .finalMileage(15000) // Rodou 500km
                .baseCost(new BigDecimal("600.00")) // 5 diárias * 120
                .totalCost(new BigDecimal("600.00"))
                .observations("Devolvido no prazo, veículo em perfeito estado de conservação.")
                .build();

        // 5. Aluguel Concluído COM ATRASO de 2 dias (HB20 alugado para Lucas Lima)
        // Multa = Diária * 1.5 * dias_atraso = 115 * 1.5 * 2 = 345.00
        Rental r5 = Rental.builder()
                .user(clients.get(4)) // Lucas Lima
                .vehicle(vehicles.get(2)) // Hyundai HB20
                .pickupDate(LocalDateTime.now().minusDays(12))
                .returnDate(LocalDateTime.now().minusDays(7)) // Deveria devolver há 7 dias
                .actualReturnDate(LocalDateTime.now().minusDays(5)) // Devolveu há 5 dias (2 dias atraso)
                .pickupLocation(bCentro)
                .returnLocation(bCentro)
                .status(RentalStatus.COMPLETED)
                .initialMileage(13800)
                .finalMileage(14200) // Rodou 400km
                .baseCost(new BigDecimal("575.00")) // 5 diárias * 115
                .lateFee(new BigDecimal("345.00")) // Multa de atraso aplicada
                .totalCost(new BigDecimal("920.00")) // 575 + 345
                .observations("Entregue com 2 dias de atraso. Multa gerada no fechamento.")
                .build();

        // 6. Aluguel Concluído COM DANOS no veículo (Civic alugado para Juliana Costa)
        // Cobrado taxa de danos de R$ 500
        Rental r6 = Rental.builder()
                .user(clients.get(5)) // Juliana Costa
                .vehicle(vehicles.get(4)) // Honda Civic
                .pickupDate(LocalDateTime.now().minusDays(8))
                .returnDate(LocalDateTime.now().minusDays(4))
                .actualReturnDate(LocalDateTime.now().minusDays(4))
                .pickupLocation(bCentro)
                .returnLocation(bCentro)
                .status(RentalStatus.COMPLETED)
                .initialMileage(34600)
                .finalMileage(35000) // Rodou 400km
                .baseCost(new BigDecimal("920.00")) // 4 diárias * 230
                .additionalsCost(new BigDecimal("500.00")) // Custos extras (Dano cobrado)
                .totalCost(new BigDecimal("1420.00")) // 920 + 500
                .observations("DANO REGISTRADO: Parachoque traseiro trincado após colisão leve. Cobrado franquia de R$ 500.")
                .build();

        rentalRepository.saveAll(Arrays.asList(r1, r2, r3, r4, r5, r6));
    }
}
