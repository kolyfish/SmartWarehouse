#!/bin/bash

# 檢查端口占用的多種方法

PORT=${1:-8080}

echo "🔍 檢查端口 $PORT 占用情況..."
echo ""

# 方法 1: 使用 lsof（需要權限）
echo "方法 1: 使用 lsof"
if command -v lsof &> /dev/null; then
    # 嘗試使用 sudo（如果需要的話）
    if sudo lsof -i :$PORT 2>/dev/null | grep -q LISTEN; then
        echo "✅ 找到占用端口的進程："
        sudo lsof -i :$PORT
    elif lsof -i :$PORT 2>/dev/null | grep -q LISTEN; then
        echo "✅ 找到占用端口的進程："
        lsof -i :$PORT
    else
        echo "ℹ️  端口 $PORT 未被占用（或需要 sudo 權限）"
    fi
else
    echo "❌ lsof 命令不存在"
fi

echo ""
echo "---"
echo ""

# 方法 2: 使用 netstat（macOS 可能不可用）
echo "方法 2: 使用 netstat"
if command -v netstat &> /dev/null; then
    RESULT=$(netstat -an | grep ":$PORT " | grep LISTEN)
    if [ -n "$RESULT" ]; then
        echo "✅ 找到占用端口的連接："
        echo "$RESULT"
    else
        echo "ℹ️  端口 $PORT 未被占用"
    fi
else
    echo "ℹ️  netstat 不可用（macOS 上可能被移除）"
fi

echo ""
echo "---"
echo ""

# 方法 3: 使用 nc (netcat) 測試端口是否開放
echo "方法 3: 使用 nc 測試端口"
if command -v nc &> /dev/null; then
    if nc -z localhost $PORT 2>/dev/null; then
        echo "✅ 端口 $PORT 正在監聽"
    else
        echo "ℹ️  端口 $PORT 未在監聽"
    fi
else
    echo "ℹ️  nc 命令不可用"
fi

echo ""
echo "---"
echo ""

# 方法 4: 使用 curl 測試服務
echo "方法 4: 使用 curl 測試服務"
if command -v curl &> /dev/null; then
    RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$PORT/api/beverages/statistics 2>/dev/null)
    if [ "$RESPONSE" = "200" ] || [ "$RESPONSE" = "404" ] || [ "$RESPONSE" = "500" ]; then
        echo "✅ 端口 $PORT 有服務在運行（HTTP 狀態碼: $RESPONSE）"
    else
        echo "ℹ️  端口 $PORT 沒有 HTTP 服務在運行"
    fi
else
    echo "ℹ️  curl 命令不可用"
fi

echo ""
echo "---"
echo ""

# 方法 5: 使用 ps 查找 Java 進程
echo "方法 5: 查找可能的 Java 進程"
JAVA_PROCESSES=$(ps aux | grep -i java | grep -v grep)
if [ -n "$JAVA_PROCESSES" ]; then
    echo "✅ 找到 Java 進程："
    echo "$JAVA_PROCESSES" | head -5
    echo ""
    echo "💡 提示：如果看到 Spring Boot 相關進程，可能就是占用端口的服務"
else
    echo "ℹ️  沒有找到 Java 進程"
fi

echo ""
echo "=========================================="
echo "📋 總結建議："
echo ""
echo "如果端口被占用但看不到進程，可以嘗試："
echo "1. 使用 sudo: sudo lsof -i :$PORT"
echo "2. 直接嘗試啟動服務，如果失敗會看到錯誤訊息"
echo "3. 使用不同端口（修改 application.properties）"
echo ""

