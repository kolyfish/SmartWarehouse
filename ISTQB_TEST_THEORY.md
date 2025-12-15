# 📚 ISTQB 測試理論在 SmartWarehouse 專案中的應用

## 📋 目錄

1. [ISTQB 測試基礎原則](#istqb-測試基礎原則)
2. [測試設計技術](#測試設計技術)
3. [測試級別](#測試級別)
4. [測試類型](#測試類型)
5. [測試管理](#測試管理)
6. [實際應用案例](#實際應用案例)

---

## ISTQB 測試基礎原則

### 原則 1：測試顯示缺陷的存在

**理論**：測試可以顯示缺陷存在，但不能證明系統沒有缺陷。

**專案應用**：
- ✅ **高併發測試**：`BeverageServiceConcurrencyTest` 驗證在高併發下是否存在資料不一致
- ✅ **負庫存防護測試**：驗證系統是否能正確防止負庫存
- ✅ **邊界值測試**：測試庫存為 0、負數、極大值等邊界情況

**實作範例**：
```java
// 驗證負庫存防護
assertTrue(actualRemainingStock >= 0, "庫存不能為負數");
```

---

### 原則 2：窮盡測試是不可能的

**理論**：除了簡單系統，完全測試所有輸入和前置條件組合是不可能的。

**專案應用**：
- ✅ **風險導向測試**：優先測試高風險功能（出庫操作、高併發場景）
- ✅ **等價類劃分**：將測試資料分為有效/無效等價類
- ✅ **邊界值分析**：測試邊界條件（庫存 = 0, 1, 最大庫存）

**實作範例**：
```java
// 測試不同庫存場景
- 正常庫存（100-1000）
- 低庫存（1-10）
- 零庫存（0）
- 庫存不足場景
```

---

### 原則 3：早期測試

**理論**：測試活動應該盡早開始，與開發活動並行。

**專案應用**：
- ✅ **TDD（測試驅動開發）**：先寫測試，再實作功能
- ✅ **單元測試**：在開發 Service 層時同步撰寫測試
- ✅ **CI/CD 整合**：每次提交自動執行測試

**實作範例**：
```java
// TDD 流程
1. 撰寫測試案例（BeverageServiceConcurrencyTest）
2. 執行測試（失敗）
3. 實作功能（悲觀鎖機制）
4. 執行測試（通過）
```

---

### 原則 4：缺陷集群

**理論**：大部分缺陷通常集中在少數模組中。

**專案應用**：
- ✅ **重點測試出庫功能**：出庫操作涉及資料一致性，風險最高
- ✅ **重點測試 Service 層**：業務邏輯複雜，容易出錯
- ✅ **重點測試併發場景**：高併發是最容易出現問題的場景

**實作範例**：
```java
// 重點測試模組
- BeverageService.stockOut() - 出庫邏輯（高風險）
- BeverageRepository - 資料庫操作（高風險）
- 併發測試場景（高風險）
```

---

### 原則 5：小心殺蟲劑悖論

**理論**：重複執行相同的測試案例，最終會無法發現新的缺陷。

**專案應用**：
- ✅ **多樣化測試場景**：正常流程、異常流程、邊界條件
- ✅ **不同測試工具**：JUnit（單元測試）、JMeter（壓力測試）、Playwright（E2E 測試）
- ✅ **定期更新測試案例**：根據新需求更新測試

**實作範例**：
```java
// 多樣化測試場景
- 正常出庫
- 庫存不足
- 併發出庫
- 過期商品出庫
- 無效輸入
```

---

### 原則 6：測試活動依賴於測試背景

**理論**：測試活動應該根據不同的測試背景（如安全關鍵系統 vs. 商業系統）進行調整。

**專案應用**：
- ✅ **倉庫系統特性**：資料一致性是關鍵，重點測試併發和資料完整性
- ✅ **測試策略**：針對 WMS（倉庫管理系統）特性，重點測試庫存準確性

**實作範例**：
```java
// WMS 系統重點測試
- 資料一致性（庫存準確性）
- 併發控制（多使用者同時操作）
- 業務規則（FIFO 出庫策略）
```

---

### 原則 7：無錯誤謬論

**理論**：即使系統沒有缺陷，如果不符合使用者的需求和期望，仍然是不合格的。

**專案應用**：
- ✅ **驗收標準明確**：庫存扣減必須精確匹配、不能出現負庫存
- ✅ **業務規則驗證**：FIFO 出庫策略是否符合業務需求
- ✅ **使用者體驗測試**：API 回應時間、錯誤訊息是否清晰

**實作範例**：
```java
// 驗收標準
assertEquals(
    successCount.get() * QUANTITY_PER_THREAD,
    actualTotalOut,
    "庫存扣減數量必須與訂單成功數精確匹配"
);
```

---

## 測試設計技術

### 1. 等價類劃分（Equivalence Partitioning）

**理論**：將輸入資料分成有效和無效等價類，從每個等價類選擇代表性測試資料。

**專案應用**：

#### 有效等價類
- ✅ 正常庫存數量（1-10000）
- ✅ 有效日期格式（YYYY-MM-DD）
- ✅ 有效飲料名稱（"礦泉水"）

#### 無效等價類
- ✅ 負數庫存（-1, -100）
- ✅ 零庫存出庫（quantity = 0）
- ✅ 無效日期格式
- ✅ 空字串或 null

**實作範例**：
```java
// 有效等價類測試
@Test
void testStockIn_ValidQuantity() {
    StockInRequestDTO request = new StockInRequestDTO();
    request.setQuantity(100); // 有效範圍：1-10000
    // ...
}

// 無效等價類測試
@Test
void testStockIn_InvalidQuantity() {
    StockInRequestDTO request = new StockInRequestDTO();
    request.setQuantity(-1); // 無效：負數
    // 應該拋出驗證異常
}
```

---

### 2. 邊界值分析（Boundary Value Analysis）

**理論**：測試邊界值和邊界值附近的值。

**專案應用**：

#### 庫存邊界值
- ✅ 最小值：0（零庫存）
- ✅ 最小值+1：1（最小有效庫存）
- ✅ 正常值：100（中間值）
- ✅ 最大值-1：9999
- ✅ 最大值：10000（假設的最大庫存）

#### 日期邊界值
- ✅ 今天（剛好過期）
- ✅ 今天+1（明天過期）
- ✅ 今天+7（7 天後過期，即將過期邊界）

**實作範例**：
```java
@Test
@DisplayName("邊界值測試 - 零庫存出庫")
void testStockOut_ZeroStock() {
    // 庫存為 0
    // 嘗試出庫應該失敗
}

@Test
@DisplayName("邊界值測試 - 最小庫存出庫")
void testStockOut_MinimumStock() {
    // 庫存為 1
    // 出庫 1 瓶應該成功
    // 再次出庫應該失敗
}
```

---

### 3. 決策表測試（Decision Table Testing）

**理論**：使用決策表系統化地測試業務規則的所有組合。

**專案應用**：出庫業務規則

| 條件 | 規則 1 | 規則 2 | 規則 3 | 規則 4 |
|------|--------|--------|--------|--------|
| 庫存 > 0 | T | T | F | F |
| 請求數量 <= 庫存 | T | F | - | - |
| 商品未過期 | T | T | T | F |
| **動作** | 成功出庫 | 部分出庫 | 失敗（無庫存） | 失敗（已過期） |

**實作範例**：
```java
@Test
@DisplayName("決策表測試 - 規則 1：正常出庫")
void testStockOut_DecisionTable_Rule1() {
    // 庫存 > 0, 請求數量 <= 庫存, 未過期
    // 預期：成功出庫
}

@Test
@DisplayName("決策表測試 - 規則 2：庫存不足")
void testStockOut_DecisionTable_Rule2() {
    // 庫存 > 0, 請求數量 > 庫存, 未過期
    // 預期：部分出庫或失敗
}
```

---

### 4. 狀態轉換測試（State Transition Testing）

**理論**：測試系統在不同狀態之間的轉換。

**專案應用**：飲料庫存狀態轉換

```
[入庫] → [在庫] → [出庫] → [已出庫]
           ↓
        [即將過期] → [已過期]
```

**狀態定義**：
- **在庫**：quantity > 0, expiryDate >= today
- **即將過期**：quantity > 0, expiryDate <= today + 7
- **已過期**：quantity > 0, expiryDate < today
- **已出庫**：quantity = 0

**實作範例**：
```java
@Test
@DisplayName("狀態轉換測試 - 在庫 → 出庫 → 已出庫")
void testStateTransition_InStock_To_OutOfStock() {
    // 1. 初始狀態：在庫
    // 2. 執行出庫操作
    // 3. 驗證狀態轉換為：已出庫
}
```

---

### 5. 用例測試（Use Case Testing）

**理論**：基於使用者場景設計測試案例。

**專案應用**：倉庫管理員使用場景

**主要用例**：
1. **用例 1：入庫新商品**
   - 前置條件：倉庫管理員登入
   - 主要流程：輸入商品資訊 → 確認入庫 → 系統更新庫存
   - 後置條件：庫存增加，記錄入庫時間

2. **用例 2：出庫商品**
   - 前置條件：有可用庫存
   - 主要流程：選擇商品 → 輸入數量 → 系統按 FIFO 出庫
   - 後置條件：庫存減少，優先出庫最早過期的

3. **用例 3：查詢即將過期商品**
   - 前置條件：有庫存
   - 主要流程：查詢即將過期商品 → 系統列出 7 天內過期的商品
   - 後置條件：顯示過期提醒

**實作範例**：
```java
@Test
@DisplayName("用例測試 - 用例 1：入庫新商品")
void testUseCase_StockIn() {
    // 模擬倉庫管理員入庫操作
    // 驗證庫存增加、記錄建立
}

@Test
@DisplayName("用例測試 - 用例 2：出庫商品（FIFO）")
void testUseCase_StockOut_FIFO() {
    // 模擬出庫操作
    // 驗證優先出庫最早過期的商品
}
```

---

## 測試級別

### 1. 單元測試（Unit Testing）

**理論**：測試最小的可測試單元（通常是函數或方法）。

**專案應用**：
- ✅ **Service 層方法**：`BeverageService.stockOut()`
- ✅ **Repository 層方法**：`BeverageRepository.findAvailableBeveragesByNameOrderByExpiryWithLock()`
- ✅ **Entity 方法**：`Beverage.isExpired()`, `Beverage.getDaysUntilExpiry()`

**實作範例**：
```java
@Test
@DisplayName("單元測試 - Beverage.isExpired()")
void testBeverage_IsExpired() {
    Beverage beverage = new Beverage();
    beverage.setExpiryDate(LocalDate.now().minusDays(1));
    assertTrue(beverage.isExpired());
}
```

---

### 2. 整合測試（Integration Testing）

**理論**：測試多個單元組合在一起是否正常運作。

**專案應用**：
- ✅ **Service + Repository 整合**：測試 Service 層調用 Repository 層
- ✅ **Controller + Service 整合**：測試 API 端點調用 Service 層
- ✅ **資料庫整合**：測試 JPA 與 H2 資料庫的整合

**實作範例**：
```java
@SpringBootTest
@ActiveProfiles("test")
class BeverageServiceIntegrationTest {
    // 測試 Service 與 Repository 的整合
    // 測試資料庫操作
}
```

---

### 3. 系統測試（System Testing）

**理論**：測試完整的系統是否符合需求規格。

**專案應用**：
- ✅ **端到端測試**：從 API 請求到資料庫操作的完整流程
- ✅ **高併發系統測試**：`BeverageServiceConcurrencyTest`
- ✅ **JMeter 壓力測試**：模擬真實使用場景

**實作範例**：
```java
// 系統測試：完整的出庫流程
1. API 請求 → Controller
2. Controller → Service
3. Service → Repository（加鎖）
4. Repository → Database
5. 驗證結果
```

---

### 4. 驗收測試（Acceptance Testing）

**理論**：驗證系統是否符合業務需求和驗收標準。

**專案應用**：
- ✅ **驗收標準 1**：庫存扣減數量 = 訂單成功數 × 每單數量
- ✅ **驗收標準 2**：不能出現負庫存
- ✅ **驗收標準 3**：初始庫存 - 成功出庫總數 = 最終庫存

**實作範例**：
```java
// 驗收測試驗證
assertEquals(
    successCount.get() * QUANTITY_PER_THREAD,
    actualTotalOut,
    "驗收標準 1：庫存扣減數量必須與訂單成功數精確匹配"
);
```

---

## 測試類型

### 1. 功能測試（Functional Testing）

**理論**：測試系統功能是否符合需求規格。

**專案應用**：
- ✅ **CRUD 操作測試**：新增、查詢、更新、刪除飲料
- ✅ **業務規則測試**：FIFO 出庫策略、過期檢查
- ✅ **API 功能測試**：所有 REST API 端點

**測試案例**：
```java
- testStockIn() - 入庫功能
- testStockOut() - 出庫功能
- testGetAllBeverages() - 查詢功能
- testUpdateBeverage() - 更新功能
- testDeleteBeverage() - 刪除功能
```

---

### 2. 非功能測試（Non-Functional Testing）

#### 2.1 效能測試（Performance Testing）

**理論**：測試系統在特定負載下的效能表現。

**專案應用**：
- ✅ **JMeter 壓力測試**：100 個執行緒同時出庫
- ✅ **回應時間測試**：API 回應時間 < 100ms
- ✅ **吞吐量測試**：每秒處理的請求數

**實作範例**：
```java
// JMeter 測試指標
- 平均回應時間：< 100ms
- 成功率：100%
- 吞吐量：> 100 requests/sec
```

#### 2.2 負載測試（Load Testing）

**理論**：測試系統在正常預期負載下的表現。

**專案應用**：
- ✅ **正常負載**：50 個執行緒同時操作
- ✅ **峰值負載**：100 個執行緒同時操作

#### 2.3 壓力測試（Stress Testing）

**理論**：測試系統在超過正常負載下的表現。

**專案應用**：
- ✅ **超負載測試**：200 個執行緒同時操作
- ✅ **資源耗盡測試**：測試系統在資源不足時的表現

#### 2.4 穩定性測試（Stability Testing）

**理論**：測試系統在長時間運行下的穩定性。

**專案應用**：
- ✅ **長時間運行測試**：持續運行 1 小時
- ✅ **記憶體洩漏測試**：監控記憶體使用情況

---

### 3. 結構測試（Structural Testing）

**理論**：基於程式碼內部結構設計測試（白箱測試）。

**專案應用**：
- ✅ **程式碼覆蓋率**：確保關鍵邏輯有測試覆蓋
- ✅ **分支覆蓋**：測試所有 if-else 分支
- ✅ **路徑覆蓋**：測試所有執行路徑

**實作範例**：
```java
// 測試所有分支
if (availableQuantity <= remainingQuantity) {
    // 分支 1：全部出庫
} else {
    // 分支 2：部分出庫
}
```

---

## 測試管理

### 1. 測試計劃（Test Planning）

**理論**：制定測試策略、範圍、資源和時間表。

**專案應用**：

#### 測試策略
- **單元測試**：Service 層、Repository 層
- **整合測試**：Service + Repository、Controller + Service
- **系統測試**：端到端測試、高併發測試
- **驗收測試**：驗收標準驗證

#### 測試範圍
- ✅ 功能測試：所有 CRUD 操作
- ✅ 非功能測試：效能、併發、穩定性
- ✅ 業務規則測試：FIFO 策略、過期檢查

#### 測試資源
- **工具**：JUnit 5、JMeter、Playwright
- **環境**：H2 內存資料庫、測試環境配置
- **時間**：每個功能模組的測試時間估算

---

### 2. 測試監控（Test Monitoring）

**理論**：監控測試進度和測試結果。

**專案應用**：
- ✅ **CI/CD 自動化**：GitHub Actions 自動執行測試
- ✅ **測試報告**：自動產生測試報告（HTML、JSON）
- ✅ **測試結果存儲**：Firestore 儲存測試結果

**實作範例**：
```yaml
# .github/workflows/ci.yml
- name: Run tests
  run: mvn test

- name: Upload test results
  run: python scripts/upload_results.py
```

---

### 3. 風險管理（Risk Management）

**理論**：識別、分析和應對測試風險。

**專案應用**：

#### 高風險區域
1. **出庫操作**（風險：資料不一致）
   - 測試策略：重點測試、高併發測試
   - 緩解措施：悲觀鎖機制

2. **併發場景**（風險：競態條件）
   - 測試策略：壓力測試、併發測試
   - 緩解措施：事務管理、鎖機制

3. **資料庫操作**（風險：資料遺失）
   - 測試策略：整合測試、事務測試
   - 緩解措施：事務回滾、資料備份

---

## 實際應用案例

### 案例 1：高併發出庫測試（應用多個 ISTQB 理論）

**應用的 ISTQB 理論**：
1. ✅ **等價類劃分**：測試不同庫存數量（充足、不足、零庫存）
2. ✅ **邊界值分析**：測試庫存 = 0, 1, 1000 等邊界值
3. ✅ **狀態轉換測試**：在庫 → 出庫 → 已出庫
4. ✅ **系統測試**：完整的端到端測試
5. ✅ **非功能測試**：效能測試、壓力測試
6. ✅ **風險導向測試**：重點測試高風險的出庫操作

**實作**：`BeverageServiceConcurrencyTest.java`

---

### 案例 2：FIFO 出庫策略測試（應用決策表測試）

**應用的 ISTQB 理論**：
1. ✅ **決策表測試**：不同庫存狀態下的出庫決策
2. ✅ **用例測試**：倉庫管理員出庫場景
3. ✅ **業務規則測試**：驗證 FIFO 策略正確性

**實作範例**：
```java
@Test
@DisplayName("FIFO 策略測試 - 優先出庫最早過期的")
void testFIFO_Strategy() {
    // 入庫 3 批不同日期的商品
    // 出庫時應該優先出庫最早過期的
}
```

---

### 案例 3：API 端點測試（應用等價類和邊界值）

**應用的 ISTQB 理論**：
1. ✅ **等價類劃分**：有效/無效輸入
2. ✅ **邊界值分析**：測試數量邊界（0, 1, 最大值）
3. ✅ **功能測試**：驗證 API 功能正確性

**實作範例**：
```java
// 有效輸入
- quantity: 1, 100, 1000

// 無效輸入（邊界值）
- quantity: 0, -1, null, 過大值
```

---

## 📊 ISTQB 理論應用總結

| ISTQB 理論 | 專案應用 | 實作位置 |
|-----------|---------|---------|
| **測試基礎原則** | 7 個原則全部應用 | 所有測試案例 |
| **等價類劃分** | 有效/無效輸入分類 | Service 層測試 |
| **邊界值分析** | 庫存、日期邊界測試 | 單元測試 |
| **決策表測試** | 出庫業務規則 | Service 層測試 |
| **狀態轉換測試** | 庫存狀態轉換 | 整合測試 |
| **用例測試** | 倉庫管理員場景 | E2E 測試 |
| **單元測試** | Service、Repository 方法 | JUnit 測試 |
| **整合測試** | Service + Repository | Spring Boot Test |
| **系統測試** | 端到端測試 | JMeter + JUnit |
| **驗收測試** | 驗收標準驗證 | 高併發測試 |
| **效能測試** | 回應時間、吞吐量 | JMeter |
| **壓力測試** | 高併發場景 | JMeter + JUnit |
| **風險導向測試** | 重點測試出庫功能 | 所有測試 |

---

## 🎯 下一步建議

### 擴充測試案例（應用更多 ISTQB 理論）

1. **等價類劃分測試**
   - 建立 `BeverageServiceEquivalencePartitioningTest.java`

2. **邊界值測試**
   - 建立 `BeverageServiceBoundaryValueTest.java`

3. **決策表測試**
   - 建立 `BeverageServiceDecisionTableTest.java`

4. **狀態轉換測試**
   - 建立 `BeverageStateTransitionTest.java`

5. **用例測試**
   - 建立 `BeverageUseCaseTest.java`

---

## 📚 參考資源

- [ISTQB 官方網站](https://www.istqb.org/)
- [ISTQB Foundation Level 大綱](https://www.istqb.org/certification-path-root/foundation-level.html)
- [測試設計技術](https://www.istqb.org/downloads/send/51-istqb-syllabus-2018/218-ctfl-2018-syllabus.html)

---

**這個專案完美展示了 ISTQB 測試理論在實際專案中的應用！** 🎉

