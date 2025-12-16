# 🎨 SmartWarehouse 前端介面

## 📋 簡介

這是一個簡單但功能完整的前端介面，用於查看和管理 SmartWarehouse 智慧倉庫系統的庫存。

## ✨ 功能特色

- ✅ **即時庫存統計** - 顯示總庫存、過期數量、即將過期數量
- ✅ **庫存列表查看** - 查看所有庫存商品
- ✅ **過期商品管理** - 查看已過期和即將過期的商品
- ✅ **隔離區管理** - 查看隔離區中的商品
- ✅ **報廢記錄** - 查看已報廢的商品記錄
- ✅ **響應式設計** - 支援桌面和行動裝置
- ✅ **現代化 UI** - 使用 Tailwind CSS，美觀易用

## 🚀 快速開始

### 方法 1：直接開啟 HTML 檔案

1. 確保 Spring Boot 服務正在運行（`http://localhost:8080`）
2. 在瀏覽器中開啟 `frontend/index.html`

### 方法 2：使用簡單 HTTP 伺服器

```bash
# 使用 Python 內建伺服器
cd frontend
python3 -m http.server 8000

# 然後在瀏覽器開啟
# http://localhost:8000/index.html
```

### 方法 3：使用 Node.js http-server

```bash
# 安裝 http-server（如果還沒安裝）
npm install -g http-server

# 啟動伺服器
cd frontend
http-server -p 8000

# 然後在瀏覽器開啟
# http://localhost:8000/index.html
```

## 📱 使用說明

### 1. 設定 API URL

- 預設 API URL 為 `http://localhost:8080/api/beverages`
- 如果需要修改，在頁面上方的輸入框中輸入新的 URL，然後點擊「更新」

### 2. 查看庫存統計

- 頁面載入時會自動載入統計資料
- 點擊「🔄 重新整理統計」按鈕可以手動更新

### 3. 查看不同類型的庫存

- **📋 查看所有庫存** - 顯示所有庫存商品
- **⚠️ 查看已過期** - 顯示已過期的商品
- **⏰ 查看即將過期** - 顯示 7 天內會過期的商品
- **🚧 查看隔離區** - 顯示隔離區中的商品
- **🗑️ 查看已報廢** - 顯示已報廢的商品記錄

## 🎨 介面說明

### 統計卡片

- **總庫存項目** - 顯示庫存中的商品項目總數
- **總庫存數量** - 顯示所有商品的總數量
- **已過期數量** - 顯示已過期的商品數量
- **即將過期數量** - 顯示 7 天內會過期的商品數量

### 商品卡片

每個商品卡片顯示：
- 商品名稱和 ID
- 商品狀態（正常/隔離中/已報廢）
- 數量
- 生產日期
- 有效期限
- 過期狀態
- 距離過期的天數
- 報廢原因（如果已報廢）

### 顏色標示

- 🟢 **綠色** - 正常商品
- 🟠 **橘色** - 即將過期（7 天內）
- 🔴 **紅色** - 已過期
- 🟡 **黃色** - 隔離中
- ⚫ **灰色** - 已報廢

## 🔧 技術細節

- **HTML5** - 結構
- **Tailwind CSS** - 樣式（透過 CDN）
- **Vanilla JavaScript** - 功能邏輯
- **Fetch API** - 與後端 API 通訊

## ⚠️ 注意事項

1. **CORS 問題**：如果遇到 CORS 錯誤，需要在 Spring Boot 後端設定 CORS 允許。
2. **API 連線**：確保 Spring Boot 服務正在運行且可以訪問。
3. **瀏覽器相容性**：建議使用現代瀏覽器（Chrome、Firefox、Safari、Edge）。

## 🐛 故障排除

### 問題 1：無法載入資料

**解決方案**：
1. 確認 Spring Boot 服務正在運行
2. 檢查 API URL 是否正確
3. 開啟瀏覽器開發者工具（F12）查看 Console 錯誤訊息

### 問題 2：CORS 錯誤

**解決方案**：
在 Spring Boot 後端加入 CORS 設定（`BeverageController.java`）：

```java
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/beverages")
public class BeverageController {
    // ...
}
```

### 問題 3：頁面顯示異常

**解決方案**：
1. 清除瀏覽器快取
2. 確認網路連線正常
3. 檢查 Tailwind CSS CDN 是否可以訪問

## 📚 相關文件

- [完整文件](../COMPLETE_DOCUMENTATION.md)
- [API 使用範例](../API_EXAMPLES.md)
- [後端 README](../README.md)

---

**享受使用 SmartWarehouse 前端介面！** 🎉


