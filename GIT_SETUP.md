# 🚀 Git 初始化與推送指南

## 步驟 1：初始化 Git 儲存庫

```bash
# 在專案根目錄執行
git init
```

## 步驟 2：建立 .gitignore（已完成）

`.gitignore` 檔案已經建立，會自動忽略：
- Python 虛擬環境
- Java 編譯檔案
- IDE 設定檔
- 環境變數檔案
- 測試結果

## 步驟 3：加入所有檔案

```bash
# 加入所有檔案到暫存區
git add .

# 查看即將提交的檔案
git status
```

## 步驟 4：建立第一次 Commit

```bash
git commit -m "feat: 初始化 SmartWarehouse 智慧倉庫系統

功能：
- Spring Boot 後端（飲料庫存管理）
- H2 Database SQL 資料庫
- 完整的 CRUD 操作
- 入庫/出庫管理（FIFO 策略）
- 過期檢查與提醒
- Playwright 自動化測試
- FastAPI 測試結果 API
- GitHub Actions CI/CD

技術棧：
- Java 17 + Spring Boot 3.2.0
- H2 Database（內存資料庫）
- Python 3.11 + Playwright
- FastAPI + Firestore
- GitHub Actions

ref #10152"
```

## 步驟 5：在 GitHub 建立新專案

1. 前往 https://github.com/new
2. Repository name: `SmartWarehouse`（或你喜歡的名稱）
3. Description: `智慧倉庫系統 - 飲料庫存管理（Spring Boot + H2 Database + Playwright）`
4. 選擇 Public（公開，方便展示作品集）
5. **不要**勾選 "Initialize this repository with a README"（我們已經有 README）
6. 點擊 "Create repository"

## 步驟 6：連接遠端儲存庫並推送

```bash
# 將 GitHub 儲存庫設為遠端（替換 YOUR_USERNAME 和 YOUR_REPO_NAME）
git remote add origin https://github.com/YOUR_USERNAME/SmartWarehouse.git

# 或使用 SSH（如果你有設定 SSH key）
# git remote add origin git@github.com:YOUR_USERNAME/SmartWarehouse.git

# 重新命名分支為 main（如果需要的話）
git branch -M main

# 推送到 GitHub
git push -u origin main
```

## 步驟 7：驗證推送成功

1. 前往你的 GitHub 專案頁面
2. 確認所有檔案都已上傳
3. 確認 README.md 正確顯示

## 🎉 完成！

你的專案現在已經在 GitHub 上了！

### 下一步建議：

1. **設定 GitHub Pages**（可選）
   - Settings > Pages
   - 選擇 main 分支
   - 可以展示專案文件

2. **加入 GitHub Topics**
   - 在專案頁面點擊 ⚙️ > Topics
   - 加入：`spring-boot`, `java`, `h2-database`, `playwright`, `python`, `fastapi`, `ci-cd`

3. **建立 Release**
   - Releases > Create a new release
   - Tag: `v1.0.0`
   - Title: `SmartWarehouse v1.0.0 - 初始版本`

4. **更新 LinkedIn**
   - 在 LinkedIn 分享你的新專案
   - 強調技術棧和功能

---

## 📝 Commit Message 範例（未來使用）

### 新增功能
```bash
git commit -m "feat: 新增批次入庫功能

- 支援一次入庫多種飲料
- 加入批次驗證邏輯
- 更新 API 文件

ref #10152"
```

### 修復 Bug
```bash
git commit -m "fix: 修正出庫時庫存計算錯誤

問題：出庫時未正確扣除庫存
解決：修正 Service 層的庫存計算邏輯

ref #10152"
```

### 重構
```bash
git commit -m "refactor: 重構 BeverageService 方法

- 抽取共同邏輯到私有方法
- 改善程式碼可讀性
- 保持向後兼容

ref #10152"
```

