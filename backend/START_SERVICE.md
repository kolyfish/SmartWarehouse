# 🚀 Spring Boot 服務啟動指南

## 📋 快速啟動

### 方法 1：使用 Maven（推薦）

```bash
cd backend
mvn spring-boot:run
```

### 方法 2：使用已編譯的 JAR 檔案

```bash
cd backend
mvn clean package
java -jar target/smart-warehouse-1.0.0.jar
```

### 方法 3：使用 IDE（IntelliJ IDEA / Eclipse）

1. 開啟專案
2. 找到 `BeverageWarehouseApplication.java`
3. 右鍵點擊 → Run 'BeverageWarehouseApplication'

---

## ✅ 驗證服務是否運行

### 1. 檢查端口是否被占用

```bash
# macOS / Linux
lsof -i :8080

# Windows
netstat -ano | findstr :8080
```

### 2. 測試 API 端點

```bash
# 測試健康檢查（庫存統計）
curl http://localhost:8080/api/beverages/statistics

# 應該看到 JSON 回應：
# {"totalItems":0,"totalQuantity":0,"expiredQuantity":0,"expiringSoonQuantity":0}
```

### 3. 查看 H2 Console

1. 開啟瀏覽器：http://localhost:8080/h2-console
2. 設定：
   - **JDBC URL**: `jdbc:h2:mem:beveragewarehouse`
   - **Username**: `sa`
   - **Password**: （空白）
3. 點擊 Connect

---

## 🛑 停止服務

### 方法 1：在終端中按 Ctrl+C

如果服務在終端中運行，直接按 `Ctrl+C` 停止。

### 方法 2：終止進程

```bash
# 找到進程 ID
lsof -i :8080

# 終止進程（替換 <PID> 為實際的進程 ID）
kill <PID>

# 或強制終止
kill -9 <PID>
```

---

## 🔧 常見問題

### 問題 1：端口 8080 已被占用

**錯誤訊息**：
```
Port 8080 is already in use
```

**解決方案**：

#### 選項 A：終止占用端口的程序

```bash
# 查看占用端口的程序
lsof -i :8080

# 終止程序
kill <PID>
```

#### 選項 B：修改端口

編輯 `backend/src/main/resources/application.properties`：

```properties
server.port=8081
```

然後使用新端口訪問：`http://localhost:8081`

### 問題 2：Maven 找不到

**錯誤訊息**：
```
mvn: command not found
```

**解決方案**：

```bash
# macOS
brew install maven

# 或下載：https://maven.apache.org/download.cgi
```

### 問題 3：Java 版本不正確

**錯誤訊息**：
```
Unsupported class file major version XX
```

**解決方案**：

確保使用 Java 17：

```bash
# 檢查 Java 版本
java -version

# 應該顯示：openjdk version "17.x.x"

# macOS 安裝 Java 17
brew install openjdk@17

# 設定 JAVA_HOME
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
```

### 問題 4：編譯失敗

**錯誤訊息**：
```
Could not resolve dependencies
```

**解決方案**：

```bash
cd backend

# 清理並重新下載依賴
mvn clean
mvn dependency:resolve

# 重新編譯
mvn clean install
```

### 問題 5：資料庫連線錯誤

**錯誤訊息**：
```
Unable to acquire JDBC Connection
```

**解決方案**：

1. 確認 H2 依賴已加入 `pom.xml`
2. 檢查 `application.properties` 中的資料庫設定
3. 確認服務正常啟動

---

## 📊 啟動日誌說明

### 正常啟動的標誌

當你看到以下訊息時，表示服務已成功啟動：

```
Started BeverageWarehouseApplication in X.XXX seconds
```

### 常見啟動訊息

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.0)

... (其他日誌訊息)

Started BeverageWarehouseApplication in 2.345 seconds
```

---

## 🎯 啟動後可以做的事情

### 1. 測試 API

```bash
# 查看庫存統計
curl http://localhost:8080/api/beverages/statistics

# 查看所有庫存
curl http://localhost:8080/api/beverages

# 入庫測試
curl -X POST http://localhost:8080/api/beverages/stock-in \
  -H "Content-Type: application/json" \
  -d '{
    "name": "礦泉水",
    "quantity": 100,
    "productionDate": "2024-01-01",
    "expiryDate": "2025-01-01"
  }'
```

### 2. 開啟前端介面

```bash
# 方法 1：直接開啟 HTML
open frontend/index.html

# 方法 2：使用 Python HTTP 伺服器
cd frontend
python3 -m http.server 8000
# 然後在瀏覽器開啟 http://localhost:8000/index.html
```

### 3. 執行測試

```bash
cd backend

# 執行所有測試
mvn test

# 執行特定測試
mvn test -Dtest=BeverageServiceConcurrencyTest
```

---

## 📝 環境變數設定（可選）

如果需要自訂設定，可以設定環境變數：

```bash
# 設定端口
export SERVER_PORT=8081

# 設定 Java 記憶體
export JAVA_OPTS="-Xmx2g -Xms1g"

# 然後啟動服務
mvn spring-boot:run
```

---

## 🔍 除錯模式

如果需要更詳細的日誌：

編輯 `application.properties`：

```properties
# 開啟除錯日誌
logging.level.root=DEBUG
logging.level.com.beveragewarehouse=DEBUG
```

---

## 📚 相關文件

- [完整文件](../COMPLETE_DOCUMENTATION.md)
- [快速開始](../QUICKSTART.md)
- [API 使用範例](../API_EXAMPLES.md)
- [前端使用說明](../frontend/README.md)

---

**服務啟動成功後，就可以開始使用 SmartWarehouse 系統了！** 🎉


