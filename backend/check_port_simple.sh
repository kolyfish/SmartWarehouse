#!/bin/bash

# 簡單的端口檢查腳本（多種方法）

PORT=${1:-8080}

echo "🔍 檢查端口 $PORT..."
echo ""

# 方法 1: 使用 sudo lsof（最可靠）
echo "📌 方法 1: 使用 sudo lsof（推薦）"
echo "執行: sudo lsof -i :$PORT"
echo ""
sudo lsof -i :$PORT 2>/dev/null || echo "   ℹ️  端口未被占用或需要輸入密碼"
echo ""

# 方法 2: 直接測試端口
echo "📌 方法 2: 測試服務是否運行"
if curl -s http://localhost:$PORT/api/beverages/statistics > /dev/null 2>&1; then
    echo "   ✅ 服務正在運行！"
    echo "   測試回應："
    curl -s http://localhost:$PORT/api/beverages/statistics | python3 -m json.tool 2>/dev/null || curl -s http://localhost:$PORT/api/beverages/statistics
else
    echo "   ℹ️  服務未運行或無法連接"
fi
echo ""

# 方法 3: 查找 Java Spring Boot 進程
echo "📌 方法 3: 查找 Spring Boot 進程"
SPRING_PROCESS=$(ps aux | grep -i "spring-boot\|BeverageWarehouse" | grep -v grep | head -1)
if [ -n "$SPRING_PROCESS" ]; then
    echo "   ✅ 找到可能的 Spring Boot 進程："
    echo "$SPRING_PROCESS" | awk '{print "   PID: " $2 " | " $11 " " $12 " " $13}'
else
    echo "   ℹ️  沒有找到 Spring Boot 進程"
fi


