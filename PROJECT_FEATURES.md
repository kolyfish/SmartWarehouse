# 🎯 專案功能總覽

## 📊 SmartWarehouse 智慧倉庫系統

### 核心功能

#### 1. 飲料庫存管理
- ✅ 入庫管理（新增庫存）
- ✅ 出庫管理（FIFO 策略）
- ✅ CRUD 操作（增刪查改）
- ✅ 過期檢查與提醒
- ✅ 庫存統計

#### 2. 高併發資料一致性保證 ⭐
- ✅ **悲觀鎖機制**（Pessimistic Locking）
- ✅ **事務管理**（Transactional）
- ✅ **負庫存防護**
- ✅ **精確匹配驗證**

#### 3. 測試與驗證
- ✅ **TDD 測試**（測試驅動開發）
- ✅ **JMeter 壓力測試**（高併發驗證）
- ✅ **Playwright 自動化測試**
- ✅ **CI/CD 自動化**

---

## 🔒 資料一致性機制

### 悲觀鎖實作

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT b FROM Beverage b WHERE ...")
List<Beverage> findAvailableBeveragesByNameOrderByExpiryWithLock(...);
```

### 驗收標準

| 標準 | 說明 | 驗證方式 |
|------|------|----------|
| **庫存扣減精確匹配** | 庫存扣減數量 = 訂單成功數 × 每單數量 | TDD + JMeter |
| **無負庫存** | 最終庫存 >= 0 | 自動檢查 |
| **資料一致性** | 初始庫存 - 成功出庫總數 = 最終庫存 | 統計驗證 |

---

## 📈 測試場景

### TDD 測試（JUnit）

**場景**：100 個執行緒同時出庫，每個出庫 5 瓶

**驗證**：
- ✅ 成功出庫執行緒數 × 5 = 實際出庫總數
- ✅ 最終庫存 >= 0
- ✅ 無資料不一致

### JMeter 壓力測試

**場景**：50-100 個執行緒，使用 Synchronizing Timer 同時執行

**驗證**：
- ✅ 成功率 100%
- ✅ 平均回應時間 < 100ms
- ✅ 資料一致性通過

---

## 🛠️ 技術亮點

### 後端技術
- Spring Boot 3.2.0
- Spring Data JPA
- H2 Database（內存資料庫）
- 悲觀鎖機制
- 事務管理

### 測試技術
- JUnit 5（TDD 測試）
- JMeter（壓力測試）
- Playwright（自動化測試）
- FastAPI（測試結果 API）

### DevOps
- GitHub Actions（CI/CD）
- Firestore（測試結果存儲）

---

## 💼 面試重點

### 可以強調的技術能力

1. **高併發處理**
   - 悲觀鎖機制實作
   - 資料一致性保證
   - 負庫存防護

2. **測試專業**
   - TDD 測試驅動開發
   - JMeter 壓力測試
   - 自動化測試

3. **全端能力**
   - Spring Boot 後端開發
   - SQL 資料庫操作
   - RESTful API 設計

4. **DevOps 實踐**
   - CI/CD 自動化
   - 雲端服務整合
   - 成本優化

---

## 📚 相關文件

- [README.md](./README.md) - 專案總覽
- [QUICKSTART.md](./QUICKSTART.md) - 快速開始
- [SETUP.md](./SETUP.md) - 詳細設定
- [API_EXAMPLES.md](./API_EXAMPLES.md) - API 使用範例
- [jmeter/README.md](./jmeter/README.md) - JMeter 測試說明

