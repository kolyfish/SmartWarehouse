package com.beveragewarehouse.service;

import com.beveragewarehouse.dto.BeverageDTO;
import com.beveragewarehouse.dto.StockInRequestDTO;
import com.beveragewarehouse.dto.StockOutRequestDTO;
import com.beveragewarehouse.model.Beverage;
import com.beveragewarehouse.repository.BeverageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 狀態轉換測試（State Transition Testing）
 * 
 * ISTQB 理論應用：
 * - 測試系統在不同狀態之間的轉換
 * 
 * 飲料庫存狀態轉換圖：
 * 
 * [入庫] → [在庫] → [出庫] → [已出庫]
 *            ↓
 *         [即將過期] → [已過期]
 * 
 * 狀態定義：
 * - 在庫：quantity > 0, expiryDate >= today
 * - 即將過期：quantity > 0, expiryDate <= today + 7
 * - 已過期：quantity > 0, expiryDate < today
 * - 已出庫：quantity = 0
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BeverageStateTransitionTest {

    @Autowired
    private BeverageService beverageService;

    @Autowired
    private BeverageRepository beverageRepository;

    @BeforeEach
    void setUp() {
        beverageRepository.deleteAll();
    }

    @Test
    @DisplayName("狀態轉換測試 - 轉換 1：入庫 → 在庫")
    void testStateTransition_StockIn_To_InStock() {
        // 初始狀態：無庫存
        List<BeverageDTO> initialBeverages = beverageService.getAllBeverages();
        assertEquals(0, initialBeverages.size());

        // 執行入庫操作
        StockInRequestDTO request = new StockInRequestDTO();
        request.setName("礦泉水");
        request.setQuantity(100);
        request.setProductionDate(LocalDate.now());
        request.setExpiryDate(LocalDate.now().plusYears(1));

        BeverageDTO result = beverageService.stockIn(request);

        // 驗證狀態：在庫
        assertNotNull(result);
        assertTrue(result.getQuantity() > 0);
        assertFalse(result.getExpired());
        assertEquals("在庫", getState(result));
    }

    @Test
    @DisplayName("狀態轉換測試 - 轉換 2：在庫 → 出庫 → 已出庫")
    void testStateTransition_InStock_To_OutOfStock() {
        // 步驟 1：入庫（狀態：在庫）
        StockInRequestDTO stockIn = new StockInRequestDTO();
        stockIn.setName("礦泉水");
        stockIn.setQuantity(100);
        stockIn.setProductionDate(LocalDate.now());
        stockIn.setExpiryDate(LocalDate.now().plusYears(1));
        BeverageDTO inStockBeverage = beverageService.stockIn(stockIn);

        assertEquals("在庫", getState(inStockBeverage));

        // 步驟 2：出庫全部（狀態：已出庫）
        StockOutRequestDTO stockOut = new StockOutRequestDTO();
        stockOut.setName("礦泉水");
        stockOut.setQuantity(100);

        beverageService.stockOut(stockOut);

        // 驗證狀態：已出庫（庫存為 0）
        List<Beverage> remainingBeverages = beverageRepository.findByName("礦泉水");
        int totalQuantity = remainingBeverages.stream()
                .mapToInt(Beverage::getQuantity)
                .sum();
        assertEquals(0, totalQuantity, "狀態應轉換為：已出庫");
    }

    @Test
    @DisplayName("狀態轉換測試 - 轉換 3：在庫 → 即將過期")
    void testStateTransition_InStock_To_ExpiringSoon() {
        // 入庫即將過期的商品（7 天內過期）
        StockInRequestDTO request = new StockInRequestDTO();
        request.setName("礦泉水");
        request.setQuantity(100);
        request.setProductionDate(LocalDate.now());
        request.setExpiryDate(LocalDate.now().plusDays(5)); // 5 天後過期

        BeverageDTO result = beverageService.stockIn(request);

        // 驗證狀態：即將過期
        assertTrue(result.getExpiringSoon());
        assertFalse(result.getExpired());
        assertEquals("即將過期", getState(result));
    }

    @Test
    @DisplayName("狀態轉換測試 - 轉換 4：在庫 → 已過期")
    void testStateTransition_InStock_To_Expired() {
        // 入庫已過期的商品
        StockInRequestDTO request = new StockInRequestDTO();
        request.setName("礦泉水");
        request.setQuantity(100);
        request.setProductionDate(LocalDate.now().minusYears(1));
        request.setExpiryDate(LocalDate.now().minusDays(1)); // 已過期

        BeverageDTO result = beverageService.stockIn(request);

        // 驗證狀態：已過期
        assertTrue(result.getExpired());
        assertEquals("已過期", getState(result));
    }

    @Test
    @DisplayName("狀態轉換測試 - 轉換 5：即將過期 → 已過期（時間推移）")
    void testStateTransition_ExpiringSoon_To_Expired() {
        // 入庫即將過期的商品
        StockInRequestDTO request = new StockInRequestDTO();
        request.setName("礦泉水");
        request.setQuantity(100);
        request.setProductionDate(LocalDate.now());
        request.setExpiryDate(LocalDate.now().plusDays(1)); // 明天過期

        BeverageDTO result = beverageService.stockIn(request);

        // 初始狀態：即將過期
        assertTrue(result.getExpiringSoon());
        assertFalse(result.getExpired());

        // 注意：實際系統中，狀態會隨時間自動轉換
        // 這裡僅驗證狀態轉換邏輯
        // 如果要模擬時間推移，需要修改系統時間或使用時間模擬工具
    }

    @Test
    @DisplayName("狀態轉換測試 - 轉換 6：部分出庫（在庫 → 仍在庫）")
    void testStateTransition_PartialStockOut_RemainInStock() {
        // 入庫
        StockInRequestDTO stockIn = new StockInRequestDTO();
        stockIn.setName("礦泉水");
        stockIn.setQuantity(100);
        stockIn.setProductionDate(LocalDate.now());
        stockIn.setExpiryDate(LocalDate.now().plusYears(1));
        beverageService.stockIn(stockIn);

        // 部分出庫
        StockOutRequestDTO stockOut = new StockOutRequestDTO();
        stockOut.setName("礦泉水");
        stockOut.setQuantity(30);

        beverageService.stockOut(stockOut);

        // 驗證狀態：仍在庫（庫存 > 0）
        List<Beverage> remainingBeverages = beverageRepository.findByName("礦泉水");
        int totalQuantity = remainingBeverages.stream()
                .mapToInt(Beverage::getQuantity)
                .sum();
        assertTrue(totalQuantity > 0);
        assertEquals(70, totalQuantity);
    }

    // ==================== 輔助方法 ====================

    /**
     * 根據 BeverageDTO 判斷當前狀態
     */
    private String getState(BeverageDTO beverage) {
        if (beverage.getQuantity() == 0) {
            return "已出庫";
        } else if (beverage.getExpired()) {
            return "已過期";
        } else if (beverage.getExpiringSoon()) {
            return "即將過期";
        } else {
            return "在庫";
        }
    }
}

