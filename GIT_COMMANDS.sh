#!/bin/bash

# Git 初始化與推送腳本
# 使用前請先修改 YOUR_USERNAME 和 YOUR_REPO_NAME

echo "🚀 SmartWarehouse Git 初始化"
echo "================================"
echo ""

# 檢查是否已經初始化
if [ -d ".git" ]; then
    echo "⚠️  Git 儲存庫已存在"
    read -p "是否要重新初始化？(y/N): " confirm
    if [ "$confirm" != "y" ]; then
        echo "取消操作"
        exit 0
    fi
    rm -rf .git
fi

# 1. 初始化 Git
echo "1️⃣  初始化 Git 儲存庫..."
git init
echo "✅ Git 初始化完成"
echo ""

# 2. 加入所有檔案
echo "2️⃣  加入檔案到暫存區..."
git add .
echo "✅ 檔案已加入"
echo ""

# 3. 顯示狀態
echo "3️⃣  查看暫存狀態..."
git status
echo ""

# 4. 建立第一次 Commit
echo "4️⃣  建立第一次 Commit..."
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

echo "✅ Commit 完成"
echo ""

# 5. 提示設定遠端儲存庫
echo "5️⃣  設定遠端儲存庫..."
echo ""
echo "請先在 GitHub 建立新專案："
echo "   1. 前往 https://github.com/new"
echo "   2. Repository name: SmartWarehouse"
echo "   3. 選擇 Public"
echo "   4. 不要勾選 Initialize with README"
echo "   5. 點擊 Create repository"
echo ""
read -p "按 Enter 繼續，或按 Ctrl+C 取消..."
echo ""

# 6. 詢問 GitHub 資訊
echo "請輸入你的 GitHub 資訊："
read -p "GitHub Username: " GITHUB_USERNAME
read -p "Repository Name (預設: SmartWarehouse): " REPO_NAME
REPO_NAME=${REPO_NAME:-SmartWarehouse}

# 7. 設定遠端
echo ""
echo "6️⃣  設定遠端儲存庫..."
git remote add origin "https://github.com/${GITHUB_USERNAME}/${REPO_NAME}.git"
echo "✅ 遠端儲存庫已設定：https://github.com/${GITHUB_USERNAME}/${REPO_NAME}.git"
echo ""

# 8. 重新命名分支
echo "7️⃣  設定分支名稱..."
git branch -M main
echo "✅ 分支已設定為 main"
echo ""

# 9. 推送到 GitHub
echo "8️⃣  推送到 GitHub..."
echo "⚠️  這會要求你輸入 GitHub 帳號密碼或 Personal Access Token"
echo ""
read -p "是否現在推送？(Y/n): " push_confirm
push_confirm=${push_confirm:-Y}

if [ "$push_confirm" = "Y" ] || [ "$push_confirm" = "y" ]; then
    git push -u origin main
    echo ""
    echo "✅ 推送完成！"
    echo ""
    echo "🎉 專案已成功上傳到 GitHub！"
    echo "   網址：https://github.com/${GITHUB_USERNAME}/${REPO_NAME}"
else
    echo ""
    echo "⏭️  已跳過推送"
    echo ""
    echo "💡 稍後可以手動執行："
    echo "   git push -u origin main"
fi

echo ""
echo "================================"
echo "✅ Git 設定完成！"

