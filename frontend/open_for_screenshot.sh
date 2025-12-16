#!/bin/bash

# 開啟前端畫面供截圖使用

echo "🎨 正在開啟 SmartWarehouse 前端介面..."
echo ""

# 檢查服務是否運行
if ! curl -s http://localhost:8080/api/beverages/statistics > /dev/null 2>&1; then
    echo "⚠️  Spring Boot 服務未運行"
    echo "   請先啟動服務：cd backend && mvn spring-boot:run"
    exit 1
fi

echo "✅ Spring Boot 服務正在運行"
echo ""

# 方法 1: 直接開啟 HTML 檔案（最簡單）
echo "📱 方法 1: 直接開啟 HTML 檔案"
echo "   正在開啟瀏覽器..."
open frontend/index.html 2>/dev/null || echo "   無法自動開啟，請手動開啟: frontend/index.html"

echo ""
echo "📋 截圖建議："
echo ""
echo "1. 📊 統計卡片頁面（預設載入）"
echo "   - 顯示總庫存、過期數量等統計"
echo ""
echo "2. 📦 所有庫存列表"
echo "   - 點擊「查看所有庫存」按鈕"
echo "   - 顯示完整的商品資訊"
echo ""
echo "3. ⚠️  過期商品管理"
echo "   - 點擊「查看已過期」或「查看即將過期」"
echo "   - 展示過期商品處理功能"
echo ""
echo "4. 🚧 隔離區和報廢管理"
echo "   - 點擊「查看隔離區」或「查看已報廢」"
echo "   - 展示業界標準的過期商品處理流程"
echo ""
echo "💡 截圖技巧："
echo "   - 使用瀏覽器的全螢幕模式（F11）"
echo "   - 調整瀏覽器視窗大小以獲得最佳畫面"
echo "   - 可以截取多個頁面展示不同功能"
echo ""
echo "🌐 前端 URL: file://$(pwd)/frontend/index.html"
echo "🔗 API URL: http://localhost:8080/api/beverages"
echo ""

# 方法 2: 使用 Python HTTP 伺服器（如果需要）
echo "📱 方法 2: 使用 HTTP 伺服器（可選）"
echo "   如果需要通過 HTTP 訪問，執行："
echo "   cd frontend"
echo "   python3 -m http.server 8000"
echo "   然後在瀏覽器開啟: http://localhost:8000/index.html"
echo ""


