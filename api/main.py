"""FastAPI 後端服務，提供測試結果查詢 API"""
import sys
from pathlib import Path

# 加入 api 目錄到路徑
api_dir = Path(__file__).parent
sys.path.insert(0, str(api_dir))

from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from typing import Optional
from firestore_client import FirestoreClient
import os

app = FastAPI(
    title="Test Automation Platform API",
    description="雲原生測試自動化平台 API",
    version="1.0.0",
)

# 允許 CORS（前端可以呼叫）
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 生產環境應該限制特定網域
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 初始化 Firestore 客戶端
try:
    firestore_client = FirestoreClient()
except ValueError as e:
    print(f"警告：Firestore 初始化失敗：{e}")
    firestore_client = None


@app.get("/")
async def root():
    """API 根路徑"""
    return {
        "message": "Test Automation Platform API",
        "version": "1.0.0",
        "status": "running",
    }


@app.get("/health")
async def health_check():
    """健康檢查端點"""
    if firestore_client is None:
        return {"status": "degraded", "firestore": "not_configured"}
    return {"status": "healthy", "firestore": "connected"}


@app.get("/api/test-results")
async def get_test_results(
    limit: int = 50,
    status: Optional[str] = None,
):
    """
    取得測試結果列表

    Args:
        limit: 返回結果數量限制（預設 50）
        status: 篩選狀態 (passed/failed/skipped)

    Returns:
        測試結果列表
    """
    if firestore_client is None:
        raise HTTPException(
            status_code=503, detail="Firestore 未設定，無法查詢測試結果"
        )

    try:
        results = firestore_client.get_test_results(limit=limit, status=status)
        return {"count": len(results), "results": results}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"查詢失敗：{str(e)}")


@app.get("/api/test-statistics")
async def get_test_statistics():
    """
    取得測試統計資料

    Returns:
        測試統計資訊（總數、通過數、失敗數、通過率）
    """
    if firestore_client is None:
        raise HTTPException(
            status_code=503, detail="Firestore 未設定，無法查詢統計資料"
        )

    try:
        stats = firestore_client.get_test_statistics()
        return stats
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"查詢失敗：{str(e)}")


@app.get("/api/test-results/{test_id}")
async def get_test_result_by_id(test_id: str):
    """
    根據 ID 取得單一測試結果

    Args:
        test_id: 測試結果文件 ID

    Returns:
        測試結果詳情
    """
    if firestore_client is None:
        raise HTTPException(
            status_code=503, detail="Firestore 未設定，無法查詢測試結果"
        )

    try:
        # Firestore 直接查詢文件
        doc_ref = firestore_client.db.collection(
            firestore_client.collection_name
        ).document(test_id)
        doc = doc_ref.get()

        if not doc.exists:
            raise HTTPException(status_code=404, detail="測試結果不存在")

        result = doc.to_dict()
        result["id"] = doc.id
        if "timestamp" in result and result["timestamp"]:
            result["timestamp"] = result["timestamp"].isoformat()

        return result
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"查詢失敗：{str(e)}")


if __name__ == "__main__":
    import uvicorn

    host = os.getenv("API_HOST", "0.0.0.0")
    port = int(os.getenv("API_PORT", 8000))

    uvicorn.run(app, host=host, port=port)

