#!/bin/bash

# SmartWarehouse 前端啟動腳本

echo "🚀 啟動 SmartWarehouse 前端介面..."
echo ""
echo "📋 使用說明："
echo "   1. 確保 Spring Boot 服務正在運行（http://localhost:8080）"
echo "   2. 選擇以下方式之一開啟前端："
echo ""
echo "   方式 1：直接開啟 HTML 檔案"
echo "   open index.html"
echo ""
echo "   方式 2：使用 Python HTTP 伺服器"
echo "   python3 -m http.server 8000"
echo ""
echo "   方式 3：使用 Node.js http-server"
echo "   http-server -p 8000"
echo ""

# 檢查 Python 是否可用
if command -v python3 &> /dev/null; then
    echo "✅ 偵測到 Python3，使用 HTTP 伺服器啟動..."
    echo "📱 前端將在 http://localhost:8000/index.html 開啟"
    echo ""
    echo "按 Ctrl+C 停止伺服器"
    echo ""
    python3 -m http.server 8000
else
    echo "⚠️  未偵測到 Python3，請手動開啟 index.html 檔案"
    echo ""
    echo "或安裝 Python："
    echo "  macOS: brew install python3"
    echo "  Linux: sudo apt-get install python3"
    echo ""
fi


