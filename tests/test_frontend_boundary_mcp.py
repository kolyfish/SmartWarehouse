"""使用 Playwright MCP 進行前端邊界值測試

這個測試檔案使用 Playwright 來測試前端介面的邊界值。
測試涵蓋：
- 輸入驗證邊界值（最小值、最大值、臨界值）
- 表單驗證（必填欄位、格式驗證）
- 業務邏輯邊界值（庫存不足、數量限制）
"""
import pytest
from playwright.sync_api import Page, expect
from datetime import datetime, timedelta
import requests


@pytest.fixture
def frontend_url():
    """前端 URL"""
    import socket
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    result = sock.connect_ex(('localhost', 8000))
    sock.close()
    if result == 0:
        return "http://localhost:8000/index.html"
    else:
        import os
        frontend_path = os.path.abspath(
            os.path.join(os.path.dirname(__file__), "../frontend/index.html")
        )
        return f"file://{frontend_path}"


@pytest.fixture
def api_url():
    """API 基礎 URL"""
    return "http://localhost:8080/api/beverages"


@pytest.mark.ui
@pytest.mark.boundary
class TestFrontendBoundaryMCP:
    """使用 Playwright MCP 的前端邊界值測試"""

    def test_page_load_and_elements(self, page: Page, frontend_url: str):
        """邊界測試：頁面載入和元素可見性"""
        page.goto(frontend_url)
        page.wait_for_load_state("networkidle")

        # 驗證主要元素存在
        expect(page.locator("h1:has-text('SmartWarehouse')")).to_be_visible()
        expect(page.locator('button:has-text("➕ 入庫飲料")')).to_be_visible()
        expect(page.locator('button:has-text("➖ 出庫飲料")')).to_be_visible()

        # 驗證統計卡片
        expect(page.locator("#totalItems")).to_be_visible()
        expect(page.locator("#totalQuantity")).to_be_visible()
        expect(page.locator("#expiredQuantity")).to_be_visible()
        expect(page.locator("#expiringSoonQuantity")).to_be_visible()

    def test_stock_in_boundary_values(self, page: Page, frontend_url: str):
        """邊界測試：入庫表單的邊界值"""
        page.goto(frontend_url)
        page.wait_for_load_state("networkidle")

        # 打開入庫 Modal
        page.locator('button:has-text("➕ 入庫飲料")').click()
        page.wait_for_load_state("networkidle")

        # 驗證 Modal 已開啟
        modal = page.locator("#stockInModal")
        expect(modal).to_be_visible()

        # 測試邊界值：數量 = 1（最小值）
        page.locator("#stockInName").fill("邊界測試飲料")
        page.locator("#stockInQuantity").fill("1")
        
        today = datetime.now().strftime("%Y-%m-%d")
        next_year = (datetime.now() + timedelta(days=365)).strftime("%Y-%m-%d")
        page.locator("#stockInProductionDate").fill(today)
        page.locator("#stockInExpiryDate").fill(next_year)

        # 驗證表單欄位
        quantity_input = page.locator("#stockInQuantity")
        expect(quantity_input).to_have_attribute("min", "1")
        expect(quantity_input).to_have_attribute("required", "")

    def test_stock_in_validation_boundary(self, page: Page, frontend_url: str):
        """邊界測試：入庫表單驗證邊界值"""
        page.goto(frontend_url)
        page.wait_for_load_state("networkidle")

        page.locator('button:has-text("➕ 入庫飲料")').click()
        page.wait_for_load_state("networkidle")

        # 測試數量 = 0（應該被 min="1" 阻止）
        quantity_input = page.locator("#stockInQuantity")
        quantity_input.fill("0")
        
        # 驗證 HTML5 驗證
        is_valid = page.evaluate("(input) => input.validity.valid", quantity_input)
        assert not is_valid, "數量為 0 時應該驗證失敗"

        # 測試數量 = 1（應該通過驗證）
        quantity_input.fill("1")
        is_valid = page.evaluate("(input) => input.validity.valid", quantity_input)
        assert is_valid, "數量為 1 時應該驗證通過"

    def test_stock_out_boundary_values(self, page: Page, frontend_url: str, api_url: str):
        """邊界測試：出庫表單的邊界值"""
        # 先入庫測試資料
        requests.post(
            f"{api_url}/stock-in",
            json={
                "name": "邊界出庫測試",
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

        # 驗證 Modal 已開啟
        modal = page.locator("#stockOutModal")
        expect(modal).to_be_visible()

        # 測試邊界值：數量 = 1（最小值）
        page.locator("#stockOutName").fill("邊界出庫測試")
        page.locator("#stockOutQuantity").fill("1")

        # 驗證表單欄位
        quantity_input = page.locator("#stockOutQuantity")
        expect(quantity_input).to_have_attribute("min", "1")
        expect(quantity_input).to_have_attribute("required", "")

    def test_stock_out_exceeds_stock_boundary(self, page: Page, frontend_url: str, api_url: str):
        """邊界測試：出庫數量超過庫存（邊界值）"""
        # 先入庫少量飲料
        requests.post(
            f"{api_url}/stock-in",
            json={
                "name": "限量測試",
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

        # 填寫超過庫存的數量
        page.locator("#stockOutName").fill("限量測試")
        page.locator("#stockOutQuantity").fill("100")  # 超過庫存

        # 提交表單
        page.locator('button[type="submit"]:has-text("確認出庫")').click()

        # 驗證錯誤提示
        page.wait_for_selector("#toast", state="visible", timeout=5000)
        toast = page.locator("#toast")
        expect(toast).to_be_visible()
        
        # 驗證錯誤訊息
        toast_text = toast.text_content()
        assert "失敗" in toast_text or "不足" in toast_text or "錯誤" in toast_text

    def test_date_boundary_values(self, page: Page, frontend_url: str):
        """邊界測試：日期邊界值"""
        page.goto(frontend_url)
        page.wait_for_load_state("networkidle")

        page.locator('button:has-text("➕ 入庫飲料")').click()
        page.wait_for_load_state("networkidle")

        # 測試日期：今天
        today = datetime.now().strftime("%Y-%m-%d")
        page.locator("#stockInProductionDate").fill(today)
        page.locator("#stockInExpiryDate").fill(today)

        # 測試日期：昨天（過去）
        yesterday = (datetime.now() - timedelta(days=1)).strftime("%Y-%m-%d")
        page.locator("#stockInProductionDate").fill(yesterday)
        page.locator("#stockInExpiryDate").fill(yesterday)

        # 測試日期：一年後（未來）
        next_year = (datetime.now() + timedelta(days=365)).strftime("%Y-%m-%d")
        page.locator("#stockInProductionDate").fill(today)
        page.locator("#stockInExpiryDate").fill(next_year)

        # 驗證日期輸入欄位
        production_date = page.locator("#stockInProductionDate")
        expiry_date = page.locator("#stockInExpiryDate")
        expect(production_date).to_have_attribute("required", "")
        expect(expiry_date).to_have_attribute("required", "")

    def test_form_required_fields_boundary(self, page: Page, frontend_url: str):
        """邊界測試：表單必填欄位驗證"""
        page.goto(frontend_url)
        page.wait_for_load_state("networkidle")

        page.locator('button:has-text("➕ 入庫飲料")').click()
        page.wait_for_load_state("networkidle")

        # 驗證所有必填欄位
        name_input = page.locator("#stockInName")
        quantity_input = page.locator("#stockInQuantity")
        production_date = page.locator("#stockInProductionDate")
        expiry_date = page.locator("#stockInExpiryDate")

        expect(name_input).to_have_attribute("required", "")
        expect(quantity_input).to_have_attribute("required", "")
        expect(production_date).to_have_attribute("required", "")
        expect(expiry_date).to_have_attribute("required", "")

        # 驗證數量欄位的最小值
        expect(quantity_input).to_have_attribute("min", "1")

    def test_statistics_boundary_values(self, page: Page, frontend_url: str):
        """邊界測試：統計顯示的邊界值（空庫存）"""
        page.goto(frontend_url)
        page.wait_for_load_state("networkidle")

        # 點擊重新整理統計
        page.locator('button:has-text("🔄 重新整理統計")').click()
        page.wait_for_load_state("networkidle")

        # 驗證統計卡片（邊界值：空庫存時應為 0）
        total_items = page.locator("#totalItems")
        total_quantity = page.locator("#totalQuantity")
        expired_quantity = page.locator("#expiredQuantity")
        expiring_soon_quantity = page.locator("#expiringSoonQuantity")

        expect(total_items).to_be_visible()
        expect(total_quantity).to_be_visible()
        expect(expired_quantity).to_be_visible()
        expect(expiring_soon_quantity).to_be_visible()

        # 驗證邊界值：空庫存時應為 0 或 -
        total_items_text = total_items.text_content()
        assert total_items_text in ["0", "-"], f"總庫存項目應為 0 或 -，實際為：{total_items_text}"

