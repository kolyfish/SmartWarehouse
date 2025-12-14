# 📚 API 使用範例

## 🏭 SmartWarehouse API 完整範例

### 基礎設定

```bash
# API 基礎 URL
BASE_URL=http://localhost:8080/api/beverages
```

---

## 1. 入庫管理（Stock In）

### 入庫 100 瓶礦泉水

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

**回應範例**：
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
  "expiringSoon": false
}
```

### 入庫多批不同日期的礦泉水

```bash
# 第一批：2024-01-01 生產，2025-01-01 過期
curl -X POST http://localhost:8080/api/beverages/stock-in \
  -H "Content-Type: application/json" \
  -d '{
    "name": "礦泉水",
    "quantity": 100,
    "productionDate": "2024-01-01",
    "expiryDate": "2025-01-01"
  }'

# 第二批：2024-02-01 生產，2025-02-01 過期
curl -X POST http://localhost:8080/api/beverages/stock-in \
  -H "Content-Type: application/json" \
  -d '{
    "name": "礦泉水",
    "quantity": 50,
    "productionDate": "2024-02-01",
    "expiryDate": "2025-02-01"
  }'

# 第三批：2024-03-01 生產，2025-03-01 過期
curl -X POST http://localhost:8080/api/beverages/stock-in \
  -H "Content-Type: application/json" \
  -d '{
    "name": "礦泉水",
    "quantity": 200,
    "productionDate": "2024-03-01",
    "expiryDate": "2025-03-01"
  }'
```

---

## 2. 查詢所有飲料

```bash
curl http://localhost:8080/api/beverages
```

**回應範例**：
```json
[
  {
    "id": 1,
    "name": "礦泉水",
    "quantity": 100,
    "productionDate": "2024-01-01",
    "expiryDate": "2025-01-01",
    "expired": false,
    "daysUntilExpiry": 346,
    "expiringSoon": false
  },
  {
    "id": 2,
    "name": "礦泉水",
    "quantity": 50,
    "productionDate": "2024-02-01",
    "expiryDate": "2025-02-01",
    "expired": false,
    "daysUntilExpiry": 377,
    "expiringSoon": false
  }
]
```

---

## 3. 根據 ID 查詢飲料

```bash
curl http://localhost:8080/api/beverages/1
```

---

## 4. 出庫管理（Stock Out）

### 出庫 50 瓶礦泉水（系統會自動選擇最早過期的）

```bash
curl -X POST http://localhost:8080/api/beverages/stock-out \
  -H "Content-Type: application/json" \
  -d '{
    "name": "礦泉水",
    "quantity": 50
  }'
```

**回應範例**：
```json
{
  "message": "成功出庫 50 瓶 礦泉水"
}
```

**說明**：
- 系統會按照 **FIFO（先進先出）** 原則
- 優先出庫**最早過期**的飲料
- 如果第一批數量不足，會自動從下一批補足

### 出庫 150 瓶（跨批次）

```bash
curl -X POST http://localhost:8080/api/beverages/stock-out \
  -H "Content-Type: application/json" \
  -d '{
    "name": "礦泉水",
    "quantity": 150
  }'
```

**系統行為**：
1. 先從第一批（100 瓶，2025-01-01 過期）出庫 100 瓶
2. 再從第二批（50 瓶，2025-02-01 過期）出庫 50 瓶
3. 確保優先出庫最早過期的

---

## 5. 更新飲料資訊

```bash
curl -X PUT http://localhost:8080/api/beverages/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "礦泉水",
    "quantity": 80,
    "productionDate": "2024-01-01",
    "expiryDate": "2025-01-01"
  }'
