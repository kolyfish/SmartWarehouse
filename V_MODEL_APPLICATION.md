# 📐 V 模型（V-Model）在 SmartWarehouse 專案中的完整應用

## 🎯 V 模型概述

V 模型是軟體測試的經典架構，將測試活動與開發階段對應，形成一個「V」字形：

```
需求分析 ──────────────── 驗收測試
    │                         │
系統設計 ──────────────── 系統測試
    │                         │
架構設計 ──────────────── 整合測試
    │                         │
詳細設計 ──────────────── 單元測試
    │                         │
    實作 (Implementation)
```

---

## 📊 SmartWarehouse 專案的 V 模型應用

### 第一層級：單元測試 (Unit Testing)

**定義**：針對軟體最小的可測試單元（Class 或 Method）進行驗證。

**專案應用**：

#### 1. 等價類劃分 (Equivalence Partitioning, EP)

**理論**：把無限的輸入數據，依照特性分成幾個「代表性」的組別。

**倉儲案例**：庫存數量輸入欄位限制是 1 ~ 10000

| 等價類 | 代表值 | 預期結果 | 測試檔案 |
|--------|--------|---------|---------|
| **有效等價類** | 500 | 成功 | `BeverageServiceEquivalencePartitioningTest` |
| **無效等價類（負數）** | -10 | 驗證異常 | `BeverageServiceEquivalencePartitioningTest` |
| **無效等價類（零）** | 0 | 驗證異常 | `BeverageServiceEquivalencePartitioningTest` |
| **無效等價類（超過上限）** | 10001 | 驗證異常 | `BeverageServiceEquivalencePartitioningTest` |

**實作範例**：
```java
// 有效等價類：正常庫存數量（1-10000）
@Test
void testStockIn_ValidQuantity_NormalRange() {
    request.setQuantity(500); // 代表 1-10000 之間的所有整數
    // 預期：成功
}

// 無效等價類：負數
@Test
void testStockIn_InvalidQuantity_Negative() {
    request.setQuantity(-10); // 代表所有負數
    // 預期：驗證異常
}
```

---

#### 2. 邊界值分析 (Boundary Value Analysis, BVA)

**理論**：Bug 最喜歡躲在邊界上。專門測試邊界點及其「鄰居」。

**倉儲案例**：庫存數量限制 1 ~ 10000

**測試點**：
- **下邊界附近**：`0`, `1`, `2`
- **上邊界附近**：`9999`, `10000`, `10001`

**實作檔案**：`BeverageServiceBoundaryValueTest.java`

**實作範例**：
```java
@Test
void testStockOut_BoundaryValue_ZeroStock() {
    // 邊界值：0（零庫存）
    // 預期：失敗
}

@Test
void testStockOut_BoundaryValue_MinimumStock() {
    // 邊界值：1（最小有效庫存）
    // 預期：成功
}

@Test
void testStockOut_BoundaryValue_EqualStock() {
    // 邊界值：出庫數量 = 庫存
    // 預期：成功，庫存變為 0
}
```

**教授點評**：這能抓出 Off-by-one error（例如 `for` 迴圈中的 `<` vs `<=` 錯誤）。

---

#### 3. 決策表 (Decision Table)

**理論**：當邏輯涉及多個條件組合時，用表格列出所有可能的 True/False 組合。

**倉儲案例**：出庫邏輯決策表

| 條件 | 規則 1 | 規則 2 | 規則 3 | 規則 4 |
|------|--------|--------|--------|--------|
| 庫存 > 0 | T | T | F | F |
| 請求數量 <= 庫存 | T | F | - | - |
| 商品未過期 | T | T | T | F |
| **動作** | ✅ 成功出庫 | ❌ 失敗（庫存不足） | ❌ 失敗（無庫存） | ❌ 失敗（已過期） |

**實作檔案**：`BeverageServiceDecisionTableTest.java`

**實作範例**：
```java
@Test
void testDecisionTable_Rule1_SuccessfulStockOut() {
    // 規則 1：庫存 > 0, 請求 <= 庫存, 未過期
    // 預期：成功出庫
}

@Test
void testDecisionTable_Rule2_ExceedsStock() {
    // 規則 2：庫存 > 0, 請求 > 庫存, 未過期
    // 預期：失敗（庫存不足）
}
```

**教授點評**：這是對付複雜商業邏輯的神器，能確保沒有遺漏任何一種 `if-else` 的路徑。

