# 🚀 在 GitHub 建立儲存庫 - 詳細步驟

## ❌ 錯誤原因

錯誤訊息：`remote: Repository not found`

**原因**：GitHub 上還沒有建立名為 `SmartWarehouse` 的儲存庫。

---

## ✅ 解決步驟

### 步驟 1：前往 GitHub 建立新儲存庫

1. **開啟瀏覽器**，前往：https://github.com/new

2. **填寫儲存庫資訊**：
   - **Owner**: 選擇 `wupengyue`（你的帳號）
   - **Repository name**: 輸入 `SmartWarehouse`
   - **Description**: `智慧倉庫系統 - 飲料庫存管理（Spring Boot + H2 Database + JMeter + TDD）`
   - **Visibility**: 選擇 **Public**（公開，方便展示作品集）

3. **⚠️ 重要：不要勾選以下選項**：
   - ❌ "Add a README file"（我們已經有 README）
   - ❌ "Add .gitignore"（我們已經有 .gitignore）
   - ❌ "Choose a license"（可選，之後再加）

4. **點擊綠色的 "Create repository" 按鈕**

### 步驟 2：驗證儲存庫已建立

建立完成後，你應該會看到：
- 網址：https://github.com/wupengyue/SmartWarehouse
- 頁面顯示 "Quick setup" 說明

### 步驟 3：回到終端執行推送

```bash
git push -u origin main
```

---

## 🔐 認證問題解決

如果推送時要求輸入密碼，請使用 **Personal Access Token**：

### 建立 Personal Access Token

1. **前往**：https://github.com/settings/tokens
2. **點擊**："Generate new token" > "Generate new token (classic)"
3. **填寫資訊**：
   - Note: `SmartWarehouse Push`
   - Expiration: 選擇適合的期限（建議 90 天或 No expiration）
   - **勾選權限**：`repo`（完整儲存庫權限）
4. **點擊**："Generate token"
5. **複製 Token**（只會顯示一次，請妥善保存）

### 使用 Token 推送

推送時：
- **Username**: `wupengyue`
- **Password**: 貼上剛才複製的 **Personal Access Token**（不是 GitHub 密碼）

---

## 📋 完整流程總結

```bash
# 1. 確認遠端設定（已完成）
git remote -v

# 2. 在 GitHub 建立儲存庫（在瀏覽器完成）
# 前往：https://github.com/new

# 3. 推送（在終端執行）
git push -u origin main
```

---

## 🐛 常見問題

### Q1: 還是顯示 "Repository not found"

**檢查**：
1. 確認儲存庫名稱完全一致：`SmartWarehouse`（大小寫敏感）
2. 確認儲存庫屬於你的帳號：`wupengyue`
3. 確認儲存庫已建立：前往 https://github.com/wupengyue/SmartWarehouse 查看

### Q2: 認證失敗

**解決方案**：
- 使用 Personal Access Token 而不是 GitHub 密碼
- 確認 Token 有 `repo` 權限
- 確認 Token 未過期

### Q3: 想要使用不同的儲存庫名稱

**解決方案**：
```bash
# 移除舊的遠端
git remote remove origin

# 設定新的遠端（替換 REPO_NAME）
git remote add origin https://github.com/wupengyue/REPO_NAME.git

# 推送
git push -u origin main
```

---

**準備好了嗎？前往 https://github.com/new 建立儲存庫，然後執行 `git push -u origin main`！** 🚀

