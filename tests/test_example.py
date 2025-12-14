"""範例測試：展示 Playwright 基本功能"""
import pytest
from playwright.sync_api import Page, expect
import os
from datetime import datetime


def test_google_search(page: Page):
    """測試 Google 搜尋功能"""
    # 導航到 Google
    page.goto("https://www.google.com")

    # 等待頁面載入
    page.wait_for_load_state("networkidle")

    # 搜尋 Playwright
    search_box = page.locator('textarea[name="q"]')
    search_box.fill("Playwright automation")
    search_box.press("Enter")

    # 等待搜尋結果
    page.wait_for_load_state("networkidle")

    # 驗證搜尋結果標題包含 Playwright
    results = page.locator("h3").first
    expect(results).to_be_visible()


def test_github_homepage(page: Page):
    """測試 GitHub 首頁載入"""
    page.goto("https://github.com")
    page.wait_for_load_state("networkidle")

    # 驗證 GitHub logo 可見
    logo = page.locator('svg[aria-label="GitHub"]')
    expect(logo).to_be_visible()

    # 驗證頁面標題
    expect(page).to_have_title("GitHub: Let's build from here · GitHub")


def test_wikipedia_search(page: Page):
    """測試 Wikipedia 搜尋功能"""
    page.goto("https://www.wikipedia.org")
    page.wait_for_load_state("networkidle")

    # 切換到英文版
    english_link = page.get_by_role("link", name="English")
    if english_link.is_visible():
        english_link.click()
        page.wait_for_load_state("networkidle")

    # 搜尋 "Python"
    search_input = page.locator('input[type="search"]')
    search_input.fill("Python programming language")
    search_input.press("Enter")

    # 等待搜尋結果
    page.wait_for_load_state("networkidle")

    # 驗證結果頁面標題包含 Python
    page_title = page.title()
    assert "Python" in page_title, f"頁面標題應包含 'Python'，實際為：{page_title}"

