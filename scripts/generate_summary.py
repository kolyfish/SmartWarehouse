"""產生測試摘要（用於 GitHub Actions）"""
import json
from pathlib import Path

project_root = Path(__file__).parent.parent
results_dir = project_root / "test-results"
json_file = results_dir / "report.json"


def generate_summary():
    """產生測試摘要"""
    if not json_file.exists():
        print("⚠️  找不到測試結果檔案")
        return

    try:
        with open(json_file, "r", encoding="utf-8") as f:
            data = json.load(f)

        total = data.get("summary", {}).get("total", 0)
        passed = data.get("summary", {}).get("passed", 0)
        failed = data.get("summary", {}).get("failed", 0)
        skipped = data.get("summary", {}).get("skipped", 0)

        print("=" * 50)
        print("📊 測試執行摘要")
        print("=" * 50)
        print(f"總測試數：{total}")
        print(f"✅ 通過：{passed}")
        print(f"❌ 失敗：{failed}")
        print(f"⏭️  跳過：{skipped}")
        print("=" * 50)

        if failed > 0:
            print("\n❌ 失敗的測試：")
            for test in data.get("tests", []):
                if test.get("outcome") == "failed":
                    print(f"  - {test.get('nodeid')}")

    except Exception as e:
        print(f"❌ 讀取測試結果失敗：{e}")


if __name__ == "__main__":
    generate_summary()

