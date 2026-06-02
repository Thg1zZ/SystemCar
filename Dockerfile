# Estágio de Compilação (Build Stage)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copia apenas o pom.xml primeiro para cachear as dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia os arquivos de código-fonte e compila
COPY src ./src
RUN mvn clean package -DskipTests

# Estágio de Execução (Runtime Stage)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia o arquivo .jar compilado do estágio de build
COPY --from=build /app/target/rodalivre-backend-0.0.1-SNAPSHOT.jar app.jar

# Define a porta padrão do serviço
EXPOSE 8080

# Inicia o servidor Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
