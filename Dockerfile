# -----------------------------------------------------------------------
# Build multi-stage: compila com Maven+JDK e roda com um JRE enxuto.
# O Render usa este Dockerfile para construir e iniciar a aplicação.
# -----------------------------------------------------------------------

# --- Etapa 1: build ---
# Imagem com Maven e JDK 21 para compilar e empacotar o jar.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Primeiro copia só o pom.xml e baixa as dependências.
# Assim o cache de dependências é reaproveitado quando só o código muda.
COPY pom.xml .
RUN mvn -q dependency:go-offline

# Agora copia o código e empacota (pula os testes no build de deploy).
COPY src ./src
RUN mvn -q clean package -DskipTests

# --- Etapa 2: runtime ---
# Imagem só com o JRE 21 (bem menor) para executar o jar.
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copia o jar gerado na etapa de build.
COPY --from=build /app/target/*.jar app.jar

# A aplicação lê a porta da variável de ambiente PORT (injetada pelo Render).
EXPOSE 8080

# Inicia a aplicação já com o perfil "prod" ativo (PostgreSQL).
# Fixar aqui garante que o deploy nunca caia no H2 por engano, mesmo que
# a variável de ambiente SPRING_PROFILES_ACTIVE não esteja definida.
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
