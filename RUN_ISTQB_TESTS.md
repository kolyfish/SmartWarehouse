# 🧪 執行 ISTQB 測試案例

## 📋 測試檔案總覽

本專案包含以下 ISTQB 測試理論的實作：

| 測試檔案 | ISTQB 理論 | 測試級別 |
|---------|-----------|---------|
| `BeverageServiceConcurrencyTest.java` | 高併發測試、驗收測試 | 系統測試 |
| `BeverageServiceEquivalencePartitioningTest.java` | 等價類劃分 | 單元測試 |
| `BeverageServiceBoundaryValueTest.java` | 邊界值分析 | 單元測試 |
| `BeverageServiceDecisionTableTest.java` | 決策表測試 | 單元測試 |
| `BeverageStateTransitionTest.java` | 狀態轉換測試 | 整合測試 |
| `BeverageUseCaseTest.java` | 用例測試 | 系統測試 |

---

## 🚀 執行測試

### 執行所有 ISTQB 測試

```bash
cd backend
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
mvn test
```

### 執行特定測試類別

```bash
# 等價類劃分測試
mvn test -Dtest=BeverageServiceEquivalencePartitioningTest

# 邊界值分析測試
mvn test -Dtest=BeverageServiceBoundaryValueTest

# 決策表測試
mvn test -Dtest=BeverageServiceDecisionTableTest

# 狀態轉換測試
mvn test -Dtest=BeverageStateTransitionTest

# 用例測試
mvn test -Dtest=BeverageUseCaseTest

# 高併發測試
mvn test -Dtest=BeverageServiceConcurrencyTest
```

### 執行特定測試方法

```bash
# 執行特定測試方法
mvn test -Dtest=BeverageServiceEquivalencePartitioningTest#testStockIn_ValidQuantity_NormalRange
```

---

## 📊 測試結果解讀

### 等價類劃分測試結果

```
✅ 有效等價類測試通過
✅ 無效等價類測試通過（正確拋出異常）
```

### 邊界值分析測試結果

```
✅ 最小值邊界測試通過
✅ 最大值邊界測試通過
✅ 邊界值+1/-1 測試通過
```

### 決策表測試結果

```
✅ 規則 1：成功出庫
✅ 規則 2：庫存不足
✅ 規則 3：無庫存
✅ 規則 4：已過期
```

### 狀態轉換測試結果

```
✅ 入庫 → 在庫
✅ 在庫 → 出庫 → 已出庫
✅ 在庫 → 即將過期
✅ 在庫 → 已過期
```

---

## 📈 測試覆蓋率

執行測試覆蓋率分析：

```bash
# 需要加入 jacoco 插件（可選）
mvn clean test jacoco:report
```

---

## 🎯 ISTQB 理論對照

### 測試設計技術

| 技術 | 測試檔案 | 測試案例數 |
|------|---------|-----------|
| 等價類劃分 | `BeverageServiceEquivalencePartitioningTest` | 10+ |
| 邊界值分析 | `BeverageServiceBoundaryValueTest` | 10+ |
| 決策表測試 | `BeverageServiceDecisionTableTest` | 5+ |
| 狀態轉換測試 | `BeverageStateTransitionTest` | 6+ |
| 用例測試 | `BeverageUseCaseTest` | 5+ |

### 測試級別

| 級別 | 測試檔案 | 說明 |
|------|---------|------|
| 單元測試 | 等價類、邊界值、決策表 | 測試單一方法 |
| 整合測試 | 狀態轉換 | 測試多個組件整合 |
| 系統測試 | 高併發、用例測試 | 測試完整系統 |

---

## 💡 測試最佳實踐

1. **測試命名**：使用 `@DisplayName` 清楚描述測試目的
2. **測試組織**：按照 ISTQB 理論分類組織測試
3. **測試獨立性**：每個測試都是獨立的，使用 `@BeforeEach` 準備資料
4. **測試驗證**：使用明確的斷言驗證預期結果

---

**詳細理論說明請參考：[ISTQB_TEST_THEORY.md](../ISTQB_TEST_THEORY.md)**

