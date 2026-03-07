# 多架构支持：maven、eclipse-temurin 官方镜像均支持 linux/amd64、linux/arm64
# 验证：docker pull --platform linux/arm64 maven:3.9-eclipse-temurin-17
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

# 验证：docker pull --platform linux/arm64 eclipse-temurin:17-jre
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
