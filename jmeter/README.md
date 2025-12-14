# 📊 JMeter 高併發測試說明

## 🎯 測試目標

驗證智慧倉庫系統在高併發場景下的資料一致性：

1. **驗證悲觀鎖機制**：DB Lock（Pessimistic Lock）是否生效
2. **資料一致性**：庫存扣減數量必須與訂單成功數精確匹配
3. **負庫存防護**：不能出現負庫存

## 📋 測試場景

### 測試參數

- **執行緒數**：50-100 個（可調整）
- **同步機制**：Synchronizing Timer（確保所有執行緒在同一毫秒內執行）
- **操作類型**：對同一個熱門商品（礦泉水）進行出庫操作
- **每個執行緒出庫數量**：5 瓶

### 測試流程

1. **初始化庫存**：入庫 1000 瓶礦泉水
2. **高併發出庫**：100 個執行緒同時出庫，每個出庫 5 瓶
3. **驗證結果**：查詢最終庫存統計

## 🚀 執行步驟

### 1. 啟動 Spring Boot 服務

```bash
cd backend
mvn spring-boot:run
```

確保服務運行在 `http://localhost:8080`

### 2. 執行 JMeter 測試

#### 方法 1：使用 JMeter GUI

```bash
# 開啟 JMeter
jmeter

# 載入測試腳本
File > Open > jmeter/SmartWarehouse_Concurrency_Test.jmx

# 執行測試
Run > Start
```

#### 方法 2：使用命令列（無 GUI）

```bash
# 執行測試並產生報告
jmeter -n -t jmeter/SmartWarehouse_Concurrency_Test.jmx \
  -l jmeter/results.jtl \
  -e -o jmeter/report
```

### 3. 查看測試結果

- **GUI 模式**：在 "View Results Tree" 和 "Summary Report" 查看
- **命令列模式**：開啟 `jmeter/report/index.html`

## 📊 驗收標準

### ✅ 必須通過的檢查

1. **庫存扣減精確匹配**
   ```
   成功出庫的執行緒數 × 每執行緒出庫數量 = 實際庫存減少數量
   ```

2. **無負庫存**
   ```
   最終庫存 >= 0
   ```

3. **資料一致性**
   ```
   初始庫存 - 成功出庫總數 = 最終庫存
   ```

### 📈 預期結果範例

```
初始庫存: 1000 瓶
併發執行緒數: 100
每個執行緒出庫: 5 瓶
預期總出庫: 500 瓶

實際結果：
- 成功出庫執行緒: 100
- 失敗執行緒: 0
- 實際出庫總數: 500
- 最終庫存: 500
- 庫存扣減匹配: ✅
- 無負庫存: ✅
```

## 🔧 調整測試參數

在 JMeter GUI 中修改 "User Defined Variables"：

- `THREAD_COUNT`: 執行緒數（預設 100）
- `QUANTITY_PER_THREAD`: 每個執行緒出庫數量（預設 5）
- `BASE_URL`: API 基礎 URL（預設 http://localhost:8080）

## 📝 測試報告解讀

### Summary Report 關鍵指標

- **Samples**: 總請求數
- **Average**: 平均回應時間
- **Min/Max**: 最小/最大回應時間
- **Error %**: 錯誤率（應該為 0% 或接近 0%）
- **Throughput**: 每秒處理的請求數

### 驗證資料一致性

執行測試後，手動查詢 API 驗證：

```bash
# 查詢庫存統計
curl http://localhost:8080/api/beverages/statistics

# 查詢所有飲料
curl http://localhost:8080/api/beverages
```

檢查：
- 總庫存數量是否正確
- 是否有負庫存
- 庫存扣減是否與成功請求數匹配

## 🐛 故障排除

### 問題 1：測試失敗率高

**可能原因**：
- Spring Boot 服務未啟動
- 資料庫連線問題
- 執行緒數過多，超過系統負載

**解決方案**：
- 確認服務運行狀態
- 降低執行緒數
- 增加 JVM 記憶體：`export JAVA_OPTS="-Xmx2g"`

### 問題 2：出現負庫存

**可能原因**：
- 悲觀鎖未正確實作
- 事務隔離級別設定錯誤

**解決方案**：
- 檢查 `BeverageRepository` 中的 `@Lock(LockModeType.PESSIMISTIC_WRITE)`
- 確認 `@Transactional` 註解正確

### 問題 3：JMeter 無法連線

**可能原因**：
- 服務未啟動
- 端口被占用
- 防火牆阻擋

**解決方案**：
```bash
# 檢查服務狀態
curl http://localhost:8080/api/beverages/statistics

# 檢查端口
lsof -i :8080
```

## 📚 相關文件

- [JMeter 官方文件](https://jmeter.apache.org/usermanual/)
- [Spring Data JPA 鎖機制](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#locking)
- [TDD 測試案例](../backend/src/test/java/com/beveragewarehouse/service/BeverageServiceConcurrencyTest.java)

