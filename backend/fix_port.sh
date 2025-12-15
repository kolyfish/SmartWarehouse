#!/bin/bash

# 修復端口占用問題的腳本

PORT=8080

echo "🔍 檢查端口 $PORT 占用情況..."
echo ""

# 檢查端口是否被占用
PID=$(lsof -ti :$PORT)

if [ -z "$PID" ]; then
    echo "✅ 端口 $PORT 未被占用，可以直接啟動服務"
    echo ""
    echo "執行：mvn spring-boot:run"
    exit 0
fi

echo "⚠️  端口 $PORT 已被進程 $PID 占用"
echo ""
echo "進程詳情："
ps -p $PID -o pid,command | head -2
echo ""

# 詢問用戶要如何處理
echo "請選擇處理方式："
echo "1) 停止舊進程並啟動新服務（推薦）"
echo "2) 使用不同端口（8081）啟動新服務"
echo "3) 取消"
echo ""
read -p "請輸入選項 (1/2/3): " choice

case $choice in
    1)
        echo ""
        echo "🛑 正在停止進程 $PID..."
        kill $PID
        sleep 2
        
        # 確認進程是否已停止
        if lsof -ti :$PORT > /dev/null 2>&1; then
            echo "⚠️  進程仍在運行，強制終止..."
            kill -9 $PID
            sleep 1
        fi
        
        if lsof -ti :$PORT > /dev/null 2>&1; then
            echo "❌ 無法停止進程，請手動處理"
            exit 1
        else
            echo "✅ 進程已停止"
            echo ""
            echo "🚀 現在可以啟動服務："
            echo "   mvn spring-boot:run"
        fi
        ;;
    2)
        echo ""
        echo "📝 修改 application.properties 使用端口 8081..."
        
        # 備份原文件
        cp src/main/resources/application.properties src/main/resources/application.properties.bak
        
        # 修改端口
        sed -i '' "s/server.port=8080/server.port=8081/" src/main/resources/application.properties
        
        echo "✅ 端口已修改為 8081"
        echo ""
        echo "🚀 現在可以啟動服務："
        echo "   mvn spring-boot:run"
        echo ""
        echo "📱 服務將在 http://localhost:8081 運行"
        echo "📱 前端需要更新 API URL 為：http://localhost:8081/api/beverages"
        echo ""
        echo "💡 要恢復原設定，執行："
        echo "   mv src/main/resources/application.properties.bak src/main/resources/application.properties"
        ;;
    3)
        echo "取消操作"
        exit 0
        ;;
    *)
        echo "無效選項"
        exit 1
        ;;
esac

