#!/bin/bash

# SmartWarehouse API 快速測試腳本

BASE_URL="http://localhost:8080/api/beverages"

echo "🏭 SmartWarehouse API 測試"
echo "================================"
echo ""

# 檢查服務是否運行
echo "📡 檢查服務狀態..."
if ! curl -s http://localhost:8080/api/beverages > /dev/null 2>&1; then
    echo "❌ 錯誤：Spring Boot 服務未運行"
    echo "請先執行：cd backend && mvn spring-boot:run"
    exit 1
fi
echo "✅ 服務運行中"
echo ""

# 1. 入庫測試
echo "1️⃣  入庫 100 瓶礦泉水（2024-01-01 生產，2025-01-01 過期）..."
RESPONSE1=$(curl -s -X POST $BASE_URL/stock-in \
  -H "Content-Type: application/json" \
  -d '{
    "name": "礦泉水",
    "quantity": 100,
    "productionDate": "2024-01-01",
    "expiryDate": "2025-01-01"
  }')
echo "$RESPONSE1" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE1"
echo ""

# 2. 入庫第二批
echo "2️⃣  入庫 50 瓶礦泉水（2024-02-01 生產，2025-02-01 過期）..."
RESPONSE2=$(curl -s -X POST $BASE_URL/stock-in \
  -H "Content-Type: application/json" \
  -d '{
    "name": "礦泉水",
    "quantity": 50,
    "productionDate": "2024-02-01",
    "expiryDate": "2025-02-01"
  }')
echo "$RESPONSE2" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE2"
echo ""

# 3. 查詢所有飲料
echo "3️⃣  查詢所有飲料庫存..."
RESPONSE3=$(curl -s $BASE_URL)
echo "$RESPONSE3" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE3"
echo ""

# 4. 查看統計
echo "4️⃣  查看庫存統計..."
RESPONSE4=$(curl -s $BASE_URL/statistics)
echo "$RESPONSE4" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE4"
echo ""

# 5. 出庫測試
echo "5️⃣  出庫 30 瓶礦泉水（系統會自動選擇最早過期的）..."
RESPONSE5=$(curl -s -X POST $BASE_URL/stock-out \
  -H "Content-Type: application/json" \
  -d '{
    "name": "礦泉水",
    "quantity": 30
  }')
echo "$RESPONSE5" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE5"
echo ""

# 6. 再次查詢
echo "6️⃣  查詢出庫後的庫存..."
RESPONSE6=$(curl -s $BASE_URL)
echo "$RESPONSE6" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE6"
echo ""

# 7. 檢查即將過期
echo "7️⃣  檢查即將過期的飲料（7 天內）..."
RESPONSE7=$(curl -s $BASE_URL/expiring-soon)
echo "$RESPONSE7" | python3 -m json.tool 2>/dev/null || echo "$RESPONSE7"
echo ""

echo "================================"
echo "✅ 測試完成！"
echo ""
echo "💡 提示："
echo "   - 查看所有 API 範例：cat API_EXAMPLES.md"
echo "   - H2 Console：http://localhost:8080/h2-console"
echo "   - API 文件：http://localhost:8080/api/beverages"
echo ""
echo "🔒 高併發測試："
echo "   - TDD 測試：cd backend && mvn test -Dtest=BeverageServiceConcurrencyTest"
echo "   - JMeter 測試：jmeter -t jmeter/SmartWarehouse_Concurrency_Test.jmx"
echo "   - 查看 JMeter 說明：cat jmeter/README.md"

