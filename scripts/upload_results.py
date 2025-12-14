"""上傳測試結果到 Firestore"""
import sys
import os
import json
from pathlib import Path

# 加入專案根目錄到路徑
project_root = Path(__file__).parent.parent
sys.path.insert(0, str(project_root))

from api.firestore_client import FirestoreClient


def parse_pytest_json_results(json_file: str) -> list:
    """解析 pytest JSON 測試結果"""
    try:
        with open(json_file, "r", encoding="utf-8") as f:
            data = json.load(f)
            return data.get("tests", [])
    except FileNotFoundError:
        print(f"警告：找不到測試結果檔案 {json_file}")
        return []


def upload_results():
    """上傳測試結果到 Firestore"""
    try:
        client = FirestoreClient()
        print("✅ Firestore 客戶端初始化成功")
    except Exception as e:
        print(f"❌ Firestore 初始化失敗：{e}")
        print("提示：請確認 GCP_SA_KEY 和 GCP_PROJECT_ID 已設定")
        return

    # 尋找測試結果檔案（pytest-json-report 或自訂格式）
    results_dir = project_root / "test-results"
    json_file = results_dir / "report.json"

    if not json_file.exists():
        print(f"⚠️  找不到測試結果檔案：{json_file}")
        print("提示：執行測試時請加上 --json-report 參數")
        return

    test_results = parse_pytest_json_results(str(json_file))

    if not test_results:
        print("⚠️  沒有測試結果可上傳")
        return

    uploaded_count = 0
    for test in test_results:
        try:
            test_name = test.get("nodeid", "unknown")
            status = test.get("outcome", "unknown")
            duration = test.get("duration", 0)
            error_message = None

            if status == "failed":
                error_message = test.get("call", {}).get("longrepr", "未知錯誤")

            client.save_test_result(
                test_name=test_name,
                status=status,
                duration=duration,
                error_message=error_message,
                metadata={"pytest_data": test},
            )
            uploaded_count += 1
        except Exception as e:
            print(f"❌ 上傳測試 {test.get('nodeid')} 失敗：{e}")

    print(f"✅ 成功上傳 {uploaded_count}/{len(test_results)} 個測試結果到 Firestore")


if __name__ == "__main__":
    upload_results()

