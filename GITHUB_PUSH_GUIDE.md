# 🚀 推送到 GitHub 完整指南

## ✅ 本地準備完成

你的本地 Git 儲存庫已經準備好了：
- ✅ Git 已初始化
- ✅ 所有檔案已加入
- ✅ 第一次 Commit 已完成
- ✅ 分支已設定為 `main`

## 📋 下一步：在 GitHub 建立新儲存庫

### 步驟 1：前往 GitHub 建立新儲存庫

1. **開啟瀏覽器**，前往：https://github.com/new

2. **填寫儲存庫資訊**：
   - **Repository name**: `SmartWarehouse`（或你喜歡的名稱）
   - **Description**: `智慧倉庫系統 - 飲料庫存管理（Spring Boot + H2 Database + JMeter + TDD）`
   - **Visibility**: 選擇 **Public**（公開，方便展示作品集）
   - ⚠️ **重要**：**不要**勾選以下選項：
     - ❌ "Add a README file"（我們已經有 README）
     - ❌ "Add .gitignore"（我們已經有 .gitignore）
     - ❌ "Choose a license"（可選，之後再加）

3. **點擊 "Create repository"**

### 步驟 2：複製儲存庫 URL

建立完成後，GitHub 會顯示儲存庫 URL，例如：
```
https://github.com/YOUR_USERNAME/SmartWarehouse.git
```

**記下這個 URL**，下一步會用到。

---

## 🔗 連接遠端儲存庫並推送

### 方法 1：使用 HTTPS（推薦，簡單）

在終端執行以下命令（**替換 YOUR_USERNAME**）：

```bash
# 1. 設定遠端儲存庫
git remote add origin https://github.com/YOUR_USERNAME/SmartWarehouse.git

# 2. 驗證遠端設定
git remote -v

# 3. 推送到 GitHub
git push -u origin main
```

**如果要求輸入帳號密碼**：
- Username: 你的 GitHub 使用者名稱
- Password: 使用 **Personal Access Token**（不是 GitHub 密碼）
  - 如何建立 Token：https://github.com/settings/tokens
  - 權限選擇：`repo`（完整儲存庫權限）

### 方法 2：使用 SSH（如果你有設定 SSH key）

```bash
# 1. 設定遠端儲存庫（使用 SSH）
git remote add origin git@github.com:YOUR_USERNAME/SmartWarehouse.git

# 2. 推送到 GitHub
git push -u origin main
```

---

## 🎯 快速執行指令

**複製以下指令，替換 `YOUR_USERNAME` 後執行**：

```bash
# 設定遠端（替換 YOUR_USERNAME）
git remote add origin https://github.com/YOUR_USERNAME/SmartWarehouse.git

# 推送到 GitHub
git push -u origin main
```

---

## ✅ 驗證推送成功

推送完成後：

1. **前往你的 GitHub 專案頁面**
   - URL: `https://github.com/YOUR_USERNAME/SmartWarehouse`

2. **確認所有檔案都已上傳**
   - 應該看到：`backend/`, `tests/`, `api/`, `jmeter/`, `README.md` 等

3. **確認 README.md 正確顯示**
   - GitHub 會自動渲染 Markdown

---

## 🎉 完成後的下一步

### 1. 設定 GitHub Topics（增加曝光度）

1. 前往專案頁面
2. 點擊右側的 ⚙️ **Settings** 圖示
3. 在 "Topics" 區塊加入：
   - `spring-boot`
   - `java`
   - `h2-database`
   - `playwright`
   - `python`
   - `fastapi`
   - `jmeter`
   - `tdd`
   - `ci-cd`
   - `warehouse-management`

### 2. 建立 Release（可選）

1. 前往 **Releases** > **Create a new release**
2. **Tag**: `v1.0.0`
3. **Title**: `SmartWarehouse v1.0.0 - 初始版本`
4. **Description**: 貼上專案功能說明
5. 點擊 **Publish release**

### 3. 更新 LinkedIn 和履歷

- 在 LinkedIn 分享你的新專案
- 更新履歷，加入 GitHub 連結
- 強調技術棧和功能亮點

---

## 🐛 常見問題

### Q1: 推送時要求輸入密碼

**解決方案**：使用 Personal Access Token
1. 前往：https://github.com/settings/tokens
2. 點擊 "Generate new token (classic)"
3. 選擇 `repo` 權限
4. 複製 Token，當作密碼使用

### Q2: 遠端儲存庫已存在

**錯誤**：`remote origin already exists`

**解決方案**：
```bash
# 移除舊的遠端
git remote remove origin

# 重新加入
git remote add origin https://github.com/YOUR_USERNAME/SmartWarehouse.git
```

### Q3: 推送被拒絕

**錯誤**：`rejected: updates were rejected`

**解決方案**：
```bash
# 強制推送（僅限第一次推送）
git push -u origin main --force
```

---

## 📝 未來更新專案

推送完成後，未來更新專案只需要：

```bash
# 1. 加入變更
git add .

# 2. 提交
git commit -m "feat: 新增功能說明

詳細描述...

ref #10152"

# 3. 推送
git push
```

---

**準備好了嗎？前往 GitHub 建立儲存庫，然後執行推送指令！** 🚀