```

---

## 6. 刪除飲料

```bash
curl -X DELETE http://localhost:8080/api/beverages/1
```

**回應範例**：
```json
{
  "message": "成功刪除飲料，ID: 1"
}
```

---

## 7. 查詢已過期的飲料

```bash
curl http://localhost:8080/api/beverages/expired
```

**使用場景**：
- 定期清理過期庫存
- 產生過期報告

---

## 8. 查詢即將過期的飲料（7 天內）

```bash
curl http://localhost:8080/api/beverages/expiring-soon
```

**使用場景**：
- 提前處理即將過期的庫存
- 促銷活動規劃
- 庫存預警

---

## 9. 取得庫存統計

```bash
curl http://localhost:8080/api/beverages/statistics
```

**回應範例**：
```json
{
  "totalItems": 3,
  "totalQuantity": 350,
  "expiredQuantity": 0,
  "expiringSoonQuantity": 0
}
```

**欄位說明**：
- `totalItems`: 總庫存項目數
- `totalQuantity`: 總庫存數量
- `expiredQuantity`: 已過期數量
- `expiringSoonQuantity`: 即將過期數量（7 天內）

---

## 🔄 完整流程範例

### 場景：倉庫日常操作

```bash
# 1. 入庫新貨
curl -X POST http://localhost:8080/api/beverages/stock-in \
  -H "Content-Type: application/json" \
  -d '{
    "name": "礦泉水",
    "quantity": 100,
    "productionDate": "2024-12-20",
    "expiryDate": "2025-12-20"
  }'

# 2. 查看庫存統計
curl http://localhost:8080/api/beverages/statistics

# 3. 檢查即將過期的飲料
curl http://localhost:8080/api/beverages/expiring-soon

# 4. 出庫 30 瓶（給客戶）
curl -X POST http://localhost:8080/api/beverages/stock-out \
  -H "Content-Type: application/json" \
  -d '{
    "name": "礦泉水",
    "quantity": 30
  }'

# 5. 再次查看庫存
curl http://localhost:8080/api/beverages
```

---

## ⚠️ 錯誤處理範例

### 庫存不足

```bash
# 嘗試出庫 1000 瓶（但庫存只有 100 瓶）
curl -X POST http://localhost:8080/api/beverages/stock-out \
  -H "Content-Type: application/json" \
  -d '{
    "name": "礦泉水",
    "quantity": 1000
  }'
```

**錯誤回應**：
```json
{
  "timestamp": "2024-12-20T10:00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "庫存不足，無法出庫 1000 瓶 礦泉水"
}
```

### 驗證錯誤

```bash
# 數量為負數
curl -X POST http://localhost:8080/api/beverages/stock-in \
  -H "Content-Type: application/json" \
  -d '{
    "name": "礦泉水",
    "quantity": -10,
    "productionDate": "2024-01-01",
    "expiryDate": "2025-01-01"
  }'
```

**錯誤回應**：
```json
{
  "timestamp": "2024-12-20T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "數量必須大於 0"
}
```

---

## 📝 使用 Postman 測試

1. 匯入以下 Collection（JSON）：

```json
{
  "info": {
    "name": "SmartWarehouse API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "入庫飲料",
      "request": {
        "method": "POST",
        "url": "http://localhost:8080/api/beverages/stock-in",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"name\": \"礦泉水\",\n  \"quantity\": 100,\n  \"productionDate\": \"2024-01-01\",\n  \"expiryDate\": \"2025-01-01\"\n}"
        }
      }
    }
  ]
}
```

2. 或直接在 Postman 中建立新的 Request

---

## 🎯 測試腳本範例（Bash）

```bash
#!/bin/bash

BASE_URL="http://localhost:8080/api/beverages"

echo "=== SmartWarehouse API 測試 ==="

# 1. 入庫
echo "1. 入庫 100 瓶礦泉水..."
curl -X POST $BASE_URL/stock-in \
  -H "Content-Type: application/json" \
  -d '{
    "name": "礦泉水",
    "quantity": 100,
    "productionDate": "2024-01-01",
    "expiryDate": "2025-01-01"
  }'

echo -e "\n\n2. 查詢所有飲料..."
curl $BASE_URL

echo -e "\n\n3. 出庫 50 瓶..."
curl -X POST $BASE_URL/stock-out \
  -H "Content-Type: application/json" \
  -d '{
    "name": "礦泉水",
    "quantity": 50
  }'

echo -e "\n\n4. 查看統計..."
curl $BASE_URL/statistics

echo -e "\n\n=== 測試完成 ==="
```

儲存為 `test_api.sh`，執行：
```bash
chmod +x test_api.sh
./test_api.sh
```

