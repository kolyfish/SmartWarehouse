# ⚡ 5 分鐘快速開始

## 🎯 目標

在 5 分鐘內完成設定並執行第一次測試！

---

## 步驟 1：啟動 Spring Boot 後端（1 分鐘）

```bash
cd backend
mvn spring-boot:run
```

**等待服務啟動**（看到 "Started BeverageWarehouseApplication" 訊息）

**驗證服務運行**：
```bash
curl http://localhost:8080/api/beverages/statistics
```

應該看到 JSON 回應。

---

## 步驟 2：執行快速 API 測試（1 分鐘）

```bash
# 回到專案根目錄
cd ..

# 執行快速測試腳本
./QUICK_TEST.sh
```

這個腳本會：
1. ✅ 入庫 100 瓶礦泉水
2. ✅ 入庫 50 瓶礦泉水（不同日期）
3. ✅ 查詢所有庫存
4. ✅ 查看統計資料
5. ✅ 出庫 30 瓶（系統自動選擇最早過期的）
6. ✅ 再次查詢庫存
7. ✅ 檢查即將過期的飲料

---

## 步驟 3：執行 TDD 高併發測試（2 分鐘）

```bash
cd backend
mvn test -Dtest=BeverageServiceConcurrencyTest
```

**測試內容**：
- 模擬 100 個執行緒同時出庫
- 驗證悲觀鎖機制
- 驗證資料一致性（無負庫存）

**預期結果**：
```
✅ 高併發測試通過
✅ 庫存扣減精確匹配
✅ 無負庫存
```

---

## 步驟 4：執行 JMeter 壓力測試（可選，1 分鐘）

### 前置需求

1. **安裝 JMeter**：
   ```bash
   # macOS
   brew install jmeter
   
   # 或下載：https://jmeter.apache.org/download_jmeter.cgi
   ```

2. **啟動 Spring Boot 服務**（如果還沒啟動）

### 執行測試

```bash
# 使用 GUI 模式
jmeter -t jmeter/SmartWarehouse_Concurrency_Test.jmx

# 或使用命令列（無 GUI）
jmeter -n -t jmeter/SmartWarehouse_Concurrency_Test.jmx \
  -l jmeter/results.jtl \
  -e -o jmeter/report
```

### 查看結果

- **GUI 模式**：在 "Summary Report" 查看統計
- **命令列模式**：開啟 `jmeter/report/index.html`

---

## ✅ 完成！

你現在已經：

1. ✅ 啟動了 Spring Boot 後端服務
2. ✅ 執行了基本 API 測試
3. ✅ 驗證了高併發資料一致性（TDD 測試）
4. ✅ （可選）執行了 JMeter 壓力測試

---

## 🎯 下一步

### 查看詳細文件

- **API 使用範例**：`cat API_EXAMPLES.md`
- **JMeter 測試說明**：`cat jmeter/README.md`
- **完整設定指南**：`cat SETUP.md`

### 手動測試 API

```bash
# 入庫
curl -X POST http://localhost:8080/api/beverages/stock-in \
  -H "Content-Type: application/json" \
  -d '{
    "name": "礦泉水",
    "quantity": 100,
    "productionDate": "2024-01-01",
    "expiryDate": "2025-01-01"
  }'

# 查詢
curl http://localhost:8080/api/beverages

# 出庫
curl -X POST http://localhost:8080/api/beverages/stock-out \
  -H "Content-Type: application/json" \
  -d '{
    "name": "礦泉水",
    "quantity": 50
  }'
```

### 查看 H2 資料庫

1. 開啟瀏覽器：http://localhost:8080/h2-console
2. JDBC URL: `jdbc:h2:mem:beveragewarehouse`
3. Username: `sa`
4. Password: （空白）
5. 執行 SQL：`SELECT * FROM beverages;`

---

## 🐛 遇到問題？

### 問題 1：Maven 找不到

**解決**：
```bash
# macOS
brew install maven

# 或下載：https://maven.apache.org/download.cgi
```

### 問題 2：端口 8080 被占用

**解決**：
```bash
# 查看占用端口的程序
lsof -i :8080

# 或修改 application.properties 中的端口
# server.port=8081
```

### 問題 3：測試失敗

**檢查**：
1. Spring Boot 服務是否運行？
2. 資料庫連線是否正常？
3. 查看測試日誌中的錯誤訊息

---

## 💡 提示

- **快速重啟服務**：在終端按 `Ctrl+C` 停止，然後重新執行 `mvn spring-boot:run`
- **查看 API 文件**：服務啟動後，訪問 http://localhost:8080/api/beverages
- **測試腳本位置**：`./QUICK_TEST.sh` 在專案根目錄

---

**準備好了嗎？開始測試吧！** 🚀
