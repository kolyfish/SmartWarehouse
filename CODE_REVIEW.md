### SmartWarehouse 程式碼審查報告

#### **三大關鍵問題 (Top 3 Critical Issues)**

1.  **不恰當的例外處理 (Improper Exception Handling)**:
    *   **問題**: Service 層在處理業務邏輯錯誤時（如「庫存不足」），普遍拋出通用的 `RuntimeException`。這讓 Controller 層和 API 的呼叫者難以針對不同錯誤類型做精確處理。
    *   **建議**: 建立具體的自定義例外（如 `InsufficientStockException`），繼承自 `RuntimeException`。這能讓錯誤處理更精確，API 回應的語意也更清晰。
    *   **範例**:
        ```java
        // 自定義例外
        public class InsufficientStockException extends RuntimeException {
            public InsufficientStockException(String message) {
                super(message);
            }
        }
        ```

2.  **不安全的 CORS 設定**:
    *   **問題**: Spring Boot (`BeverageController.java`) 和 FastAPI (`api/main.py`) 的 CORS 設定都允許所有來源 (`*`)。在生產環境中，這會帶來嚴重的安全風險，因為任何網站都能對你的 API 發出請求。
    *   **建議**: 將允許的來源限制在前端應用的特定網域。使用設定檔（如 `application.properties`）來管理不同環境的 CORS 設定。
    *   **範例**:
        ```java
        // 在 BeverageController.java
        @CrossOrigin(origins = "${app.cors.allowed-origins}")

        // 在 application.properties
        // app.cors.allowed-origins=http://localhost:3000,https://your-frontend-domain.com
        ```

3.  **`stockOut` 方法中潛在的併發問題**:
    *   **問題**: `BeverageService.stockOut` 方法雖然對查詢加了悲觀鎖，但在迴圈中又對每一筆資料單獨呼叫 `findByIdWithLock` 重新加鎖。這種作法效率低，且在複雜情況下可能增加死鎖（Deadlock）的風險。
    *   **建議**: 重構 `stockOut` 邏輯，讓整個庫存扣減在一個更穩固的資料庫交易中完成。初始查詢就應鎖定所有相關的資料行，並在這些鎖定的資料上直接進行更新，避免在迴圈中重複查詢和加鎖。

#### **三大重構建議 (Top 3 Refactoring Suggestions)**

1.  **簡化 `stockOut` 邏輯**:
    *   **建議**: 目前的 `stockOut` 方法邏輯較為複雜。可以先計算總可用庫存，再遍歷批次扣除數量。這能避免在迴圈中重複查詢資料庫，讓程式碼更簡潔、高效。

2.  **使用 MapStruct 進行 DTO 轉換**:
    *   **建議**: `BeverageService` 中的 `convertToDTO` 是手動轉換。引入 MapStruct 這個工具可以自動化這個過程，減少重複的樣板程式碼，並降低出錯機率。
    *   **範例**:
        ```java
        // MapStruct Mapper 介面範例
        @Mapper(componentModel = "spring")
        public interface BeverageMapper {
            BeverageDTO toDto(Beverage beverage);
            Beverage toEntity(BeverageDTO dto);
        }
        ```

3.  **集中管理測試資料的生成**:
    *   **建議**: 測試案例中多次重複建立 `StockInRequestDTO` 等物件。可以建立一個測試資料工廠（Test Data Factory）或使用 Builder 模式來集中產生測試資料，讓測試程式碼更乾淨、易於維護。
    *   **範例**:
        ```java
        // 測試資料工廠範例
        public class TestDataFactory {
            public static StockInRequestDTO createStockInRequest(String name, int quantity, LocalDate expiryDate) {
                // ... 建立物件的邏輯
            }
        }
        ```

#### **安全性考量 (Security Concerns)**

1.  **過於寬鬆的 CORS 策略**: 如前述，這是最需要被修正的安全問題。
2.  **錯誤訊息可能洩漏過多資訊**: 部分錯誤訊息（如「只能報廢隔離區中的商品」）揭露了內部業務邏輯。建議對外提供通用的錯誤提示，並在伺服器端留下詳細的日誌。
3.  **缺乏 CSRF 保護**: 應用程式似乎未明確設定 CSRF 保護。雖然現代基於 Token 的驗證（如 JWT）能緩解此問題，但若使用 session-cookie 機制，則 CSRF 保護至關重要。

#### **CI/CD 準備度評分 (CI/CD Readiness Score)**

**分數: 8/10**

*   **優點**:
    *   專案已具備非常完整的 CI/CD 工作流程（`.github/workflows/ci-cd.yml`），涵蓋了自動化測試、建構、Docker 映像打包等。
    *   `Dockerfile` 採用多階段建構，效率很高。
    *   `docker-compose.yml` 配置完整，並包含健康檢查，是很好的實踐。
    *   測試報告會被自動產生並上傳。

*   **可改進之處**:
    *   整合測試仍依賴 H2 記憶體資料庫。在 CI/CD 流程中，改用 Docker 啟動一個真實的資料庫（如專案中已註解掉的 PostgreSQL）進行測試會更貼近生產環境。
    *   Playwright 前端測試依賴本地啟動的後端服務，這在 CI/CD 中可能不穩定。工作流程應使用更可靠的方式（如 `healthcheck`）確保後端服務完全就緒後再執行前端測試。
