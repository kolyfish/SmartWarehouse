# SmartWarehouse 完整文件

智慧倉庫系統 - 完整技術文件與使用指南

## 目錄

1. [專案簡介](#專案簡介)
2. [快速開始](#快速開始)
3. [完整設定指南](#完整設定指南)
4. [API 使用範例](#api-使用範例)
5. [V 模型與 ISTQB 測試理論](#v-模型與-istqb-測試理論)
6. [過期商品處理](#過期商品處理)
7. [JMeter 壓力測試](#jmeter-壓力測試)
8. [前端介面使用](#前端介面使用)
9. [服務啟動與管理](#服務啟動與管理)
10. [Docker 容器化部署](#docker-容器化部署推薦)
11. [Git/GitHub 設定](#gitgithub-設定)
12. [CI/CD 自動化](#cicd-自動化)
13. [技術棧與架構](#技術棧與架構)
14. [面試應用指南](#面試應用指南)
15. [履歷截圖指南](#履歷截圖指南)

---

# 1. 專案簡介

## 專案概述

SmartWarehouse 是一個完全免費的全端作品集專案，整合了：

- Spring Boot - Java 後端框架（飲料庫存管理）
- H2 Database - 內存 SQL 資料庫（完全免費）
- Docker & Docker Compose - 容器化部署（一鍵啟動應用和資料庫）
- 悲觀鎖機制 - 確保高併發下的資料一致性
- JMeter - 壓力測試工具（驗證資料一致性）
- TDD 測試 - 測試驅動開發（高併發測試案例）
- ISTQB 測試理論 - V 模型完整應用（單元→整合→系統→驗收）
- Playwright - Python 自動化測試框架
- FastAPI - Python 輕量級後端框架
- Firestore - GCP NoSQL 資料庫（免費層）
- GitHub Actions - 免費 CI/CD（每月 2000 分鐘）

## 核心功能

### 1. 飲料倉庫管理系統（Spring Boot）

#### 基本功能
- 入庫管理 - 新增飲料庫存
- 出庫管理 - 按照 FIFO 原則出庫（優先出庫最早過期的）
- CRUD 操作 - 完整的增刪查改功能
- 過期檢查 - 自動檢查飲料是否過期
- 即將過期提醒 - 7 天內過期的飲料提醒
- 庫存統計 - 總庫存、過期數量、即將過期數量

#### 高併發資料一致性保證
- 悲觀鎖機制 - 使用 `@Lock(LockModeType.PESSIMISTIC_WRITE)` 確保資料一致性
- 事務管理 - `@Transactional` 保證原子性操作
- 負庫存防護 - 防止高併發下出現負庫存
- 精確匹配驗證 - 庫存扣減數量與訂單成功數精確匹配

### 2. 測試自動化平台（Python）
- 自動化測試執行 - GitHub Actions 觸發 Playwright 測試
- 測試結果存儲 - 結果自動寫入 Firestore
- 測試報告 API - FastAPI 提供測試結果查詢

### 3. ISTQB 測試理論實踐（V 模型完整應用）
- V 模型第一層：單元測試 - 等價類劃分、邊界值分析、決策表測試
- V 模型第二層：整合測試 - 狀態轉換測試（Service + Repository）
- V 模型第三層：系統測試 - 用例測試（基本流、替代流、異常流）、高併發測試（效能、壓力）
- V 模型第四層：驗收測試 - 驗收標準驗證（AC1、AC2、AC3）
- 測試管理 - 風險導向測試、測試計劃、測試監控

## 專案結構

```
.
├── backend/                    # Spring Boot 後端
│   ├── src/main/java/com/beveragewarehouse/
│   │   ├── model/              # 實體類別（Beverage）
│   │   ├── repository/         # 資料庫操作（JPA Repository + 悲觀鎖）
│   │   ├── service/            # 業務邏輯層（事務管理）
│   │   ├── controller/        # REST API Controller
│   │   └── dto/                # 資料傳輸物件
│   ├── src/test/java/          # ISTQB 測試案例（V 模型完整應用）
│   │   ├── BeverageServiceVModelTest.java           # V 模型完整測試
│   │   ├── BeverageServiceConcurrencyTest.java      # 高併發測試（系統/驗收）
│   │   ├── BeverageServiceEquivalencePartitioningTest.java  # 等價類劃分（單元）
│   │   ├── BeverageServiceBoundaryValueTest.java    # 邊界值分析（單元）
│   │   ├── BeverageServiceDecisionTableTest.java    # 決策表測試（單元）
│   │   ├── BeverageStateTransitionTest.java         # 狀態轉換測試（整合）
│   │   ├── BeverageUseCaseTest.java                 # 用例測試（系統）
│   │   └── BeverageExpiredHandlingTest.java          # 過期商品處理測試
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── data.sql
│   └── pom.xml
│
├── jmeter/                      # JMeter 壓力測試
│   ├── SmartWarehouse_Concurrency_Test.jmx
│   └── README.md
│
├── tests/                       # Playwright 測試腳本
│   ├── test_example.py
│   └── conftest.py
│
├── api/                         # FastAPI 後端
│   ├── main.py
│   └── firestore_client.py
│
├── .github/workflows/
│   └── ci.yml                   # GitHub Actions CI/CD
│
├── requirements.txt            # Python 依賴
├── QUICK_TEST.sh               # 快速 API 測試腳本
├── Dockerfile                  # Docker 映像構建檔案
├── docker-compose.yml          # Docker Compose 配置（應用 + 資料庫）
├── .dockerignore               # Docker 構建忽略檔案
└── README.md
```

## 成本說明

完全免費，所有服務都在免費層範圍內：

| 服務 | 免費額度 | 說明 |
|------|---------|------|
| H2 Database | 無限制 | 內存資料庫，完全免費 |
| JMeter | 無限制 | 開源工具，完全免費 |
| GitHub Actions | 每月 2000 分鐘 | 個人帳號免費 |
| Firestore | 每天 50K 讀取、20K 寫入 | GCP 免費層 |
| GCP Logging | 50 GB/月 | 免費 |

---

# 2. 快速開始

## 5 分鐘快速開始

### 步驟 1：啟動 Spring Boot 後端

```bash
cd backend
mvn spring-boot:run
```

等待服務啟動（看到 "Started BeverageWarehouseApplication" 訊息）

驗證服務運行（兩種方式）：

**方式 1：使用命令行（curl）**
```bash
curl http://localhost:8080/api/beverages/statistics
```
應該看到 JSON 回應：`{"totalItems":0,"totalQuantity":0,"expiredQuantity":0,"expiringSoonQuantity":0}`

**方式 2：使用前端介面（推薦）**
1. 在瀏覽器中開啟 `frontend/index.html`
2. 或使用簡單 HTTP 伺服器：
   ```bash
   cd frontend
   python3 -m http.server 8000
   # 然後在瀏覽器開啟 http://localhost:8000/index.html
   ```
3. 前端會自動載入統計資料，可以看到圖形化介面

### 步驟 2：執行快速 API 測試

```bash
# 回到專案根目錄
cd ..

# 執行快速測試腳本
./QUICK_TEST.sh
```

這個腳本會：
1. 入庫 100 瓶礦泉水
2. 入庫 50 瓶礦泉水（不同日期）
3. 查詢所有庫存
4. 查看統計資料
5. 出庫 30 瓶（系統自動選擇最早過期的）
6. 再次查詢庫存
7. 檢查即將過期的飲料

### 步驟 3：執行 TDD 高併發測試

```bash
cd backend
mvn test -Dtest=BeverageServiceConcurrencyTest
```

測試內容：
- 模擬 100 個執行緒同時出庫
- 驗證悲觀鎖機制
- 驗證資料一致性（無負庫存）

預期結果：
```
高併發測試通過
庫存扣減精確匹配
無負庫存
```

### 步驟 4：執行 JMeter 壓力測試（可選）

#### 前置需求

1. 安裝 JMeter：
   ```bash
   # macOS
   brew install jmeter
   
   # 或下載：https://jmeter.apache.org/download_jmeter.cgi
   ```

2. 啟動 Spring Boot 服務（如果還沒啟動）

#### 執行測試

```bash
# 使用 GUI 模式
jmeter -t jmeter/SmartWarehouse_Concurrency_Test.jmx

# 或使用命令列（無 GUI）
jmeter -n -t jmeter/SmartWarehouse_Concurrency_Test.jmx \
  -l jmeter/results.jtl \
  -e -o jmeter/report
```

---

# 3. 完整設定指南

## 環境需求

### Java 開發環境

- Java 17+
- Maven 3.6+

檢查版本：
```bash
java -version
mvn -version
```

### Python 開發環境

- Python 3.11+
- pip

檢查版本：
```bash
python3 --version
pip3 --version
```

### 可選工具

- JMeter 5.6+（壓力測試）
- Git（版本控制）

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

預設配置：
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

## 常見問題

### Q1: Maven 編譯失敗

錯誤：`Could not resolve dependencies`

解決：
```bash
# 清理並重新下載依賴
mvn clean
mvn dependency:resolve
```

### Q2: 測試失敗 - 資料庫連線錯誤

錯誤：`Unable to acquire JDBC Connection`

解決：
1. 確認 H2 資料庫依賴已加入 `pom.xml`
2. 檢查 `application.properties` 中的資料庫設定
3. 確認服務正常啟動

### Q3: 端口被占用

錯誤：`Port 8080 is already in use`

解決：
```bash
# 查看占用端口的程序
lsof -i :8080

# 終止程序
kill -9 <PID>

# 或修改端口（在 application.properties）
server.port=8081
```

---

# 4. API 使用範例

## SmartWarehouse API 完整範例

### 基礎設定

```bash
# API 基礎 URL
BASE_URL=http://localhost:8080/api/beverages
```

## 1. 入庫管理（Stock In）

**業務規則：一次入庫數量限制為 1-100 瓶**

### 入庫 100 瓶礦泉水（最大值）

```bash
curl -X POST http://localhost:8080/api/beverages/stock-in \
  -H "Content-Type: application/json" \
  -d '{
    "name": "礦泉水",
    "quantity": 100,
    "productionDate": "2024-01-01",
    "expiryDate": "2025-01-01"
  }'
```

回應範例：
```json
{
  "id": 1,
  "name": "礦泉水",
  "quantity": 100,
  "productionDate": "2024-01-01",
  "expiryDate": "2025-01-01",
  "createdAt": "2024-12-20T10:00:00",
  "updatedAt": "2024-12-20T10:00:00",
  "expired": false,
  "daysUntilExpiry": 346,
  "expiringSoon": false,
  "status": "NORMAL"
}
```

## 2. 查詢所有飲料

```bash
curl http://localhost:8080/api/beverages
```

## 3. 出庫管理（Stock Out）

### 出庫 50 瓶礦泉水（系統會自動選擇最早過期的）

```bash
curl -X POST http://localhost:8080/api/beverages/stock-out \
  -H "Content-Type: application/json" \
  -d '{
    "name": "礦泉水",
    "quantity": 50
  }'
```

回應範例：
```json
{
  "message": "成功出庫 50 瓶 礦泉水"
}
```

說明：
- 系統會按照 FIFO（先進先出）原則
- 優先出庫最早過期的飲料
- 如果第一批數量不足，會自動從下一批補足

## 4. 查詢已過期的飲料

```bash
curl http://localhost:8080/api/beverages/expired
```

## 5. 查詢即將過期的飲料（7 天內）

```bash
curl http://localhost:8080/api/beverages/expiring-soon
```

## 6. 取得庫存統計

```bash
curl http://localhost:8080/api/beverages/statistics
```

回應範例：
```json
{
  "totalItems": 3,
  "totalQuantity": 350,
  "expiredQuantity": 0,
  "expiringSoonQuantity": 0
}
```

## 7. 過期商品處理 API（業界標準）

### 自動隔離過期商品

```bash
curl -X POST http://localhost:8080/api/beverages/quarantine-expired
```

回應：
```json
{
  "message": "成功隔離 5 個過期商品",
  "quarantinedCount": 5
}
```

### 查詢隔離區商品

```bash
curl http://localhost:8080/api/beverages/quarantined
```

### 報廢商品

```bash
curl -X POST http://localhost:8080/api/beverages/1/dispose \
  -H "Content-Type: application/json" \
  -d '{
    "reason": "過期報廢，已超過有效期限"
  }'
```

### 查詢已報廢商品

```bash
curl http://localhost:8080/api/beverages/disposed
```

## API 端點總覽

| 方法 | 路徑 | 功能 | 鎖機制 |
|------|------|------|--------|
| GET | `/api/beverages` | 取得所有飲料 | - |
| GET | `/api/beverages/{id}` | 根據 ID 取得飲料 | - |
| POST | `/api/beverages/stock-in` | 入庫飲料 | 一次入庫數量限制：1-100 瓶 |
| POST | `/api/beverages/stock-out` | 出庫飲料 | 悲觀鎖 |
| PUT | `/api/beverages/{id}` | 更新飲料資訊 | - |
| DELETE | `/api/beverages/{id}` | 刪除飲料 | - |
| GET | `/api/beverages/expired` | 取得已過期的飲料 | - |
| GET | `/api/beverages/expiring-soon` | 取得即將過期的飲料 | - |
| GET | `/api/beverages/statistics` | 取得庫存統計 | - |
| POST | `/api/beverages/quarantine-expired` | 自動隔離過期商品 | - |
| GET | `/api/beverages/quarantined` | 查詢隔離區商品 | - |
| POST | `/api/beverages/{id}/dispose` | 報廢商品 | - |
| GET | `/api/beverages/disposed` | 查詢已報廢商品 | - |

---

# 5. V 模型與 ISTQB 測試理論

## V 模型在 SmartWarehouse 專案中的完整應用

SmartWarehouse 專案展示了 V 模型（V-Model）和 ISTQB 測試理論在實際專案中的系統化應用。

## V 模型四層級完整對照

### 第一層：單元測試 (Unit Testing)

責任人：RD / SDET  
測試對象：最小的可測試單元（Class 或 Method）

| ISTQB 技術 | 測試檔案 | 測試案例數 | 關鍵測試點 |
|-----------|---------|-----------|-----------|
| 等價類劃分 | `BeverageServiceEquivalencePartitioningTest` | 10+ | 有效/無效輸入分類 |
| 邊界值分析 | `BeverageServiceBoundaryValueTest` | 10+ | 0, 1, 2, 9999, 10000, 10001 |
| 決策表測試 | `BeverageServiceDecisionTableTest` | 5+ | 4 個業務規則組合 |

實作範例：
```java
// 等價類劃分：有效輸入
request.setQuantity(500); // 代表 1-10000 之間的所有整數

// 邊界值分析：下邊界
request.setQuantity(0);  // 無效
request.setQuantity(1);  // 有效（最小值）
request.setQuantity(2);  // 有效（最小值+1）

// 決策表：規則 1
// 庫存 > 0, 請求 <= 庫存, 未過期 → 成功出庫
```

### 第二層：整合測試 (Integration Testing)

責任人：SDET  
測試對象：多個單元組裝後的介面和互動

| ISTQB 技術 | 測試檔案 | 測試案例數 | 關鍵測試點 |
|-----------|---------|-----------|-----------|
| 狀態轉換測試 | `BeverageStateTransitionTest` | 6+ | 4 個狀態，6 個轉換路徑 |

狀態轉換圖：
```
[入庫] → [在庫] → [出庫] → [已出庫]
           ↓
        [即將過期] → [已過期]
```

### 第三層：系統測試 (System Testing)

責任人：SDET / QA  
測試對象：完整的系統，黑箱測試

| ISTQB 技術 | 測試檔案 | 測試案例數 | 關鍵測試點 |
|-----------|---------|-----------|-----------|
| 用例測試 | `BeverageUseCaseTest` | 5+ | 基本流、替代流、異常流 |
| 高併發測試 | `BeverageServiceConcurrencyTest` | 2+ | 100 執行緒，資料一致性 |

用例測試策略：

1. 基本流 (Happy Path)
   ```java
   // 倉庫管理員順利完成入庫和出庫
   testUseCase1_StockIn_NewBeverage()
   ```

2. 替代流 (Alternative Path)
   ```java
   // 庫存不足，系統提示並拒絕
   testUseCase_StockOut_InsufficientStock()
   ```

3. 異常流 (Exception Path)
   ```java
   // 資料庫連線失敗、網路斷線
   // （需要模擬異常環境）
   ```

### 第四層：驗收測試 (Acceptance Testing)

責任人：客戶 / 產品經理  
測試對象：業務需求、驗收標準

| 驗收標準 | 測試檔案 | 驗證內容 |
|---------|---------|---------|
| AC1 | `BeverageServiceConcurrencyTest`<br>`BeverageServiceVModelTest` | 庫存扣減數量 = 訂單成功數 × 每單數量 |
| AC2 | `BeverageServiceConcurrencyTest`<br>`BeverageServiceVModelTest` | 不能出現負庫存 |
| AC3 | `BeverageServiceConcurrencyTest`<br>`BeverageServiceVModelTest` | 初始庫存 - 成功出庫總數 = 最終庫存 |

## V 模型完整對照表

| V 模型層級 | 開發階段 | 測試活動 | 測試檔案 | ISTQB 技術 |
|-----------|---------|---------|---------|-----------|
| 單元測試 | 詳細設計 | 單元測試 | `BeverageServiceEquivalencePartitioningTest`<br>`BeverageServiceBoundaryValueTest`<br>`BeverageServiceDecisionTableTest` | 等價類劃分<br>邊界值分析<br>決策表 |
| 整合測試 | 架構設計 | 整合測試 | `BeverageStateTransitionTest` | 狀態轉換測試 |
| 系統測試 | 系統設計 | 系統測試 | `BeverageUseCaseTest`<br>`BeverageServiceConcurrencyTest` | 用例測試<br>高併發測試 |
| 驗收測試 | 需求分析 | 驗收測試 | `BeverageServiceConcurrencyTest`<br>`BeverageServiceVModelTest` | 驗收標準驗證 |

## 測試覆蓋率統計

| 測試級別 | 測試檔案數 | 測試案例總數（估算） |
|---------|-----------|-------------------|
| 單元測試 | 3 | 30+ |
| 整合測試 | 1 | 6+ |
| 系統測試 | 2 | 7+ |
| 驗收測試 | 2 | 3+ |
| 總計 | 8 | 46+ |

---

# 6. 過期商品處理

## 業界標準流程

SmartWarehouse 專案已實作業界標準的過期商品處理機制，符合真實 WMS 系統的做法。

## 核心功能

### 1. 狀態管理（Status Management）

商品有三種狀態：

| 狀態 | 說明 | 是否可以出庫 |
|------|------|-------------|
| NORMAL | 正常商品 | 可以 |
| QUARANTINED | 隔離中（已過期但尚未處理） | 不可以 |
| DISPOSED | 已報廢 | 不可以 |

### 2. 自動隔離機制

- 系統自動檢測過期商品
- 自動將狀態改為 `QUARANTINED`
- 防止過期商品誤出庫

### 3. 報廢流程

- 只能報廢隔離區中的商品
- 記錄報廢原因和時間
- 狀態改為 `DISPOSED`

### 4. 出庫保護

- 出庫時只查詢 `NORMAL` 狀態的商品
- 過期商品（`QUARANTINED`）不能出庫
- 已報廢商品（`DISPOSED`）不能出庫

## 處理流程

### 流程圖

```
[入庫] → [NORMAL] → [出庫] → [已出庫]
           │
           ├─→ [時間推移] → [過期] → [自動隔離] → [QUARANTINED]
           │                                              │
           │                                              ↓
           │                                        [報廢審批] → [DISPOSED]
           │
           └─→ [即將過期提醒]
```

### 詳細步驟

#### 步驟 1：自動隔離過期商品

```bash
# API 呼叫
POST /api/beverages/quarantine-expired

# 回應
{
  "message": "成功隔離 5 個過期商品",
  "quarantinedCount": 5
}
```

系統行為：
1. 查詢所有過期商品（`expiryDate < today`）
2. 將狀態為 `NORMAL` 的商品改為 `QUARANTINED`
3. 返回隔離數量

通常由定時任務（Scheduler）每日執行

#### 步驟 2：查看隔離區商品

```bash
# API 呼叫
GET /api/beverages/quarantined
```

#### 步驟 3：報廢商品

```bash
# API 呼叫
POST /api/beverages/{id}/dispose
Content-Type: application/json

{
  "reason": "過期報廢，已超過有效期限"
}
```

#### 步驟 4：查看已報廢商品

```bash
# API 呼叫
GET /api/beverages/disposed
```

## 出庫保護機制

系統在出庫時會自動過濾：

```java
// Repository 查詢（只查詢 NORMAL 狀態的商品）
@Query("SELECT b FROM Beverage b WHERE b.name = :name AND b.quantity > 0 AND b.expiryDate >= :today AND b.status = 'NORMAL' ORDER BY b.expiryDate ASC")
List<Beverage> findAvailableBeveragesByNameOrderByExpiryWithLock(...);
```

保護機制：
- 只查詢 `NORMAL` 狀態的商品
- 只查詢未過期的商品（`expiryDate >= today`）
- 過期商品（`QUARANTINED`）不會被查詢到
- 已報廢商品（`DISPOSED`）不會被查詢到

## 測試案例

`BeverageExpiredHandlingTest.java` 包含完整的測試案例：

1. 自動隔離過期商品
2. 報廢隔離區中的商品
3. 不能報廢非隔離區的商品
4. 過期商品不能正常出庫
5. 已報廢商品不能出庫
6. 查詢隔離區商品
7. 查詢已報廢商品

### 執行測試

```bash
cd backend
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
mvn test -Dtest=BeverageExpiredHandlingTest
```

---

# 7. JMeter 壓力測試

## 測試目標

驗證智慧倉庫系統在高併發場景下的資料一致性：

1. 驗證悲觀鎖機制：DB Lock（Pessimistic Lock）是否生效
2. 資料一致性：庫存扣減數量必須與訂單成功數精確匹配
3. 負庫存防護：不能出現負庫存

## 測試場景

### 測試參數

- 執行緒數：50-100 個（可調整）
- 同步機制：Synchronizing Timer（確保所有執行緒在同一毫秒內執行）
- 操作類型：對同一個熱門商品（礦泉水）進行出庫操作
- 每個執行緒出庫數量：5 瓶

### 測試流程

1. 初始化庫存：入庫 1000 瓶礦泉水
2. 高併發出庫：100 個執行緒同時出庫，每個出庫 5 瓶
3. 驗證結果：查詢最終庫存統計

## 執行步驟

### 1. 啟動 Spring Boot 服務

```bash
cd backend
mvn spring-boot:run
```

確保服務運行在 `http://localhost:8080`

### 2. 執行 JMeter 測試

#### 方法 1：使用 JMeter GUI

```bash
# 開啟 JMeter
jmeter

# 載入測試腳本
File > Open > jmeter/SmartWarehouse_Concurrency_Test.jmx

# 執行測試
Run > Start
```

#### 方法 2：使用命令列（無 GUI）

```bash
# 執行測試並產生報告
jmeter -n -t jmeter/SmartWarehouse_Concurrency_Test.jmx \
  -l jmeter/results.jtl \
  -e -o jmeter/report
```

### 3. 查看測試結果

- GUI 模式：在 "View Results Tree" 和 "Summary Report" 查看
- 命令列模式：開啟 `jmeter/report/index.html`

## 驗收標準

### 必須通過的檢查

1. 庫存扣減精確匹配
   ```
   成功出庫的執行緒數 × 每執行緒出庫數量 = 實際庫存減少數量
   ```

2. 無負庫存
   ```
   最終庫存 >= 0
   ```

3. 資料一致性
   ```
   初始庫存 - 成功出庫總數 = 最終庫存
   ```

### 預期結果範例

```
初始庫存: 1000 瓶
併發執行緒數: 100
每個執行緒出庫: 5 瓶
預期總出庫: 500 瓶

實際結果：
- 成功出庫執行緒: 100
- 失敗執行緒: 0
- 實際出庫總數: 500
- 最終庫存: 500
- 庫存扣減匹配: 通過
- 無負庫存: 通過
```

## 調整測試參數

在 JMeter GUI 中修改 "User Defined Variables"：

- `THREAD_COUNT`: 執行緒數（預設 100）
- `QUANTITY_PER_THREAD`: 每個執行緒出庫數量（預設 5）
- `BASE_URL`: API 基礎 URL（預設 http://localhost:8080）

---

# 8. 前端介面使用

## SmartWarehouse 前端介面

### 功能特色

- 即時庫存統計 - 顯示總庫存、過期數量、即將過期數量
- 庫存列表查看 - 查看所有庫存商品
- 過期商品管理 - 查看已過期和即將過期的商品
- 隔離區管理 - 查看隔離區中的商品
- 報廢記錄 - 查看已報廢的商品記錄
- 響應式設計 - 支援桌面和行動裝置
- 現代化 UI - 使用 Tailwind CSS，美觀易用

## 快速開始

### 方法 1：直接開啟 HTML 檔案

1. 確保 Spring Boot 服務正在運行（`http://localhost:8080`）
2. 在瀏覽器中開啟 `frontend/index.html`

### 方法 2：使用簡單 HTTP 伺服器

```bash
# 使用 Python 內建伺服器
cd frontend
python3 -m http.server 8000

# 然後在瀏覽器開啟
# http://localhost:8000/index.html
```

## 使用說明

### 1. 設定 API URL

- 預設 API URL 為 `http://localhost:8080/api/beverages`
- 如果需要修改，在頁面上方的輸入框中輸入新的 URL，然後點擊「更新」

### 2. 查看庫存統計

- 頁面載入時會自動載入統計資料
- 點擊「重新整理統計」按鈕可以手動更新

### 3. 查看不同類型的庫存

- 查看所有庫存 - 顯示所有庫存商品
- 查看已過期 - 顯示已過期的商品
- 查看即將過期 - 顯示 7 天內會過期的商品
- 查看隔離區 - 顯示隔離區中的商品
- 查看已報廢 - 顯示已報廢的商品記錄

---

# 9. 服務啟動與管理

## Docker 容器化部署（選項）

### 前置需求：安裝 Docker

**macOS 安裝 Docker Desktop：**

```bash
# 方法 1: 使用 Homebrew（推薦）
brew install --cask docker

# 方法 2: 手動下載
# 前往 https://www.docker.com/products/docker-desktop/
# 下載並安裝 Docker Desktop for Mac
```

**驗證安裝：**

```bash
docker --version
docker compose version
```

**注意：** 新版本的 Docker Desktop 使用 `docker compose`（空格）而不是 `docker-compose`（連字符）

### 使用 Docker Compose 啟動（一鍵啟動應用和資料庫）

```bash
# 構建並啟動所有服務
docker compose up -d

# 或使用舊版命令（如果已安裝 docker-compose）
docker-compose up -d

# 查看服務狀態
docker compose ps

# 查看日誌
docker compose logs -f smart-warehouse

# 停止服務
docker compose down
```

### 使用 Dockerfile 單獨構建

```bash
# 構建 Docker 映像
docker build -t smart-warehouse:latest .

# 運行容器
docker run -d -p 8080:8080 --name smart-warehouse-app smart-warehouse:latest

# 查看容器日誌
docker logs -f smart-warehouse-app

# 停止並刪除容器
docker stop smart-warehouse-app
docker rm smart-warehouse-app
```

### Docker 部署優勢

- 環境一致性：開發、測試、生產環境完全一致
- 快速部署：一鍵啟動應用和資料庫
- 隔離性：應用運行在獨立容器中，不影響本地環境
- 系統測試友好：適合系統測試環境的快速搭建

### 如果不想安裝 Docker

如果系統上沒有 Docker，可以使用本地開發方式（見下方「Spring Boot 服務啟動」章節）。Docker 檔案已準備好，當需要時可以隨時使用。

## Spring Boot 服務啟動（本地開發）

### 快速啟動

```bash
cd backend
mvn spring-boot:run
```

### 驗證服務是否運行

```bash
# 測試 API
curl http://localhost:8080/api/beverages/statistics
```

**預期回應（資料庫為空時）：**
```json
{
  "totalItems": 0,
  "totalQuantity": 0,
  "expiredQuantity": 0,
  "expiringSoonQuantity": 0
}
```

**回應欄位說明：**
- `totalItems`: 總庫存項目數（不同飲料種類的數量）
- `totalQuantity`: 總庫存數量（所有飲料的總瓶數）
- `expiredQuantity`: 已過期的數量
- `expiringSoonQuantity`: 即將過期（7 天內）的數量

**添加資料後的範例回應：**
```json
{
  "totalItems": 1,
  "totalQuantity": 100,
  "expiredQuantity": 0,
  "expiringSoonQuantity": 0
}
```

**檢查端口：**
```bash
lsof -i :8080
```

## 端口檢查

### 為什麼看不到占用端口的進程？

1. 端口確實未被占用：`lsof -i :8080` 沒有輸出 = 端口可用
2. 權限問題：可能需要 `sudo lsof -i :8080` 才能查看所有進程

### 解決方案

```bash
# 方法 1: 使用 sudo（最準確）
sudo lsof -i :8080

# 方法 2: 直接測試服務
curl http://localhost:8080/api/beverages/statistics

# 方法 3: 使用檢查腳本
cd backend
./check_port.sh 8080
```

## 停止服務

```bash
# 方法 1: 在終端按 Ctrl+C

# 方法 2: 終止進程
lsof -i :8080
kill <PID>
```

## 常見問題

### 端口被占用

```bash
# 查看占用端口的程序
lsof -i :8080

# 終止程序
kill <PID>

# 或使用修復腳本
cd backend
./fix_port.sh
```

### Maven 找不到

```bash
# macOS
brew install maven
```

### Java 版本問題

```bash
# 確保使用 Java 17
java -version

# macOS 安裝 Java 17
brew install openjdk@17
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
```

---

# 10. Git/GitHub 設定

## Git 初始化與推送指南

### 步驟 1：初始化 Git 儲存庫

```bash
# 在專案根目錄執行
git init
```

### 步驟 2：建立 .gitignore（已完成）

`.gitignore` 檔案已經建立，會自動忽略：
- Python 虛擬環境
- Java 編譯檔案
- IDE 設定檔
- 環境變數檔案
- 測試結果

### 步驟 3：加入所有檔案

```bash
# 加入所有檔案到暫存區
git add .

# 查看即將提交的檔案
git status
```

### 步驟 4：建立第一次 Commit

```bash
git commit -m "feat: 初始化 SmartWarehouse 智慧倉庫系統

功能：
- Spring Boot 後端（飲料庫存管理）
- H2 Database SQL 資料庫
- 完整的 CRUD 操作
- 入庫/出庫管理（FIFO 策略）
- 過期檢查與提醒
- 業界標準過期商品處理
- ISTQB 測試理論實踐（V 模型）
- Playwright 自動化測試
- FastAPI 測試結果 API
- GitHub Actions CI/CD

技術棧：
- Java 17 + Spring Boot 3.2.0
- H2 Database（內存資料庫）
- Python 3.11 + Playwright
- FastAPI + Firestore
- GitHub Actions
- JMeter 壓力測試"
```

### 步驟 5：在 GitHub 建立新專案

1. 前往 https://github.com/new
2. Repository name: `SmartWarehouse`（或你喜歡的名稱）
3. Description: `智慧倉庫系統 - 飲料庫存管理（Spring Boot + H2 Database + ISTQB 測試理論）`
4. 選擇 Public（公開，方便展示作品集）
5. 不要勾選 "Initialize this repository with a README"（我們已經有 README）
6. 點擊 "Create repository"

### 步驟 6：連接遠端儲存庫並推送

```bash
# 將 GitHub 儲存庫設為遠端（替換 YOUR_USERNAME 和 YOUR_REPO_NAME）
git remote add origin https://github.com/YOUR_USERNAME/SmartWarehouse.git

# 或使用 SSH（如果你有設定 SSH key）
# git remote add origin git@github.com:YOUR_USERNAME/SmartWarehouse.git

# 重新命名分支為 main（如果需要的話）
git branch -M main

# 推送到 GitHub
git push -u origin main
```

### 步驟 7：驗證推送成功

1. 前往你的 GitHub 專案頁面
2. 確認所有檔案都已上傳
3. 確認 README.md 正確顯示

---

# 11. CI/CD 自動化

## GitHub Actions CI/CD Pipeline

SmartWarehouse 專案已配置完整的 CI/CD 自動化流程，每次推送程式碼時會自動執行測試、構建和部署驗證。

## 工作流程說明

### 觸發條件

- 推送到 `main` 或 `develop` 分支
- 建立 Pull Request 到 `main` 或 `develop` 分支
- 手動觸發（workflow_dispatch）

### 工作流程階段

#### 1. 測試階段（Test）

- 執行所有 JUnit 測試
- 生成測試報告
- 上傳測試結果作為 Artifact

**執行命令：**
```bash
mvn test
```

#### 2. 構建階段（Build）

- 編譯 Spring Boot 應用
- 打包成 JAR 檔案
- 上傳 JAR 作為 Artifact

**執行命令：**
```bash
mvn clean package -DskipTests
```

#### 3. Docker 構建階段（Docker Build）

- 構建 Docker 映像
- 測試 Docker 映像是否正常運行
- 驗證容器健康檢查

**執行步驟：**
```bash
docker build -t smart-warehouse:latest .
docker run -d -p 8080:8080 --name test-container smart-warehouse:latest
curl -f http://localhost:8080/api/beverages/statistics
```

#### 4. 整合測試階段（Integration Test）

- 使用 Docker Compose 啟動完整服務
- 執行 API 端到端測試
- 驗證所有 API 端點功能

**測試內容：**
- 統計 API：`GET /api/beverages/statistics`
- 入庫 API：`POST /api/beverages/stock-in`
- 查詢 API：`GET /api/beverages`
- 出庫 API：`POST /api/beverages/stock-out`

#### 5. Playwright 前端測試（Playwright Frontend Tests）

- 使用 Playwright 測試前端介面
- 執行邊界值測試（ISTQB Boundary Value Analysis）
- 測試入庫/出庫表單的邊界值
- 驗證表單驗證和業務邏輯

**測試內容：**
- 入庫數量邊界值（最小值、0、負數、大數值）
- 出庫數量邊界值（最小值、超過庫存、等於庫存）
- 日期邊界值（今天、過去、未來）
- 表單驗證（必填欄位、格式驗證）
- 統計顯示邊界值（空庫存狀態）

**執行步驟：**
1. 構建 Docker 映像
2. 啟動後端服務（Docker 容器）
3. 啟動前端服務（HTTP 伺服器）
4. 執行 Playwright 邊界測試
5. 上傳測試結果和報告

#### 6. 程式碼品質檢查（Code Quality）

- 檢查程式碼格式
- 靜態程式碼分析（如果配置）

## 查看 CI/CD 狀態

### 在 GitHub 上查看

1. 前往你的 GitHub 專案頁面
2. 點擊「Actions」標籤
3. 查看工作流程執行狀態

### 工作流程檔案位置

- `.github/workflows/ci-cd.yml` - 主要的 CI/CD 工作流程（包含 Playwright 測試）
- `.github/workflows/ci.yml` - Python/Playwright 測試工作流程（如果有的話）

### Playwright 測試階段

**測試檔案：**
- `tests/test_frontend_boundary.py` - 完整邊界測試（14 個測試案例）
- `tests/test_frontend_boundary_mcp.py` - Playwright MCP 邊界測試（8 個測試案例）

**測試涵蓋：**
- 入庫數量邊界值（最小值、0、負數、大數值）
- 出庫數量邊界值（最小值、超過庫存、等於庫存）
- 日期邊界值（今天、過去、未來）
- 表單驗證（必填欄位、格式驗證）
- 統計顯示邊界值（空庫存狀態）

**執行環境：**
- 後端：Docker 容器（端口 8080）
- 前端：HTTP 伺服器（端口 8000）
- 瀏覽器：Chromium（headless 模式）

## CI/CD 優勢

1. 自動化測試：每次推送自動執行測試，及早發現問題
2. 自動構建：確保程式碼可以成功編譯和打包
3. Docker 驗證：確保容器化部署正常運作
4. 整合測試：驗證 API 功能完整性
5. 品質保證：確保程式碼符合標準

## 本地測試 CI/CD

### 手動執行測試

```bash
# 執行測試
cd backend
mvn test

# 構建應用
mvn clean package

# 構建 Docker 映像
docker build -t smart-warehouse:latest .

# 測試 Docker Compose
docker compose up -d
curl http://localhost:8080/api/beverages/statistics
docker compose down
```

### 驗證工作流程語法

```bash
# 使用 act 工具（可選，需要安裝）
act -l

# 或直接推送到 GitHub 查看結果
git push origin main
```

## 常見問題

### CI/CD 失敗怎麼辦？

1. 查看 Actions 頁面的錯誤訊息
2. 檢查測試是否通過：`mvn test`
3. 檢查 Docker 構建是否成功：`docker build -t smart-warehouse:latest .`
4. 檢查 API 是否正常：`curl http://localhost:8080/api/beverages/statistics`

### 如何跳過 CI/CD？

在 commit message 中加入 `[skip ci]`：
```bash
git commit -m "docs: 更新文件 [skip ci]"
```

---

# 12. 技術棧與架構

## 技術棧

### 後端（Java）
```
Framework: Spring Boot 3.2.0
Database: H2 Database (內存資料庫)
ORM: Spring Data JPA
Locking: Pessimistic Locking (悲觀鎖)
Validation: Jakarta Validation
Build Tool: Maven
Java Version: 17
Testing: JUnit 5 + Spring Boot Test
```

### 壓力測試
```
Tool: Apache JMeter 5.6+
Test Type: 高併發資料一致性測試
Concurrency: 50-100 執行緒
Synchronization: Synchronizing Timer
```

### 測試與 API（Python）
```
Testing: Playwright + pytest
Backend API: FastAPI
Database: Firestore (GCP 免費層)
CI/CD: GitHub Actions
```

## 資料一致性保證

### 悲觀鎖機制

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT b FROM Beverage b WHERE ...")
List<Beverage> findAvailableBeveragesByNameOrderByExpiryWithLock(...);
```

工作原理：
1. 當執行緒 A 查詢並加鎖時，其他執行緒必須等待
2. 執行緒 A 完成事務提交後，鎖才會釋放
3. 確保同一時間只有一個執行緒可以修改庫存

### 驗收標準

- 庫存扣減精確匹配：庫存扣減數量 = 訂單成功數 × 每單數量
- 無負庫存：最終庫存 >= 0
- 資料一致性：初始庫存 - 成功出庫總數 = 最終庫存

## 核心功能說明

### 1. FIFO 出庫策略
系統會自動按照先進先出（FIFO）原則出庫，優先出庫最早過期的飲料，確保庫存新鮮度。

### 2. 悲觀鎖機制
- 使用 `@Lock(LockModeType.PESSIMISTIC_WRITE)` 對資料庫記錄加鎖
- 確保高併發下不會出現負庫存
- 保證庫存扣減的原子性

### 3. 過期檢查
- 已過期：有效期限 < 今天
- 即將過期：7 天內會過期
- 自動計算：每次查詢自動計算過期狀態

### 4. 業界標準過期商品處理
- 狀態管理：NORMAL → QUARANTINED → DISPOSED
- 自動隔離：過期商品自動隔離
- 報廢流程：完整的報廢記錄
- 出庫保護：過期商品不能出庫

---

# 12. 面試應用指南

## 專案價值總結

### 技術能力展示

1. V 模型完整實踐：從單元到驗收，四層級完整覆蓋
2. ISTQB 測試理論：等價類、邊界值、決策表、狀態轉換
3. 測試設計專業：系統化的測試案例設計
4. 自動化測試：JUnit、JMeter、Playwright
5. 測試管理：風險導向、測試計劃、CI/CD
6. 業界標準實作：過期商品處理符合真實 WMS 系統

### 面試加分項

1. 理論與實務結合：不僅會寫測試，還知道為什麼這樣寫
2. 完整的測試金字塔：單元、整合、系統、驗收全覆蓋
3. 專業測試設計：使用 ISTQB 標準技術
4. 實際專案經驗：WMS 系統的測試經驗
5. 業界標準理解：過期商品處理、狀態管理、報廢流程

## 技術亮點

1. 全端能力：Java Spring Boot + Python FastAPI
2. 資料庫經驗：SQL (H2) + NoSQL (Firestore)
3. 高併發處理：悲觀鎖機制確保資料一致性
4. 測試專業：ISTQB 測試理論 + TDD + JMeter 壓力測試
5. 系統化測試：等價類、邊界值、決策表、狀態轉換測試
6. 多層級測試：單元、整合、系統、驗收測試完整覆蓋
7. 自動化思維：CI/CD 最佳實踐
8. 成本意識：完全使用免費服務
9. 業界標準：過期商品處理符合真實 WMS 系統

## 學習重點

這個專案展示了：

### 後端開發
1. Spring Boot 框架 - RESTful API 開發
2. JPA/Hibernate - ORM 資料庫操作
3. 悲觀鎖機制 - 高併發資料一致性保證
4. 事務管理 - `@Transactional` 原子性操作
5. 分層架構 - Controller → Service → Repository
6. 資料驗證 - Jakarta Validation

### ISTQB 測試理論實踐
1. 測試設計技術 - 等價類劃分、邊界值分析、決策表、狀態轉換
2. 測試級別 - 單元測試、整合測試、系統測試、驗收測試
3. 測試類型 - 功能測試、非功能測試（效能、壓力）
4. 測試管理 - 風險導向測試、測試計劃、測試監控
5. 測試基礎原則 - 7 個 ISTQB 測試原則的實際應用

### 測試與 DevOps
1. TDD 測試 - 測試驅動開發
2. 壓力測試 - JMeter 高併發測試
3. CI/CD 自動化 - GitHub Actions 工作流程
4. 測試自動化 - Playwright 最佳實踐
5. 雲端整合 - Firestore NoSQL 操作

### 業界標準實作
1. 狀態管理 - NORMAL、QUARANTINED、DISPOSED
2. 自動隔離 - 過期商品自動隔離機制
3. 報廢流程 - 完整的報廢記錄和審批流程
4. 出庫保護 - 防止過期商品誤出庫

## 下一步擴展

1. 前端儀表板 - React/Vue 視覺化介面
2. 告警系統 - Slack/Discord 過期提醒
3. 批次操作 - 批量入庫/出庫
4. 報表功能 - 庫存報表、進出貨記錄
5. 多種飲料 - 擴展支援其他飲料類型
6. 分散式鎖 - Redis 分散式鎖（多實例部署）
7. 審批流程 - 大額報廢需要主管審批

---

# 13. 履歷截圖指南

## 推薦截圖畫面

### 1. 主頁面 - 統計儀表板（必截）

畫面內容：
- 4 個統計卡片（總庫存項目、總庫存數量、已過期數量、即將過期數量）
- 操作按鈕列
- 現代化的 UI 設計

展示重點：
- 即時統計功能
- 響應式設計
- 現代化 UI（Tailwind CSS）

### 2. 庫存列表頁面（必截）

畫面內容：
- 所有庫存商品的詳細資訊
- 商品卡片（名稱、數量、日期、狀態）

展示重點：
- 完整的 CRUD 功能展示
- 資料呈現清晰
- 狀態標示（正常/過期/即將過期）

### 3. 過期商品管理（推薦）

展示重點：
- 過期商品管理功能
- 預警機制
- 業界標準的庫存管理

### 4. 隔離區管理（加分項）

展示重點：
- 業界標準的過期商品處理流程
- 狀態管理（NORMAL → QUARANTINED → DISPOSED）
- 完整的 WMS 系統功能

## 截圖技巧

### 瀏覽器設定

1. 全螢幕模式：`Cmd + Ctrl + F`（macOS）
2. 視窗大小：建議使用 1920x1080 或更大的視窗
3. 縮放比例：使用 100% 縮放

### 截圖工具（macOS）

- `Cmd + Shift + 4 + Space`：截取視窗（推薦）

## 履歷使用建議

在履歷中可以這樣描述：

> **SmartWarehouse - 智慧倉庫管理系統**
> 
> 全端作品集專案，整合 Spring Boot 後端與現代化前端介面。
> 
> **技術亮點**：
> - 響應式前端設計（Tailwind CSS）
> - RESTful API 整合
> - 即時庫存統計與管理
> - 過期商品處理流程（業界標準）
> - 狀態管理（NORMAL → QUARANTINED → DISPOSED）

---

## License

MIT License

---

**專案名稱**：SmartWarehouse（智慧倉庫系統）  
**技術棧**：Spring Boot + H2 Database + Pessimistic Locking + JMeter + TDD + ISTQB  
**成本**：$0/月（完全免費）

---

這個專案完美展示了從理論到實作的完整轉換，是一個可以拿來面試自動化測試工程師職位的完整作品集。
