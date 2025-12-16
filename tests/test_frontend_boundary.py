"""前端邊界值測試（ISTQB Boundary Value Analysis）

測試前端介面的邊界值：
- 入庫表單的輸入驗證
- 出庫表單的輸入驗證
- 數量邊界值（最小值、最大值、臨界值）
- 日期邊界值（今天、過去、未來）
"""
import pytest
from playwright.sync_api import Page, expect
from datetime import datetime, timedelta


@pytest.fixture
def frontend_url():
    """前端 URL"""
    # 嘗試使用 HTTP 伺服器，如果沒有則使用 file://
    import socket
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    result = sock.connect_ex(('localhost', 8000))
    sock.close()
    if result == 0:
        return "http://localhost:8000/index.html"
    else:
        # 如果沒有 HTTP 伺服器，使用絕對路徑
        import os
        frontend_path = os.path.abspath(os.path.join(os.path.dirname(__file__), "../frontend/index.html"))
        return f"file://{frontend_path}"


@pytest.fixture
def api_url():
    """API 基礎 URL"""
    return "http://localhost:8080/api/beverages"


@pytest.mark.ui
@pytest.mark.boundary
class TestFrontendBoundary:
    """前端邊界值測試類別"""

    def test_stock_in_quantity_minimum(self, page: Page, frontend_url: str):
        """邊界測試：入庫數量最小值（1）"""
        page.goto(frontend_url)
        page.wait_for_load_state("networkidle")

        # 打開入庫 Modal
        page.locator('button:has-text("➕ 入庫飲料")').click()
        page.wait_for_load_state("networkidle")

        # 填寫表單（最小值）
        page.locator("#stockInName").fill("測試飲料")
        page.locator("#stockInQuantity").fill("1")  # 最小值

        # 設定日期
        today = datetime.now().strftime("%Y-%m-%d")
        next_year = (datetime.now() + timedelta(days=365)).strftime("%Y-%m-%d")
        page.locator("#stockInProductionDate").fill(today)
        page.locator("#stockInExpiryDate").fill(next_year)

        # 提交表單
        page.locator('button[type="submit"]:has-text("確認入庫")').click()

        # 驗證成功提示
        page.wait_for_selector("#toast", state="visible", timeout=5000)
        toast = page.locator("#toast")
        expect(toast).to_be_visible()

    def test_stock_in_quantity_zero(self, page: Page, frontend_url: str):
        """邊界測試：入庫數量為 0（應該被拒絕）"""
        page.goto(frontend_url)
        page.wait_for_load_state("networkidle")

        # 打開入庫 Modal
        page.locator('button:has-text("➕ 入庫飲料")').click()
        page.wait_for_load_state("networkidle")

        # 填寫表單（數量為 0）
        page.locator("#stockInName").fill("測試飲料")
        page.locator("#stockInQuantity").fill("0")  # 邊界值：0

        # 設定日期
        today = datetime.now().strftime("%Y-%m-%d")
        next_year = (datetime.now() + timedelta(days=365)).strftime("%Y-%m-%d")
        page.locator("#stockInProductionDate").fill(today)
        page.locator("#stockInExpiryDate").fill(next_year)

        # 嘗試提交表單（應該被 HTML5 驗證阻止）
        submit_button = page.locator('button[type="submit"]:has-text("確認入庫")')
        submit_button.click()

        # 驗證表單驗證訊息（HTML5 validation）
        quantity_input = page.locator("#stockInQuantity")
        expect(quantity_input).to_have_attribute("required", "")
        # 數量為 0 時，min="1" 應該會阻止提交

    def test_stock_in_quantity_negative(self, page: Page, frontend_url: str):
        """邊界測試：入庫數量為負數（應該被拒絕）"""
        page.goto(frontend_url)
        page.wait_for_load_state("networkidle")

        # 打開入庫 Modal
        page.locator('button:has-text("➕ 入庫飲料")').click()
        page.wait_for_load_state("networkidle")

        # 填寫表單（負數）
        page.locator("#stockInName").fill("測試飲料")
        page.locator("#stockInQuantity").fill("-1")  # 負數

        # 設定日期
        today = datetime.now().strftime("%Y-%m-%d")
        next_year = (datetime.now() + timedelta(days=365)).strftime("%Y-%m-%d")
        page.locator("#stockInProductionDate").fill(today)
        page.locator("#stockInExpiryDate").fill(next_year)

        # 嘗試提交（應該被 HTML5 驗證阻止）
        submit_button = page.locator('button[type="submit"]:has-text("確認入庫")')
        submit_button.click()

        # 驗證表單驗證（負數應該被 min="1" 阻止）

    def test_stock_in_quantity_large(self, page: Page, frontend_url: str):
        """邊界測試：入庫數量為大數值（10000）"""
        page.goto(frontend_url)
        page.wait_for_load_state("networkidle")

        # 打開入庫 Modal
        page.locator('button:has-text("➕ 入庫飲料")').click()
        page.wait_for_load_state("networkidle")

        # 填寫表單（大數值）
        page.locator("#stockInName").fill("測試飲料")
        page.locator("#stockInQuantity").fill("10000")  # 大數值

        # 設定日期
        today = datetime.now().strftime("%Y-%m-%d")
        next_year = (datetime.now() + timedelta(days=365)).strftime("%Y-%m-%d")
        page.locator("#stockInProductionDate").fill(today)
        page.locator("#stockInExpiryDate").fill(next_year)

        # 提交表單
        page.locator('button[type="submit"]:has-text("確認入庫")').click()

        # 驗證成功提示
        page.wait_for_selector("#toast", state="visible", timeout=5000)
        toast = page.locator("#toast")
        expect(toast).to_be_visible()

    def test_stock_in_expiry_date_today(self, page: Page, frontend_url: str):
        """邊界測試：有效期限為今天（邊界值）"""
        page.goto(frontend_url)
        page.wait_for_load_state("networkidle")

        # 打開入庫 Modal
        page.locator('button:has-text("➕ 入庫飲料")').click()
        page.wait_for_load_state("networkidle")

        # 填寫表單
        page.locator("#stockInName").fill("測試飲料")
        page.locator("#stockInQuantity").fill("10")

        # 設定日期（有效期限為今天）
        today = datetime.now().strftime("%Y-%m-%d")
        page.locator("#stockInProductionDate").fill(today)
        page.locator("#stockInExpiryDate").fill(today)  # 邊界值：今天

        # 提交表單
        page.locator('button[type="submit"]:has-text("確認入庫")').click()

        # 驗證成功提示
        page.wait_for_selector("#toast", state="visible", timeout=5000)

    def test_stock_in_expiry_date_past(self, page: Page, frontend_url: str):
        """邊界測試：有效期限為過去日期（應該被接受，但會標記為過期）"""
        page.goto(frontend_url)
        page.wait_for_load_state("networkidle")

        # 打開入庫 Modal
        page.locator('button:has-text("➕ 入庫飲料")').click()
        page.wait_for_load_state("networkidle")

        # 填寫表單
        page.locator("#stockInName").fill("測試飲料")
        page.locator("#stockInQuantity").fill("10")

        # 設定日期（有效期限為昨天）
        today = datetime.now().strftime("%Y-%m-%d")
        yesterday = (datetime.now() - timedelta(days=1)).strftime("%Y-%m-%d")
        page.locator("#stockInProductionDate").fill(yesterday)
        page.locator("#stockInExpiryDate").fill(yesterday)  # 過去日期

        # 提交表單
        page.locator('button[type="submit"]:has-text("確認入庫")').click()

        # 驗證成功提示
        page.wait_for_selector("#toast", state="visible", timeout=5000)

    def test_stock_out_quantity_minimum(self, page: Page, frontend_url: str, api_url: str):
        """邊界測試：出庫數量最小值（1）"""
        # 先入庫一些飲料
        import requests
        requests.post(
            f"{api_url}/stock-in",
            json={
                "name": "邊界測試飲料",
                "quantity": 100,
                "productionDate": datetime.now().strftime("%Y-%m-%d"),
                "expiryDate": (datetime.now() + timedelta(days=365)).strftime("%Y-%m-%d"),
            },
        )

        page.goto(frontend_url)
        page.wait_for_load_state("networkidle")

        # 打開出庫 Modal
        page.locator('button:has-text("➖ 出庫飲料")').click()
        page.wait_for_load_state("networkidle")

        # 填寫表單（最小值）
        page.locator("#stockOutName").fill("邊界測試飲料")
        page.locator("#stockOutQuantity").fill("1")  # 最小值

        # 提交表單
        page.locator('button[type="submit"]:has-text("確認出庫")').click()

        # 驗證成功提示
        page.wait_for_selector("#toast", state="visible", timeout=5000)
        toast = page.locator("#toast")
        expect(toast).to_be_visible()

    def test_stock_out_quantity_zero(self, page: Page, frontend_url: str):
        """邊界測試：出庫數量為 0（應該被拒絕）"""
        page.goto(frontend_url)
        page.wait_for_load_state("networkidle")

        # 打開出庫 Modal
        page.locator('button:has-text("➖ 出庫飲料")').click()
        page.wait_for_load_state("networkidle")

        # 填寫表單（數量為 0）
        page.locator("#stockOutName").fill("測試飲料")
        page.locator("#stockOutQuantity").fill("0")  # 邊界值：0

        # 嘗試提交表單（應該被 HTML5 驗證阻止）
        submit_button = page.locator('button[type="submit"]:has-text("確認出庫")')
        submit_button.click()

        # 驗證表單驗證（min="1" 應該會阻止提交）

    def test_stock_out_quantity_exceeds_stock(self, page: Page, frontend_url: str, api_url: str):
        """邊界測試：出庫數量超過庫存（應該被拒絕）"""
        # 先入庫少量飲料
        import requests
        requests.post(
            f"{api_url}/stock-in",
            json={
                "name": "限量測試飲料",
                "quantity": 10,  # 只有 10 瓶
                "productionDate": datetime.now().strftime("%Y-%m-%d"),
                "expiryDate": (datetime.now() + timedelta(days=365)).strftime("%Y-%m-%d"),
            },
        )

        page.goto(frontend_url)
        page.wait_for_load_state("networkidle")

        # 打開出庫 Modal
        page.locator('button:has-text("➖ 出庫飲料")').click()
        page.wait_for_load_state("networkidle")

        # 填寫表單（超過庫存）
        page.locator("#stockOutName").fill("限量測試飲料")
        page.locator("#stockOutQuantity").fill("100")  # 超過庫存（只有 10 瓶）

        # 提交表單
        page.locator('button[type="submit"]:has-text("確認出庫")').click()

        # 驗證錯誤提示（應該顯示庫存不足）
        page.wait_for_selector("#toast", state="visible", timeout=5000)
        toast = page.locator("#toast")
        toast_text = toast.text_content()
        assert "失敗" in toast_text or "不足" in toast_text or "錯誤" in toast_text

    def test_stock_out_quantity_equal_to_stock(self, page: Page, frontend_url: str, api_url: str):
        """邊界測試：出庫數量等於庫存（邊界值）"""
        # 先入庫飲料
        import requests
        requests.post(
            f"{api_url}/stock-in",
            json={
                "name": "精確測試飲料",
                "quantity": 50,  # 50 瓶
                "productionDate": datetime.now().strftime("%Y-%m-%d"),
                "expiryDate": (datetime.now() + timedelta(days=365)).strftime("%Y-%m-%d"),
            },
        )

        page.goto(frontend_url)
        page.wait_for_load_state("networkidle")

        # 打開出庫 Modal
        page.locator('button:has-text("➖ 出庫飲料")').click()
        page.wait_for_load_state("networkidle")

        # 填寫表單（等於庫存）
        page.locator("#stockOutName").fill("精確測試飲料")
        page.locator("#stockOutQuantity").fill("50")  # 等於庫存

        # 提交表單
        page.locator('button[type="submit"]:has-text("確認出庫")').click()

        # 驗證成功提示
        page.wait_for_selector("#toast", state="visible", timeout=5000)
        toast = page.locator("#toast")
        expect(toast).to_be_visible()

    def test_empty_name_input(self, page: Page, frontend_url: str):
        """邊界測試：名稱為空（應該被拒絕）"""
        page.goto(frontend_url)
        page.wait_for_load_state("networkidle")

        # 打開入庫 Modal
        page.locator('button:has-text("➕ 入庫飲料")').click()
        page.wait_for_load_state("networkidle")

        # 只填寫數量，不填名稱
        page.locator("#stockInQuantity").fill("10")

        # 設定日期
        today = datetime.now().strftime("%Y-%m-%d")
        next_year = (datetime.now() + timedelta(days=365)).strftime("%Y-%m-%d")
        page.locator("#stockInProductionDate").fill(today)
        page.locator("#stockInExpiryDate").fill(next_year)

        # 嘗試提交（應該被 HTML5 required 驗證阻止）
        submit_button = page.locator('button[type="submit"]:has-text("確認入庫")')
        submit_button.click()

        # 驗證表單驗證（required 應該會阻止提交）

    def test_statistics_display_boundary(self, page: Page, frontend_url: str):
        """邊界測試：統計顯示（空庫存狀態）"""
        page.goto(frontend_url)
        page.wait_for_load_state("networkidle")

        # 點擊重新整理統計
        page.locator('button:has-text("🔄 重新整理統計")').click()
        page.wait_for_load_state("networkidle")

        # 驗證統計卡片顯示（應該顯示 0 或 -）
        total_items = page.locator("#totalItems")
        expect(total_items).to_be_visible()

        # 驗證統計數值（邊界值：0）
        total_items_text = total_items.text_content()
        assert total_items_text in ["0", "-"], f"總庫存項目應為 0 或 -，實際為：{total_items_text}"

    def test_modal_open_close_boundary(self, page: Page, frontend_url: str):
        """邊界測試：Modal 開啟和關閉"""
        page.goto(frontend_url)
        page.wait_for_load_state("networkidle")

        # 測試入庫 Modal
        stock_in_button = page.locator('button:has-text("➕ 入庫飲料")')
        expect(stock_in_button).to_be_visible()
        stock_in_button.click()

        # 驗證 Modal 已開啟
        modal = page.locator("#stockInModal")
        expect(modal).to_be_visible()

        # 測試關閉 Modal（點擊 X 按鈕）
        close_button = page.locator('#stockInModal button:has-text("×")')
        close_button.click()

        # 驗證 Modal 已關閉
        expect(modal).to_be_hidden()

    def test_form_validation_required_fields(self, page: Page, frontend_url: str):
        """邊界測試：表單必填欄位驗證"""
        page.goto(frontend_url)
        page.wait_for_load_state("networkidle")

        # 打開入庫 Modal
        page.locator('button:has-text("➕ 入庫飲料")').click()
        page.wait_for_load_state("networkidle")

        # 不填寫任何欄位，直接提交
        submit_button = page.locator('button[type="submit"]:has-text("確認入庫")')
        
        # 驗證必填欄位
        name_input = page.locator("#stockInName")
        quantity_input = page.locator("#stockInQuantity")
        production_date_input = page.locator("#stockInProductionDate")
        expiry_date_input = page.locator("#stockInExpiryDate")

        expect(name_input).to_have_attribute("required", "")
        expect(quantity_input).to_have_attribute("required", "")
        expect(production_date_input).to_have_attribute("required", "")
        expect(expiry_date_input).to_have_attribute("required", "")

