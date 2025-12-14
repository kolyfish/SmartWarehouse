# 🚀 完整設定指南

## 📋 目錄

1. [環境需求](#環境需求)
2. [Spring Boot 後端設定](#spring-boot-後端設定)
3. [Python 測試平台設定](#python-測試平台設定)
4. [JMeter 壓力測試設定](#jmeter-壓力測試設定)
5. [GitHub Actions CI/CD 設定](#github-actions-cicd-設定)
6. [常見問題](#常見問題)

---

## 環境需求

### Java 開發環境

- **Java 17+**
- **Maven 3.6+**

檢查版本：
```bash
java -version
mvn -version
```

### Python 開發環境

- **Python 3.11+**
- **pip**

檢查版本：
```bash
python3 --version
pip3 --version
```

### 可選工具

- **JMeter 5.6+**（壓力測試）
- **Git**（版本控制）

---

## Spring Boot 後端設定

### 1. 安裝 Maven 依賴

```bash
cd backend
mvn clean install
```

### 2. 啟動服務

```bash
mvn spring-boot:run
```

**預設配置**：
- 端口：8080
- 資料庫：H2 內存資料庫
- 自動建立表結構

### 3. 驗證服務

```bash
# 健康檢查
curl http://localhost:8080/api/beverages/statistics

# 應該看到 JSON 回應
```

### 4. 查看 H2 Console

1. 開啟瀏覽器：http://localhost:8080/h2-console
2. 設定：
   - JDBC URL: `jdbc:h2:mem:beveragewarehouse`
   - Username: `sa`
   - Password: （空白）
3. 點擊 Connect

### 5. 執行 TDD 測試

```bash
# 執行所有測試
mvn test

# 執行高併發測試
mvn test -Dtest=BeverageServiceConcurrencyTest
```

---

## Python 測試平台設定

### 1. 建立虛擬環境（建議）

```bash
python3 -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
```

### 2. 安裝依賴

```bash
pip install -r requirements.txt
playwright install chromium
```

### 3. 執行 Playwright 測試

```bash
pytest tests/ -v
```

### 4. 啟動 FastAPI 服務（可選）

```bash
cd api
uvicorn main:app --reload
```

API 文件：http://localhost:8000/docs

---

## JMeter 壓力測試設定

### 1. 安裝 JMeter

#### macOS
```bash
brew install jmeter
```

#### Windows/Linux
下載：https://jmeter.apache.org/download_jmeter.cgi

### 2. 啟動 Spring Boot 服務

確保服務運行在 `http://localhost:8080`

### 3. 執行測試

#### 方法 1：GUI 模式

```bash
jmeter -t jmeter/SmartWarehouse_Concurrency_Test.jmx
```

在 GUI 中：
1. 點擊 "Run" > "Start"
2. 查看 "View Results Tree" 和 "Summary Report"

#### 方法 2：命令列模式（無 GUI）

```bash
jmeter -n -t jmeter/SmartWarehouse_Concurrency_Test.jmx \
  -l jmeter/results.jtl \
  -e -o jmeter/report
```

查看報告：
```bash
open jmeter/report/index.html  # macOS
# 或直接在瀏覽器開啟 jmeter/report/index.html
```

### 4. 調整測試參數

在 JMeter GUI 中修改 "User Defined Variables"：

- `THREAD_COUNT`: 執行緒數（預設 100）
- `QUANTITY_PER_THREAD`: 每個執行緒出庫數量（預設 5）
- `BASE_URL`: API 基礎 URL

### 5. 驗證測試結果

執行測試後，手動查詢 API：

```bash
# 查詢庫存統計
curl http://localhost:8080/api/beverages/statistics

# 查詢所有飲料
curl http://localhost:8080/api/beverages
```

**驗收標準**：
- ✅ 庫存扣減數量 = 成功請求數 × 每單數量
- ✅ 最終庫存 >= 0（無負庫存）
- ✅ 初始庫存 - 成功出庫總數 = 最終庫存

詳細說明請參考：[jmeter/README.md](./jmeter/README.md)

---

## GitHub Actions CI/CD 設定

### 1. GCP Firestore 設定（可選，用於測試結果存儲）

1. 前往 [GCP Console](https://console.cloud.google.com/)
2. 建立新專案
3. 啟用 Firestore API
4. 建立服務帳號並下載 JSON 金鑰

### 2. 設定 GitHub Secrets

在 GitHub 專案設定中，加入以下 Secrets：

- `GCP_SA_KEY`: 服務帳號 JSON 內容
- `FIRESTORE_PROJECT_ID`: GCP 專案 ID

### 3. 推送程式碼

```bash
git add .
git commit -m "feat: 初始化專案"
git push origin main
```

GitHub Actions 會自動執行測試。

---

## 常見問題

### Q1: Maven 編譯失敗

**錯誤**：`Could not resolve dependencies`

**解決**：
```bash
# 清理並重新下載依賴
mvn clean
mvn dependency:resolve
```

### Q2: 測試失敗 - 資料庫連線錯誤

**錯誤**：`Unable to acquire JDBC Connection`

**解決**：
1. 確認 H2 資料庫依賴已加入 `pom.xml`
2. 檢查 `application.properties` 中的資料庫設定
3. 確認服務正常啟動

### Q3: JMeter 無法連線

**錯誤**：`Connection refused`

**解決**：
1. 確認 Spring Boot 服務已啟動
2. 檢查端口是否正確（預設 8080）
3. 測試連線：`curl http://localhost:8080/api/beverages/statistics`

### Q4: 高併發測試出現負庫存

**原因**：悲觀鎖未正確實作

**檢查**：
1. 確認 `BeverageRepository` 中的方法使用 `@Lock(LockModeType.PESSIMISTIC_WRITE)`
2. 確認 `@Transactional` 註解正確
3. 檢查資料庫是否支援悲觀鎖（H2 支援）

### Q5: JMeter 測試結果不一致

**可能原因**：
- 執行緒數過多，超過系統負載
- 服務未完全啟動
- 資料庫連線池不足

**解決**：
1. 降低執行緒數（從 100 降到 50）
2. 增加 JVM 記憶體：`export JAVA_OPTS="-Xmx2g"`
3. 檢查 Spring Boot 日誌中的錯誤

### Q6: Playwright 測試失敗

**錯誤**：`Browser not found`

**解決**：
```bash
playwright install chromium
```

### Q7: 端口被占用

**錯誤**：`Port 8080 is already in use`

**解決**：
```bash
# 查看占用端口的程序
lsof -i :8080

# 終止程序
kill -9 <PID>

# 或修改端口（在 application.properties）
server.port=8081
```

---

## 📚 相關文件

- [快速開始指南](./QUICKSTART.md)
- [API 使用範例](./API_EXAMPLES.md)
- [JMeter 測試說明](./jmeter/README.md)
- [Git 設定指南](./GIT_SETUP.md)

---

## 🎓 學習資源

- [Spring Boot 官方文件](https://spring.io/projects/spring-boot)
- [Spring Data JPA 鎖機制](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#locking)
- [JMeter 官方文件](https://jmeter.apache.org/usermanual/)
- [Playwright 文件](https://playwright.dev/python/)

---

**設定完成！開始開發吧！** 🚀
