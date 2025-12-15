# 🏭 過期商品處理指南（業界標準實作）

## 📋 業界標準流程

SmartWarehouse 專案已實作業界標準的過期商品處理機制，符合真實 WMS 系統的做法。

---

## 🎯 核心功能

### 1. **狀態管理（Status Management）**

商品有三種狀態：

| 狀態 | 說明 | 是否可以出庫 |
|------|------|-------------|
| **NORMAL** | 正常商品 | ✅ 可以 |
| **QUARANTINED** | 隔離中（已過期但尚未處理） | ❌ 不可以 |
| **DISPOSED** | 已報廢 | ❌ 不可以 |

### 2. **自動隔離機制**

- 系統自動檢測過期商品
- 自動將狀態改為 `QUARANTINED`
- 防止過期商品誤出庫

### 3. **報廢流程**

- 只能報廢隔離區中的商品
- 記錄報廢原因和時間
- 狀態改為 `DISPOSED`

### 4. **出庫保護**

- 出庫時只查詢 `NORMAL` 狀態的商品
- 過期商品（`QUARANTINED`）不能出庫
- 已報廢商品（`DISPOSED`）不能出庫

---

## 🔄 處理流程

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

**系統行為**：
1. 查詢所有過期商品（`expiryDate < today`）
2. 將狀態為 `NORMAL` 的商品改為 `QUARANTINED`
3. 返回隔離數量

**通常由定時任務（Scheduler）每日執行**

---

#### 步驟 2：查看隔離區商品

```bash
# API 呼叫
GET /api/beverages/quarantined

# 回應
[
  {
    "id": 1,
    "name": "礦泉水",
    "quantity": 100,
    "status": "QUARANTINED",
    "expiryDate": "2024-12-10",
    "expired": true
  }
]
```

---

#### 步驟 3：報廢商品

```bash
# API 呼叫
POST /api/beverages/{id}/dispose
Content-Type: application/json

{
  "reason": "過期報廢，已超過有效期限"
}

# 回應
{
  "id": 1,
  "name": "礦泉水",
  "quantity": 100,
  "status": "DISPOSED",
  "disposalReason": "過期報廢，已超過有效期限",
  "disposedAt": "2024-12-15T10:30:00"
}
```

**系統行為**：
1. 檢查商品狀態（必須是 `QUARANTINED`）
2. 將狀態改為 `DISPOSED`
3. 記錄報廢原因和時間

---

#### 步驟 4：查看已報廢商品

```bash
# API 呼叫
GET /api/beverages/disposed

# 回應
[
  {
    "id": 1,
    "name": "礦泉水",
    "quantity": 100,
    "status": "DISPOSED",
    "disposalReason": "過期報廢，已超過有效期限",
    "disposedAt": "2024-12-15T10:30:00"
  }
]
```

---

## 🛡️ 出庫保護機制

### 出庫時的自動過濾

系統在出庫時會自動過濾：

```java
// Repository 查詢（只查詢 NORMAL 狀態的商品）
@Query("SELECT b FROM Beverage b WHERE b.name = :name AND b.quantity > 0 AND b.expiryDate >= :today AND b.status = 'NORMAL' ORDER BY b.expiryDate ASC")
List<Beverage> findAvailableBeveragesByNameOrderByExpiryWithLock(...);
```

**保護機制**：
- ✅ 只查詢 `NORMAL` 狀態的商品
- ✅ 只查詢未過期的商品（`expiryDate >= today`）
- ✅ 過期商品（`QUARANTINED`）不會被查詢到
- ✅ 已報廢商品（`DISPOSED`）不會被查詢到

---

## 📊 業界最佳實踐

### 1. **FIFO + 過期檢查**

- 出庫時優先出庫最早過期的（FIFO）
- 但只出庫未過期的商品
- 過期商品自動隔離

### 2. **批次管理**

- 每個入庫批次有獨立的過期日期
- 可以追蹤哪個批次過期
- 方便批次報廢

### 3. **審批流程**

- 小額報廢：直接報廢
- 大額報廢：需要主管審批（未來擴展）
- 記錄審批人、時間、原因

### 4. **報表統計**

- 過期商品數量統計
- 報廢金額統計
- 過期趨勢分析

---

## 🧪 測試案例

### 測試檔案

`BeverageExpiredHandlingTest.java` 包含完整的測試案例：

1. ✅ 自動隔離過期商品
2. ✅ 報廢隔離區中的商品
3. ✅ 不能報廢非隔離區的商品
4. ✅ 過期商品不能正常出庫
5. ✅ 已報廢商品不能出庫
6. ✅ 查詢隔離區商品
7. ✅ 查詢已報廢商品

### 執行測試

```bash
cd backend
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
mvn test -Dtest=BeverageExpiredHandlingTest
```

---

## 💡 面試應用

### 可以強調的內容

1. **業界標準實作**
   - 符合真實 WMS 系統的做法
   - 狀態管理、隔離機制、報廢流程

2. **資料完整性**
   - 過期商品不能誤出庫
   - 報廢記錄完整（原因、時間）

3. **系統保護機制**
   - 出庫時自動過濾過期商品
   - 防止資料不一致

4. **擴展性**
   - 未來可以加入審批流程
   - 可以加入報廢金額統計

---

## 📚 相關文件

- [業界過期商品處理標準](./INDUSTRY_EXPIRED_PRODUCT_HANDLING.md)
- [狀態轉換圖](./STATE_TRANSITION_DIAGRAM.md)
- [V 模型應用](./V_MODEL_APPLICATION.md)

---

**這個實作完美展示了對業界標準的理解和實際應用能力！** 🎉

