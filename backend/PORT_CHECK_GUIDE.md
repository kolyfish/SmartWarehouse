# 🔍 端口檢查指南

## 為什麼在 Cursor 終端看不到占用端口的進程？

### 常見原因

1. **權限問題**：`lsof` 需要管理員權限才能查看所有進程
2. **端口確實未被占用**：沒有輸出表示端口可用
3. **命令執行但無結果**：這是正常的，表示端口未被占用

---

## ✅ 解決方案

### 方法 1：使用 sudo（最可靠）

```bash
sudo lsof -i :8080
```

**說明**：
- 需要輸入你的 macOS 密碼
- 可以查看所有占用端口的進程（包括系統進程）
- 最準確的方法

### 方法 2：使用檢查腳本

```bash
cd backend
./check_port.sh 8080
```

這個腳本會使用多種方法檢查端口，包括：
- lsof（帶 sudo）
- netstat
- nc (netcat)
- curl 測試服務
- 查找 Java 進程

### 方法 3：直接測試服務

```bash
# 測試服務是否運行
curl http://localhost:8080/api/beverages/statistics

# 如果有回應，表示服務正在運行
# 如果沒有回應，表示服務未運行
```

### 方法 4：查找 Java 進程

```bash
# 查找所有 Java 進程
ps aux | grep java | grep -v grep

# 查找 Spring Boot 相關進程
ps aux | grep -i "spring-boot\|BeverageWarehouse" | grep -v grep
```

---

## 🎯 實際使用建議

### 檢查端口是否被占用

```bash
# 快速檢查（不需要 sudo）
lsof -i :8080

# 如果沒有輸出 = 端口未被占用 ✅
# 如果有輸出 = 端口被占用 ⚠️
```

### 如果需要查看詳細資訊（需要 sudo）

```bash
sudo lsof -i :8080
```

### 測試服務是否運行

```bash
curl http://localhost:8080/api/beverages/statistics
```

**結果說明**：
- 返回 JSON = 服務正在運行 ✅
- `Connection refused` = 服務未運行 ❌
- 無回應 = 服務未運行或端口被其他程序占用 ❌

---

## 🛠️ 實用腳本

### 快速檢查腳本

```bash
#!/bin/bash
PORT=8080

echo "檢查端口 $PORT..."

# 方法 1: lsof
if lsof -i :$PORT > /dev/null 2>&1; then
    echo "✅ 端口被占用："
    lsof -i :$PORT
else
    echo "ℹ️  端口未被占用（或需要 sudo）"
    echo "   嘗試: sudo lsof -i :$PORT"
fi

# 方法 2: 測試服務
echo ""
echo "測試服務..."
if curl -s http://localhost:$PORT/api/beverages/statistics > /dev/null 2>&1; then
    echo "✅ 服務正在運行"
else
    echo "ℹ️  服務未運行"
fi
```

---

## 💡 常見問題

### Q1: 為什麼 `lsof -i :8080` 沒有輸出？

**A**: 這表示端口 8080 **未被占用**，這是好事！你可以直接啟動服務。

### Q2: 如何確認端口真的被占用了？

**A**: 嘗試啟動服務，如果端口被占用，會看到錯誤訊息：
```
Port 8080 was already in use.
```

### Q3: 為什麼需要 sudo？

**A**: macOS 的安全機制，某些系統進程只有管理員才能查看。

### Q4: 有沒有不需要 sudo 的方法？

**A**: 有！直接測試服務：
```bash
curl http://localhost:8080/api/beverages/statistics
```
如果服務在運行，會返回 JSON；如果沒有，會顯示連接錯誤。

---

## 📋 總結

| 情況 | 命令 | 結果 |
|------|------|------|
| **端口未被占用** | `lsof -i :8080` | 無輸出（正常）✅ |
| **端口被占用** | `lsof -i :8080` | 顯示進程資訊 ⚠️ |
| **需要詳細資訊** | `sudo lsof -i :8080` | 顯示所有進程（需要密碼） |
| **測試服務** | `curl http://localhost:8080/...` | 有回應 = 運行中 ✅ |

---

**記住**：`lsof` 沒有輸出通常是好事，表示端口可用！🎉