---

### 第二層級：整合測試 (Integration Testing)

**定義**：將測試過的單元組裝起來，測試它們之間的「介面」和「互動」。

#### 狀態轉換測試 (State Transition Testing)

**理論**：系統中的物件會有不同的狀態，測試從「狀態 A」變成「狀態 B」的過程是否合法。

**倉儲案例**：飲料庫存狀態轉換

**狀態轉換圖**：

```
        [入庫]
          │
          ↓
    [在庫] ───→ [出庫] ───→ [已出庫]
      │
      ├──→ [即將過期] ───→ [已過期]
      │
      └──→ [已過期]
```

**狀態定義**：
- **在庫**：`quantity > 0, expiryDate >= today`
- **即將過期**：`quantity > 0, expiryDate <= today + 7`
- **已過期**：`quantity > 0, expiryDate < today`
- **已出庫**：`quantity = 0`

**合法路徑測試**：
```java
@Test
void testStateTransition_InStock_To_OutOfStock() {
    // 合法路徑：在庫 → 出庫 → 已出庫
    // 1. 入庫（狀態：在庫）
    // 2. 出庫全部（狀態：已出庫）
    // 預期：狀態轉換成功
}
```

**非法路徑測試**：
```java
// 非法路徑：不能直接從「在庫」變成「已出庫」而不經過「出庫」操作
// 系統應該強制執行出庫流程
```

**實作檔案**：`BeverageStateTransitionTest.java`

**教授點評**：自動化測試腳本往往就是模擬一連串的操作來驗證這些狀態變化。

---

### 第三層級：系統測試 (System Testing)

**定義**：將軟體視為一個完整的系統，驗證是否符合規格需求。這是黑箱測試的主戰場。

#### 1. 用例測試 (Use Case Testing)

**理論**：模擬真實使用者的操作情境（User Journey）。

**測試策略**：

##### 基本流 (Happy Path)
**用例**：倉庫管理員順利完成入庫和出庫

```java
@Test
void testUseCase1_StockIn_NewBeverage() {
    // 基本流：入庫新商品
    // 1. 倉庫管理員登入（模擬）
    // 2. 輸入商品資訊
    // 3. 確認入庫
    // 4. 系統更新庫存
    // 預期：成功，庫存增加
}
```

##### 替代流 (Alternative Path)
**用例**：出庫時庫存不足，系統提示並部分出庫

```java
@Test
void testUseCase_StockOut_InsufficientStock() {
    // 替代流：庫存不足場景
    // 1. 嘗試出庫 200 瓶
    // 2. 庫存只有 100 瓶
    // 3. 系統拒絕或部分出庫
    // 預期：失敗或部分成功
}
```

##### 異常流 (Exception Path)
**用例**：資料庫連線失敗、網路斷線

```java
// 異常流測試（需要模擬異常環境）
// 1. 模擬資料庫連線失敗
// 2. 驗證系統是否顯示友善的錯誤訊息
// 3. 驗證資料是否回滾
```

**實作檔案**：`BeverageUseCaseTest.java`

**教授點評**：自動化測試最優先做的就是「基本流」的 Use Case。

---

#### 2. 高併發測試 (High Concurrency / Load Testing)

**理論**：這是「非功能性測試」。測試當 100 人同時操作時，系統會不會崩潰。

**工具**：JMeter, JUnit (多執行緒)

**關注指標**：
- **TPS (Transactions Per Second)**：每秒處理幾筆交易
- **Response Time**：平均回應時間（目標 < 100ms）
- **Error Rate**：錯誤率（目標 0%）
- **資料一致性**：庫存扣減是否精確匹配

**實作檔案**：
- `BeverageServiceConcurrencyTest.java`（JUnit）
- `jmeter/SmartWarehouse_Concurrency_Test.jmx`（JMeter）

**實作範例**：
```java
@Test
void testConcurrentStockOut_ShouldMaintainDataConsistency() {
    // 100 個執行緒同時出庫
    // 每個執行緒出庫 5 瓶
    // 預期：
    // - 成功率 100%
    // - 庫存扣減精確匹配
    // - 無負庫存
}
```

**JMeter 測試指標**：
- 執行緒數：100
- 同步機制：Synchronizing Timer
- 驗證：資料一致性、無負庫存

---

### 第四層級：驗收測試 (Acceptance Testing)

