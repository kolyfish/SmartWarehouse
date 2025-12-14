"""Playwright 測試配置檔案"""
import pytest
from playwright.sync_api import Page, Browser, BrowserContext
import os
from datetime import datetime


@pytest.fixture(scope="session")
def browser_type_launch_args(browser_type_launch_args):
    """設定瀏覽器啟動參數"""
    return {
        **browser_type_launch_args,
        "headless": True,  # CI 環境使用 headless
        "args": ["--no-sandbox", "--disable-setuid-sandbox"],
    }


@pytest.fixture(scope="function")
def page(browser: Browser) -> Page:
    """建立測試頁面，設定預設超時時間"""
    context = browser.new_context(
        viewport={"width": 1920, "height": 1080},
        record_video_dir="test-results/videos/",
    )
    page = context.new_page()
    page.set_default_timeout(10_000)  # 10 秒預設超時

    yield page

    # 測試結束後關閉
    context.close()


@pytest.fixture(scope="session")
def test_timestamp():
    """取得測試執行時間戳記"""
    return datetime.now().isoformat()


@pytest.fixture(scope="session")
def project_id():
    """取得 GCP 專案 ID"""
    return os.getenv("GCP_PROJECT_ID", "test-project")


@pytest.fixture(scope="session")
def firestore_collection():
    """取得 Firestore 集合名稱"""
    return os.getenv("FIRESTORE_COLLECTION", "test_results")

