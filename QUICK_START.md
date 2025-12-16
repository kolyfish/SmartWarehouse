# SmartWarehouse 快速運行指南

## 方式一：使用 Docker（推薦）

### 1. 啟動服務

```bash
# 啟動服務（後台運行）
docker compose up -d

# 查看服務狀態
docker compose ps

# 查看日誌
docker compose logs -f smart-warehouse
```

### 2. 驗證服務運行

```bash
# 測試 API
curl http://localhost:8080/api/beverages/statistics

# 應該看到：
# {"totalItems":0,"totalQuantity":0,"expiredQuantity":0,"expiringSoonQuantity":0}
```

### 3. 訪問前端介面

**方法 1：直接開啟 HTML 檔案**
```bash
# 在瀏覽器中開啟
open frontend/index.html
```

**方法 2：使用簡單 HTTP 伺服器**
```bash
cd frontend
python3 -m http.server 8000
# 然後在瀏覽器開啟 http://localhost:8000/index.html
```

### 4. 停止服務

```bash
docker compose down
```

---

## 方式二：本地開發（不使用 Docker）

### 1. 啟動 Spring Boot 後端

```bash
cd backend
mvn spring-boot:run
```

等待看到 `Started BeverageWarehouseApplication` 訊息

### 2. 驗證服務

```bash
# 在另一個終端視窗
curl http://localhost:8080/api/beverages/statistics
```

### 3. 訪問前端

```bash
# 開啟前端
open frontend/index.html
```

---

## 基本使用流程

### 1. 查看庫存統計

- 開啟前端介面
- 點擊「重新整理統計」按鈕
- 查看總庫存、過期數量等統計

### 2. 入庫飲料（使用 API）

**這是什麼？**
這是一個 HTTP POST 請求，用來「入庫飲料」到倉庫系統。

**命令說明：**
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

**各部分解釋：**
- `curl -X POST`: 使用 curl 工具發送 POST 請求（新增資料）
- `http://localhost:8080/api/beverages/stock-in`: API 端點（入庫飲料的網址）
- `-H "Content-Type: application/json"`: 告訴伺服器傳送的是 JSON 格式資料
- `-d '{ ... }'`: 要傳送的資料內容

**資料內容：**
- `"name": "礦泉水"`: 飲料名稱
- `"quantity": 100`: 數量（100 瓶，**一次入庫限制：1-100 瓶**）
- `"productionDate": "2024-01-01"`: 生產日期
- `"expiryDate": "2025-01-01"`: 有效期限

**執行後會發生什麼？**
1. 系統接收你的入庫請求
2. 在資料庫中新增這筆飲料記錄
3. 返回新增的飲料資訊（包含 ID、建立時間等）

**簡單理解：**
「我要入庫 100 瓶礦泉水，生產日期是 2024-01-01，有效期限是 2025-01-01」

### 3. 查看所有庫存

- 在前端點擊「查看所有庫存」
- 或使用 API：
```bash
curl http://localhost:8080/api/beverages
```

### 4. 出庫飲料

```bash
curl -X POST http://localhost:8080/api/beverages/stock-out \
  -H "Content-Type: application/json" \
  -d '{
    "name": "礦泉水",
    "quantity": 30
  }'
```

---

## 快速測試腳本

```bash
# 執行快速測試（入庫、查詢、出庫）
./QUICK_TEST.sh
```

---

## 常用 API 端點

| 功能 | 方法 | 路徑 |
|------|------|------|
| 查看統計 | GET | `/api/beverages/statistics` |
| 查看所有庫存 | GET | `/api/beverages` |
| 入庫 | POST | `/api/beverages/stock-in` |
| 出庫 | POST | `/api/beverages/stock-out` |
| 查看已過期 | GET | `/api/beverages/expired` |
| 查看即將過期 | GET | `/api/beverages/expiring-soon` |

---

## 執行 Playwright 邊界測試

### 前置需求

1. 確保後端服務運行：
   ```bash
   docker compose up -d
   # 或
   cd backend && mvn spring-boot:run
   ```

2. 啟動前端服務：
   ```bash
   cd frontend
   python3 -m http.server 8000
   ```

3. 安裝測試依賴：
   ```bash
   pip install -r requirements.txt
   playwright install chromium
   ```

### 執行邊界測試

```bash
# 執行所有邊界測試
pytest tests/test_frontend_boundary.py -v -m boundary

# 執行特定測試
pytest tests/test_frontend_boundary.py::TestFrontendBoundary::test_stock_in_quantity_minimum -v

# 使用 Playwright MCP 測試
pytest tests/test_frontend_boundary_mcp.py -v -m boundary
```

### 測試覆蓋範圍

邊界測試涵蓋：
- 入庫數量邊界值（最小值 1、0、負數、大數值）
- 出庫數量邊界值（最小值、超過庫存、等於庫存）
- 日期邊界值（今天、過去、未來）
- 表單驗證（必填欄位、格式驗證）
- 統計顯示邊界值（空庫存狀態）

---

## 故障排除

### 端口被占用

```bash
# 查看占用端口的程序
lsof -i :8080

# 停止程序
kill <PID>
```

### Docker 服務無法啟動

```bash
# 查看日誌
docker compose logs smart-warehouse

# 重新構建
docker compose up -d --build
```

### 前端無法連接 API

- 確認後端服務正在運行
- 檢查 API URL 是否正確（預設：`http://localhost:8080/api/beverages`）
- 檢查瀏覽器控制台是否有錯誤訊息

