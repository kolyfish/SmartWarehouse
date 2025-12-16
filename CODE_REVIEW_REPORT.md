# SmartWarehouse 程式碼審查報告

**審查日期**：2024-12-16  
**審查工具**：Gemini CLI (Will 保哥風格)  
**審查範圍**：完整專案程式碼

---

## 🔴 三大關鍵問題 (Top 3 Critical Issues)

### 1. 不恰當的例外處理 (Improper Exception Handling)

**問題**：
Service 層在處理業務邏輯錯誤時（如「庫存不足」），普遍拋出通用的 `RuntimeException`。這讓 Controller 層和 API 的呼叫者難以針對不同錯誤類型做精確處理。

**影響**：
- API 回應的語意不清晰
- 前端無法根據錯誤類型做不同處理
- 測試時難以精確驗證特定錯誤

**建議**：
建立具體的自定義例外，繼承自 `RuntimeException`。這能讓錯誤處理更精確，API 回應的語意也更清晰。

**範例**：
```java
// 自定義例外
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}

// 在 BeverageService 中使用
if (availableQuantity < request.getQuantity()) {
    throw new InsufficientStockException(
        String.format("庫存不足，可用庫存：%d，請求數量：%d", 
                     availableQuantity, request.getQuantity())
    );
}
```

**建議的例外類別**：
- `InsufficientStockException` - 庫存不足
- `BeverageNotFoundException` - 飲料不存在
- `StockInLimitExceededException` - 超過入庫限制
- `InvalidBeverageStatusException` - 無效的商品狀態

---

### 2. 不安全的 CORS 設定

**問題**：
Spring Boot (`BeverageController.java`) 和 FastAPI (`api/main.py`) 的 CORS 設定都允許所有來源 (`*`)。在生產環境中，這會帶來嚴重的安全風險，因為任何網站都能對你的 API 發出請求。

**影響**：
- 任何網站都可以呼叫你的 API
- 可能導致 CSRF 攻擊
- 資料可能被惡意網站竊取

**建議**：
將允許的來源限制在前端應用的特定網域。使用設定檔（如 `application.properties`）來管理不同環境的 CORS 設定。

**範例**：
```java
// 在 BeverageController.java
@CrossOrigin(origins = "${app.cors.allowed-origins}")

// 在 application.properties
app.cors.allowed-origins=http://localhost:3000,https://your-frontend-domain.com

// 在 application-prod.properties
app.cors.allowed-origins=https://smartwarehouse.example.com
```

---

### 3. `stockOut` 方法中潛在的併發問題

**問題**：
`BeverageService.stockOut` 方法雖然對查詢加了悲觀鎖，但在迴圈中又對每一筆資料單獨呼叫 `findByIdWithLock` 重新加鎖。這種作法效率低，且在複雜情況下可能增加死鎖（Deadlock）的風險。

**影響**：
- 效能問題：重複查詢和加鎖
- 潛在的死鎖風險
- 程式碼複雜度高

**建議**：
重構 `stockOut` 邏輯，讓整個庫存扣減在一個更穩固的資料庫交易中完成。初始查詢就應鎖定所有相關的資料行，並在這些鎖定的資料上直接進行更新，避免在迴圈中重複查詢和加鎖。

**改進方向**：
1. 在單一查詢中鎖定所有需要的資料
2. 在記憶體中計算扣減邏輯
3. 批次更新資料庫

---

## 🔧 三大重構建議 (Top 3 Refactoring Suggestions)

### 1. 簡化 `stockOut` 邏輯

**建議**：
目前的 `stockOut` 方法邏輯較為複雜。可以先計算總可用庫存，再遍歷批次扣除數量。這能避免在迴圈中重複查詢資料庫，讓程式碼更簡潔、高效。

**改進方向**：
- 先計算總可用庫存
- 驗證是否足夠
- 批次扣除，減少資料庫查詢次數

---

### 2. 使用 MapStruct 進行 DTO 轉換

**建議**：
`BeverageService` 中的 `convertToDTO` 是手動轉換。引入 MapStruct 這個工具可以自動化這個過程，減少重複的樣板程式碼，並降低出錯機率。

**範例**：
```java
// MapStruct Mapper 介面範例
@Mapper(componentModel = "spring")
public interface BeverageMapper {
    BeverageDTO toDto(Beverage beverage);
    Beverage toEntity(BeverageDTO dto);
    List<BeverageDTO> toDtoList(List<Beverage> beverages);
}

// 在 BeverageService 中使用
@Autowired
private BeverageMapper beverageMapper;

public BeverageDTO convertToDTO(Beverage beverage) {
    return beverageMapper.toDto(beverage);
}
```

**優點**：
- 減少樣板程式碼
- 編譯時生成，效能好
- 類型安全

---

### 3. 集中管理測試資料的生成

**建議**：
測試案例中多次重複建立 `StockInRequestDTO` 等物件。可以建立一個測試資料工廠（Test Data Factory）或使用 Builder 模式來集中產生測試資料，讓測試程式碼更乾淨、易於維護。