**定義**：由客戶或最終使用者進行的測試，決定是否「收貨」(Go/No-Go)。

#### 驗收標準驗證 (Acceptance Criteria Verification)

**理論**：根據 User Story 背面的「驗收標準 (AC)」逐條核對。

**專案應用**：SmartWarehouse 驗收標準

##### User Story 1：作為倉儲管理員，我希望能夠入庫商品

**AC1**：上傳商品資訊後，系統應顯示入庫成功訊息。
```java
// 驗證：入庫後返回 BeverageDTO，包含完整資訊
```

**AC2**：若輸入無效資料（負數、空字串），系統應顯示具體錯誤訊息。
```java
// 驗證：拋出驗證異常，錯誤訊息清晰
```

**AC3**：入庫操作應在 1 秒內完成。
```java
// 驗證：回應時間 < 1000ms
```

##### User Story 2：作為倉儲管理員，我希望能夠出庫商品（FIFO 策略）

**AC1**：出庫時應優先出庫最早過期的商品。
```java
// 驗證：出庫順序按照過期日期排序
```

**AC2**：在高併發下（100 個使用者同時出庫），庫存扣減必須精確匹配。
```java
// 驗證：庫存扣減數量 = 訂單成功數 × 每單數量
```

**AC3**：不能出現負庫存。
```java
// 驗證：最終庫存 >= 0
```

**實作檔案**：`BeverageServiceConcurrencyTest.java`

**教授點評**：在敏捷開發中，這就是「Definition of Done (DoD)」。如果 AC 沒過，這張票就不能關。

---

## 📐 V 模型完整對照表

| V 模型層級 | ISTQB 測試技術 | 測試檔案 | 測試對象 | 責任人 |
|-----------|---------------|---------|---------|--------|
| **單元測試** | 等價類劃分、邊界值分析、決策表 | `BeverageServiceEquivalencePartitioningTest`<br>`BeverageServiceBoundaryValueTest`<br>`BeverageServiceDecisionTableTest` | Service 方法、Repository 方法 | RD / SDET |
| **整合測試** | 狀態轉換測試 | `BeverageStateTransitionTest` | Service + Repository<br>Controller + Service | SDET |
| **系統測試** | 用例測試、高併發測試 | `BeverageUseCaseTest`<br>`BeverageServiceConcurrencyTest` | 完整系統、API 端點 | SDET / QA |
| **驗收測試** | 驗收標準驗證 | `BeverageServiceConcurrencyTest` | 業務需求、驗收標準 | 客戶 / 產品經理 |

---

## 🎯 面試應用建議

### 談單元測試

**說法**：
> 「我有 Java 開發背景，所以我能讀懂 RD 的單元測試，甚至在做 Code Review 時，我會用**邊界值分析**的邏輯去檢視他們的程式碼是否有漏洞。
> 
> 例如在 SmartWarehouse 專案中，我使用等價類劃分和邊界值分析設計了完整的單元測試，確保庫存輸入驗證的邊界情況都被覆蓋。」

---

### 談整合測試

**說法**：
> 「我非常熟悉複雜的**狀態轉換**，我知道庫存狀態在資料庫中是如何流轉的，這讓我寫自動化腳本時能更精準地驗證資料的一致性。
> 
> 在 SmartWarehouse 專案中，我建立了完整的狀態轉換測試，驗證從『在庫』到『出庫』到『已出庫』的完整流程。」

---

### 談系統測試

**說法**：
> 「我會利用 CI/CD 工具自動執行**用例測試**的腳本，並使用 JMeter 來執行**高併發測試**。
> 
> 在 SmartWarehouse 專案中，我實作了完整的用例測試（基本流、替代流、異常流），並使用 JMeter 驗證 100 個執行緒同時操作時的資料一致性。」

---

### 談驗收測試

**說法**：
> 「我熟悉敏捷開發的驗收標準驗證，確保每個 User Story 的 AC 都通過測試。
> 
> 在 SmartWarehouse 專案中，我建立了明確的驗收標準（庫存扣減精確匹配、無負庫存），並用自動化測試驗證這些標準。」

---

## 📊 狀態轉換圖（State Transition Diagram）

### 飲料庫存狀態轉換圖

