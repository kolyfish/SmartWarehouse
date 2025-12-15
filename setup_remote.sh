#!/bin/bash

# 設定 GitHub 遠端儲存庫

echo "🚀 設定 GitHub 遠端儲存庫"
echo "================================"
echo ""

# 移除舊的遠端（如果存在）
if git remote | grep -q "origin"; then
    git remote remove origin
    echo "✅ 已移除舊的遠端設定"
fi

echo ""
echo "請輸入你的 GitHub 資訊："
read -p "GitHub 使用者名稱: " github_username
read -p "儲存庫名稱 (預設: SmartWarehouse): " repo_name

# 如果沒有輸入，使用預設值
if [ -z "$repo_name" ]; then
    repo_name="SmartWarehouse"
fi

echo ""
echo "設定遠端儲存庫..."
git remote add origin "https://github.com/${github_username}/${repo_name}.git"

echo "✅ 遠端已設定為: https://github.com/${github_username}/${repo_name}.git"
echo ""
echo "📋 請確認："
echo "   1. 你已經在 GitHub 建立了名為 '${repo_name}' 的儲存庫"
echo "   2. 儲存庫是 Public 或你有權限"
echo "   3. 儲存庫網址：https://github.com/${github_username}/${repo_name}"
echo ""
read -p "確認後按 Enter 繼續推送，或按 Ctrl+C 取消..."

echo ""
echo "📤 推送到 GitHub..."
git push -u origin main

if [ $? -eq 0 ]; then
    echo ""
    echo "================================"
    echo "✅ 推送成功！"
    echo ""
    echo "🎉 你的專案現在在 GitHub 上了！"
    echo "📎 專案網址：https://github.com/${github_username}/${repo_name}"
else
    echo ""
    echo "❌ 推送失敗"
    echo ""
    echo "可能的原因："
    echo "1. 儲存庫尚未建立（請先前往 https://github.com/new 建立）"
    echo "2. 認證失敗（HTTPS 需要使用 Personal Access Token）"
    echo "3. 儲存庫名稱或使用者名稱錯誤"
    echo ""
    echo "💡 提示："
    echo "   - 建立 Token: https://github.com/settings/tokens"
    echo "   - 權限選擇: repo (完整儲存庫權限)"
fi

