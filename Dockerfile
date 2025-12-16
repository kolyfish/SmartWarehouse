# 多階段構建 Dockerfile for Spring Boot Application
# Stage 1: 構建階段
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# 複製 pom.xml 並下載依賴（利用 Docker 快取層）
COPY backend/pom.xml .
RUN mvn dependency:go-offline -B

# 複製源碼並構建
COPY backend/src ./src
COPY backend/pom.xml .
RUN mvn clean package -DskipTests

# Stage 2: 運行階段
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# 安裝 wget 用於健康檢查
RUN apt-get update && apt-get install -y wget && rm -rf /var/lib/apt/lists/*

# 從構建階段複製 JAR 檔案
COPY --from=build /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 健康檢查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/beverages/statistics || exit 1

# 啟動應用
ENTRYPOINT ["java", "-jar", "app.jar"]