```
                    [入庫操作]
                         │
                         ↓
                    ┌─────────┐
                    │  在庫    │ quantity > 0, expiryDate >= today
                    │ (InStock)│
                    └─────────┘
                         │
        ┌────────────────┼────────────────┐
        │                │                │
        ↓                ↓                ↓
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│ 即將過期     │  │   出庫操作    │  │   時間推移    │
│ (ExpiringSoon)│  │              │  │              │
│ expiryDate   │  │              │  │              │
│ <= today+7   │  │              │  │              │
└──────────────┘  │              │  │              │
        │         │              │  │              │
        │         ↓              │  │              │
        │    ┌─────────┐        │  │              │
        │    │ 已出庫   │        │  │              │
        │    │(OutStock)│        │  │              │
        │    │quantity=0│        │  │              │
        │    └─────────┘        │  │              │
        │                        │  │              │
        └────────────────────────┼──┘              │
                                 │                  │
                                 ↓                  ↓
                          ┌──────────────┐  ┌──────────────┐
                          │   已過期     │  │   已過期     │
                          │  (Expired)   │  │  (Expired)   │
                          │ expiryDate   │  │ expiryDate   │
                          │  < today     │  │  < today     │
                          └──────────────┘  └──────────────┘
```

### 狀態轉換表

| 當前狀態 | 事件/操作 | 下一狀態 | 是否合法 |
|---------|----------|---------|---------|
| 在庫 | 出庫操作 | 已出庫 | ✅ 合法 |
| 在庫 | 時間推移（7 天內過期） | 即將過期 | ✅ 合法 |
| 在庫 | 時間推移（已過期） | 已過期 | ✅ 合法 |
| 即將過期 | 出庫操作 | 已出庫 | ✅ 合法 |
| 即將過期 | 時間推移（已過期） | 已過期 | ✅ 合法 |
| 已過期 | 出庫操作 | 已出庫 | ⚠️ 需確認業務規則 |
| 已出庫 | 入庫操作 | 在庫 | ✅ 合法（新商品） |
| 在庫 | 直接變成已出庫（無出庫操作） | 已出庫 | ❌ 非法 |

---

## 🧪 測試案例設計範例

### 範例 1：入庫作業的完整測試設計

#### 單元測試層級

**等價類劃分**：
- 有效：quantity = 500（代表 1-10000）
- 無效：quantity = -10, 0, 10001

**邊界值分析**：
- 下邊界：0, 1, 2
- 上邊界：9999, 10000, 10001

**決策表**：
| 條件 | 規則 1 | 規則 2 |
|------|--------|--------|
| quantity > 0 | T | F |
| name 不為空 | T | T |
| **動作** | 成功 | 失敗 |

#### 整合測試層級

**狀態轉換**：
- 無庫存 → 入庫 → 在庫

#### 系統測試層級

**用例測試**：
- 基本流：順利入庫
- 替代流：輸入錯誤，修正後成功
- 異常流：資料庫連線失敗

#### 驗收測試層級

**驗收標準**：
- AC1：入庫成功後顯示確認訊息
- AC2：無效輸入顯示錯誤訊息
- AC3：回應時間 < 1 秒

---

## 📈 測試覆蓋率目標

| 測試級別 | 覆蓋率目標 | 當前狀態 |
|---------|-----------|---------|
| **單元測試** | 80%+ | ✅ 高覆蓋 |
| **整合測試** | 70%+ | ✅ 高覆蓋 |
| **系統測試** | 60%+ | ✅ 中高覆蓋 |
| **驗收測試** | 100% | ✅ 完整覆蓋 |

---

## 🎓 學習重點

### V 模型的優勢

1. **對應關係明確**：每個開發階段都有對應的測試活動
2. **測試層級清晰**：從單元到系統，層層把關
3. **風險控制**：早期發現問題，降低修復成本

### 在 SmartWarehouse 專案中的體現

1. ✅ **單元測試**：確保每個方法都正確
2. ✅ **整合測試**：確保組件之間協作正常
3. ✅ **系統測試**：確保整體功能符合需求
4. ✅ **驗收測試**：確保符合業務需求

---

## 📚 相關文件

- [ISTQB 測試理論詳細說明](./ISTQB_TEST_THEORY.md)
- [ISTQB 應用總結](./ISTQB_APPLICATION_SUMMARY.md)
- [執行 ISTQB 測試指南](./RUN_ISTQB_TESTS.md)
- [V 模型應用](./V_MODEL_APPLICATION.md)（本文件）

---

**這個專案完美展示了 V 模型在實際專案中的系統化應用！** 🎉

