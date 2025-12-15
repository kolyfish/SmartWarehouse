# 📖 ISTQB 測試理論快速參考

## 🎯 SmartWarehouse 專案中的 ISTQB 應用

### 測試設計技術

| ISTQB 技術 | 測試檔案 | 關鍵測試案例 |
|-----------|---------|-------------|
| **等價類劃分** | `BeverageServiceEquivalencePartitioningTest` | 有效/無效輸入分類 |
| **邊界值分析** | `BeverageServiceBoundaryValueTest` | 0, 1, 100, 庫存邊界 |
| **決策表測試** | `BeverageServiceDecisionTableTest` | 4 個業務規則組合 |
| **狀態轉換測試** | `BeverageStateTransitionTest` | 6 個狀態轉換 |
| **用例測試** | `BeverageUseCaseTest` | 5 個使用者場景 |

### 測試級別

| 級別 | 測試檔案 | 測試對象 |
|------|---------|---------|
| **單元測試** | 等價類、邊界值、決策表 | Service 方法 |
| **整合測試** | 狀態轉換 | Service + Repository |
| **系統測試** | 高併發、用例測試 | 完整系統 |
| **驗收測試** | 高併發測試 | 驗收標準 |

### 測試類型

| 類型 | 實作 | 工具 |
|------|------|------|
| **功能測試** | 所有測試檔案 | JUnit 5 |
| **效能測試** | JMeter 測試 | Apache JMeter |
| **壓力測試** | 高併發測試 | JUnit + JMeter |

---

## 🚀 快速執行

```bash
# 執行所有 ISTQB 測試
cd backend
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
mvn test

# 執行特定理論測試
mvn test -Dtest=BeverageServiceEquivalencePartitioningTest  # 等價類
mvn test -Dtest=BeverageServiceBoundaryValueTest          # 邊界值
mvn test -Dtest=BeverageServiceDecisionTableTest           # 決策表
mvn test -Dtest=BeverageStateTransitionTest                # 狀態轉換
mvn test -Dtest=BeverageUseCaseTest                        # 用例測試
mvn test -Dtest=BeverageServiceConcurrencyTest            # 高併發
```

---

## 📚 詳細文件

- [ISTQB 測試理論詳細說明](./ISTQB_TEST_THEORY.md)
- [ISTQB 應用總結](./ISTQB_APPLICATION_SUMMARY.md)
- [執行 ISTQB 測試指南](./RUN_ISTQB_TESTS.md)

