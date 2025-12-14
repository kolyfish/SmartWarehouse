"""Firestore 客戶端，用於儲存和查詢測試結果"""
from google.cloud import firestore
from typing import Dict, List, Optional
import os
import json
from datetime import datetime


class FirestoreClient:
    """Firestore 客戶端封裝"""

    def __init__(self, project_id: Optional[str] = None):
        """
        初始化 Firestore 客戶端

        Args:
            project_id: GCP 專案 ID，如果為 None 則從環境變數讀取
        """
        self.project_id = project_id or os.getenv("GCP_PROJECT_ID")
        if not self.project_id:
            raise ValueError("GCP_PROJECT_ID 環境變數未設定")

        # 初始化 Firestore 客戶端
        self.db = firestore.Client(project=self.project_id)
        self.collection_name = os.getenv("FIRESTORE_COLLECTION", "test_results")

    def save_test_result(
        self,
        test_name: str,
        status: str,
        duration: float,
        error_message: Optional[str] = None,
        metadata: Optional[Dict] = None,
    ) -> str:
        """
        儲存測試結果到 Firestore

        Args:
            test_name: 測試名稱
            status: 測試狀態 (passed/failed/skipped)
            duration: 測試執行時間（秒）
            error_message: 錯誤訊息（如果失敗）
            metadata: 額外的測試元資料

        Returns:
            文件 ID
        """
        doc_data = {
            "test_name": test_name,
            "status": status,
            "duration": duration,
            "timestamp": firestore.SERVER_TIMESTAMP,
            "error_message": error_message,
            "metadata": metadata or {},
        }

        doc_ref = self.db.collection(self.collection_name).add(doc_data)
        return doc_ref[1].id

    def get_test_results(
        self, limit: int = 50, status: Optional[str] = None
    ) -> List[Dict]:
        """
        查詢測試結果

        Args:
            limit: 返回結果數量限制
            status: 篩選狀態 (passed/failed/skipped)

        Returns:
            測試結果列表
        """
        query = self.db.collection(self.collection_name)

        if status:
            query = query.where("status", "==", status)

        query = query.order_by("timestamp", direction=firestore.Query.DESCENDING)
        query = query.limit(limit)

        results = []
        for doc in query.stream():
            doc_dict = doc.to_dict()
            doc_dict["id"] = doc.id
            # 轉換 Firestore Timestamp 為 ISO 字串
            if "timestamp" in doc_dict and doc_dict["timestamp"]:
                doc_dict["timestamp"] = doc_dict["timestamp"].isoformat()
            results.append(doc_dict)

        return results

    def get_test_statistics(self) -> Dict:
        """
        取得測試統計資料

        Returns:
            包含總數、通過數、失敗數的字典
        """
        all_results = self.get_test_results(limit=1000)
        total = len(all_results)
        passed = sum(1 for r in all_results if r.get("status") == "passed")
        failed = sum(1 for r in all_results if r.get("status") == "failed")
        skipped = sum(1 for r in all_results if r.get("status") == "skipped")

        return {
            "total": total,
            "passed": passed,
            "failed": failed,
            "skipped": skipped,
            "pass_rate": round(passed / total * 100, 2) if total > 0 else 0,
        }

