#!/bin/bash

# SmartWarehouse 推送到 GitHub 腳本

echo "🚀 SmartWarehouse - 推送到 GitHub"
echo "================================"
echo ""

# 檢查是否已設定遠端
if git remote -v | grep -q "origin"; then
    echo "⚠️  遠端儲存庫已存在："
    git remote -v
    echo ""
    read -p "是否要更新遠端 URL？(y/N): " update_remote
    if [ "$update_remote" = "y" ] || [ "$update_remote" = "Y" ]; then
        read -p "請輸入新的 GitHub 儲存庫 URL: " new_url
        git remote set-url origin "$new_url"
        echo "✅ 遠端 URL 已更新"
    fi
else
    echo "📋 請先在 GitHub 建立新儲存庫："
    echo "   1. 前往：https://github.com/new"
    echo "   2. Repository name: SmartWarehouse"
    echo "   3. 選擇 Public"
    echo "   4. 不要勾選任何初始化選項"
    echo "   5. 點擊 Create repository"
    echo ""
    read -p "按 Enter 繼續，或按 Ctrl+C 取消..."
    echo ""
    
    echo "請輸入你的 GitHub 資訊："
    read -p "GitHub Username: " github_username
    read -p "Repository Name (預設: SmartWarehouse): " repo_name
    repo_name=${repo_name:-SmartWarehouse}
    
    echo ""
    echo "選擇連線方式："
    echo "1. HTTPS (推薦，簡單)"
    echo "2. SSH (需要設定 SSH key)"
    read -p "請選擇 (1/2): " connection_type
    
    if [ "$connection_type" = "2" ]; then
        remote_url="git@github.com:${github_username}/${repo_name}.git"
    else
        remote_url="https://github.com/${github_username}/${repo_name}.git"
    fi
    
    echo ""
    echo "設定遠端儲存庫..."
    git remote add origin "$remote_url"
    echo "✅ 遠端儲存庫已設定：$remote_url"
fi

echo ""
echo "📤 準備推送到 GitHub..."
echo ""

# 確認分支
current_branch=$(git branch --show-current)
if [ "$current_branch" != "main" ]; then
    echo "⚠️  當前分支是 $current_branch，切換到 main..."
    git branch -M main
fi

# 推送
echo "正在推送..."
if git push -u origin main; then
    echo ""
    echo "================================"
    echo "✅ 推送成功！"
    echo ""
    echo "🎉 你的專案現在在 GitHub 上了！"
    echo ""
    if git remote get-url origin | grep -q "https://"; then
        repo_url=$(git remote get-url origin | sed 's/\.git$//' | sed 's/^https:\/\///')
        echo "📎 專案網址：https://${repo_url}"
    else
        repo_url=$(git remote get-url origin | sed 's/\.git$//' | sed 's/^git@github.com://' | sed 's/:/\//')
        echo "📎 專案網址：https://${repo_url}"
    fi
    echo ""
    echo "💡 下一步建議："
    echo "   1. 前往專案頁面加入 Topics（spring-boot, java, h2-database 等）"
    echo "   2. 建立 Release v1.0.0"
    echo "   3. 更新 LinkedIn 和履歷"
else
    echo ""
    echo "❌ 推送失敗"
    echo ""
    echo "可能的原因："
    echo "1. 儲存庫尚未建立（請先前往 https://github.com/new 建立）"
    echo "2. 認證失敗（HTTPS 需要使用 Personal Access Token）"
    echo "3. 網路連線問題"
    echo ""
    echo "💡 提示："
    echo "   - HTTPS: 使用 Personal Access Token 作為密碼"
    echo "   - 建立 Token: https://github.com/settings/tokens"
    exit 1
fi