**範例**：
```java
// 測試資料工廠範例
public class TestDataFactory {
    public static StockInRequestDTO createStockInRequest(
        String name, 
        int quantity, 
        LocalDate expiryDate
    ) {
        StockInRequestDTO dto = new StockInRequestDTO();
        dto.setName(name);
        dto.setQuantity(quantity);
        dto.setProductionDate(LocalDate.now());
        dto.setExpiryDate(expiryDate);
        return dto;
    }
    
    public static StockInRequestDTO createDefaultStockInRequest() {
        return createStockInRequest(
            "測試飲料",
            100,
            LocalDate.now().plusYears(1)
        );
    }
}

// 在測試中使用
StockInRequestDTO request = TestDataFactory.createDefaultStockInRequest();
```

**優點**：
- 減少重複程式碼
- 易於維護
- 測試資料更一致

---

## 🔒 安全性考量 (Security Concerns)

### 1. 過於寬鬆的 CORS 策略

**問題**：如前述，這是最需要被修正的安全問題。

**優先級**：🔴 高

**建議**：立即修正 CORS 設定，限制允許的來源。

---

### 2. 錯誤訊息可能洩漏過多資訊

**問題**：
部分錯誤訊息（如「只能報廢隔離區中的商品」）揭露了內部業務邏輯。建議對外提供通用的錯誤提示，並在伺服器端留下詳細的日誌。

**範例**：
```java
// ❌ 不好的做法
throw new RuntimeException("只能報廢隔離區中的商品，當前狀態: " + beverage.getStatus());

// ✅ 好的做法
logger.warn("嘗試報廢非隔離區商品，ID: {}, 狀態: {}", id, beverage.getStatus());
throw new InvalidOperationException("無法執行此操作");
```

---

### 3. 缺乏 CSRF 保護

**問題**：
應用程式似乎未明確設定 CSRF 保護。雖然現代基於 Token 的驗證（如 JWT）能緩解此問題，但若使用 session-cookie 機制，則 CSRF 保護至關重要。

**建議**：
- 如果使用 JWT，確保 Token 儲存在安全的 HttpOnly Cookie 中
- 如果使用 Session，啟用 Spring Security 的 CSRF 保護

---

## 📊 CI/CD 準備度評分 (CI/CD Readiness Score)

### 分數：8/10

#### ✅ 優點：

1. **完整的 CI/CD 工作流程**
   - `.github/workflows/ci-cd.yml` 涵蓋了自動化測試、建構、Docker 映像打包等
   - 多階段測試（單元測試、整合測試、E2E 測試）

2. **Docker 配置優秀**
   - Dockerfile 採用多階段建構，效率很高
   - docker-compose.yml 配置完整，並包含健康檢查

3. **測試報告自動化**
   - 測試報告會被自動產生並上傳
   - 支援多種測試類型（JUnit、Playwright、性能測試）

#### ⚠️ 可改進之處：

1. **整合測試環境**
   - 整合測試仍依賴 H2 記憶體資料庫
   - 建議：在 CI/CD 流程中，改用 Docker 啟動一個真實的資料庫（如專案中已註解掉的 PostgreSQL）進行測試會更貼近生產環境

2. **前端測試穩定性**
   - Playwright 前端測試依賴本地啟動的後端服務，這在 CI/CD 中可能不穩定
   - 建議：工作流程應使用更可靠的方式（如 healthcheck）確保後端服務完全就緒後再執行前端測試

3. **性能測試整合**
   - 目前性能測試（k6）未整合到 CI/CD 流程中
   - 建議：可以作為可選階段，在重大變更後手動觸發

---

## 📋 改進優先級

### 🔴 高優先級（立即處理）

1. **修正 CORS 設定** - 安全性問題
2. **建立自定義例外** - 提升程式碼品質
3. **優化 stockOut 方法** - 效能和穩定性

### 🟡 中優先級（近期處理）

1. **引入 MapStruct** - 減少樣板程式碼
2. **建立測試資料工廠** - 提升測試可維護性
3. **改進錯誤訊息** - 安全性

### 🟢 低優先級（長期優化）

1. **整合測試改用真實資料庫** - 提升測試真實性
2. **CSRF 保護** - 如果未來需要 Session 機制
3. **性能測試 CI/CD 整合** - 自動化性能監控

---

## 📝 總結

SmartWarehouse 專案整體架構良好，CI/CD 流程完整，但在以下方面需要改進：

1. **安全性**：CORS 設定過於寬鬆，需要立即修正
2. **程式碼品質**：例外處理可以更精確，使用自定義例外
3. **效能**：stockOut 方法可以優化，減少重複查詢

這些改進將使專案更加專業、安全、高效。

---

**審查工具**：Gemini CLI  
**審查風格**：Will 保哥（微軟 MVP 級別）  
**審查日期**：2024-12-16

