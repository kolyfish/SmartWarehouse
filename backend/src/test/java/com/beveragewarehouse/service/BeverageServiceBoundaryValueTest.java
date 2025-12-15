package com.beveragewarehouse.service;

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

import static org.junit.jupiter.api.Assertions.*;

/**
 * 邊界值分析測試（Boundary Value Analysis）
 * 
 * ISTQB 理論應用：
 * - 測試邊界值和邊界值附近的值
 * - 最小值、最小值+1、正常值、最大值-1、最大值
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BeverageServiceBoundaryValueTest {

    @Autowired
    private BeverageService beverageService;

    @Autowired
    private BeverageRepository beverageRepository;

    @BeforeEach
    void setUp() {
        beverageRepository.deleteAll();
    }

    // ==================== 庫存數量邊界值測試 ====================

    @Test
    @DisplayName("邊界值測試 - 庫存最小值：0（零庫存）")
    void testStockOut_BoundaryValue_ZeroStock() {
        // 不建立任何庫存（庫存為 0）
        StockOutRequestDTO request = new StockOutRequestDTO();
        request.setName("礦泉水");
        request.setQuantity(1);

        // 應該失敗：庫存為 0
        assertThrows(RuntimeException.class, () -> {
            beverageService.stockOut(request);
        });
    }

    @Test
    @DisplayName("邊界值測試 - 庫存最小值+1：1（最小有效庫存）")
    void testStockOut_BoundaryValue_MinimumStock() {
        // 建立最小庫存：1
        StockInRequestDTO stockIn = new StockInRequestDTO();
        stockIn.setName("礦泉水");
        stockIn.setQuantity(1); // 最小值+1
        stockIn.setProductionDate(LocalDate.now());
        stockIn.setExpiryDate(LocalDate.now().plusYears(1));
        beverageService.stockIn(stockIn);

        // 出庫 1 瓶應該成功
        StockOutRequestDTO stockOut = new StockOutRequestDTO();
        stockOut.setName("礦泉水");
        stockOut.setQuantity(1);

        assertDoesNotThrow(() -> {
            beverageService.stockOut(stockOut);
        });

        // 驗證庫存為 0
        int remainingStock = beverageRepository.findByName("礦泉水").stream()
                .mapToInt(Beverage::getQuantity)
                .sum();
        assertEquals(0, remainingStock);
    }

    @Test
    @DisplayName("邊界值測試 - 庫存正常值：100（中間值）")
    void testStockOut_BoundaryValue_NormalStock() {
        StockInRequestDTO stockIn = new StockInRequestDTO();
        stockIn.setName("礦泉水");
        stockIn.setQuantity(100); // 正常值
        stockIn.setProductionDate(LocalDate.now());
        stockIn.setExpiryDate(LocalDate.now().plusYears(1));
        beverageService.stockIn(stockIn);

        StockOutRequestDTO stockOut = new StockOutRequestDTO();
        stockOut.setName("礦泉水");
        stockOut.setQuantity(50);

        assertDoesNotThrow(() -> {
            beverageService.stockOut(stockOut);
        });
    }

    @Test
    @DisplayName("邊界值測試 - 出庫數量邊界：等於庫存")
    void testStockOut_BoundaryValue_EqualStock() {
        StockInRequestDTO stockIn = new StockInRequestDTO();
        stockIn.setName("礦泉水");
        stockIn.setQuantity(100);
        stockIn.setProductionDate(LocalDate.now());
        stockIn.setExpiryDate(LocalDate.now().plusYears(1));
        beverageService.stockIn(stockIn);

        // 出庫數量等於庫存（邊界值）
        StockOutRequestDTO stockOut = new StockOutRequestDTO();
        stockOut.setName("礦泉水");
        stockOut.setQuantity(100); // 等於庫存

        assertDoesNotThrow(() -> {
            beverageService.stockOut(stockOut);
        });

        // 驗證庫存為 0
        int remainingStock = beverageRepository.findByName("礦泉水").stream()
                .mapToInt(Beverage::getQuantity)
                .sum();
        assertEquals(0, remainingStock);
    }

    @Test
    @DisplayName("邊界值測試 - 出庫數量邊界：庫存+1（超過庫存）")
    void testStockOut_BoundaryValue_ExceedsStock() {
        StockInRequestDTO stockIn = new StockInRequestDTO();
        stockIn.setName("礦泉水");
        stockIn.setQuantity(100);
        stockIn.setProductionDate(LocalDate.now());
        stockIn.setExpiryDate(LocalDate.now().plusYears(1));
        beverageService.stockIn(stockIn);

        // 出庫數量 = 庫存 + 1（邊界值）
        StockOutRequestDTO stockOut = new StockOutRequestDTO();
        stockOut.setName("礦泉水");
        stockOut.setQuantity(101); // 庫存 + 1

        // 應該失敗
        assertThrows(RuntimeException.class, () -> {
            beverageService.stockOut(stockOut);
        });
    }

    // ==================== 日期邊界值測試 ====================

    @Test
    @DisplayName("邊界值測試 - 過期日期：今天（剛好過期）")
    void testExpiryDate_BoundaryValue_Today() {
        StockInRequestDTO request = new StockInRequestDTO();
        request.setName("礦泉水");
        request.setQuantity(100);
        request.setProductionDate(LocalDate.now().minusYears(1));
        request.setExpiryDate(LocalDate.now()); // 邊界值：今天

        var result = beverageService.stockIn(request);

        // 驗證是否標記為過期
        var beverages = beverageService.getExpiredBeverages();
        assertTrue(beverages.stream().anyMatch(b -> b.getId().equals(result.getId())));
    }

    @Test
    @DisplayName("邊界值測試 - 過期日期：今天+1（明天過期，未過期）")
    void testExpiryDate_BoundaryValue_Tomorrow() {
        StockInRequestDTO request = new StockInRequestDTO();
        request.setName("礦泉水");
        request.setQuantity(100);
        request.setProductionDate(LocalDate.now());
        request.setExpiryDate(LocalDate.now().plusDays(1)); // 邊界值：明天

        var result = beverageService.stockIn(request);

        // 驗證未過期
        assertFalse(result.getExpired());
    }

    @Test
    @DisplayName("邊界值測試 - 即將過期：7 天後（邊界值）")
    void testExpiringSoon_BoundaryValue_SevenDays() {
        StockInRequestDTO request = new StockInRequestDTO();
        request.setName("礦泉水");
        request.setQuantity(100);
        request.setProductionDate(LocalDate.now());
        request.setExpiryDate(LocalDate.now().plusDays(7)); // 邊界值：7 天後

        var result = beverageService.stockIn(request);

        // 驗證標記為即將過期
        assertTrue(result.getExpiringSoon());
        assertEquals(7, result.getDaysUntilExpiry());
    }

    @Test
    @DisplayName("邊界值測試 - 即將過期：8 天後（超過邊界）")
    void testExpiringSoon_BoundaryValue_EightDays() {
        StockInRequestDTO request = new StockInRequestDTO();
        request.setName("礦泉水");
        request.setQuantity(100);
        request.setProductionDate(LocalDate.now());
        request.setExpiryDate(LocalDate.now().plusDays(8)); // 超過邊界：8 天後

        var result = beverageService.stockIn(request);

        // 驗證未標記為即將過期
        assertFalse(result.getExpiringSoon());
        assertEquals(8, result.getDaysUntilExpiry());
    }

    // ==================== 數量邊界值組合測試 ====================

    @Test
    @DisplayName("邊界值測試 - 組合：庫存 1，出庫 1（最小有效組合）")
    void testBoundaryValue_Combination_MinimumValid() {
        StockInRequestDTO stockIn = new StockInRequestDTO();
        stockIn.setName("礦泉水");
        stockIn.setQuantity(1); // 最小庫存
        stockIn.setProductionDate(LocalDate.now());
        stockIn.setExpiryDate(LocalDate.now().plusYears(1));
        beverageService.stockIn(stockIn);

        StockOutRequestDTO stockOut = new StockOutRequestDTO();
        stockOut.setName("礦泉水");
        stockOut.setQuantity(1); // 最小出庫數量

        assertDoesNotThrow(() -> {
            beverageService.stockOut(stockOut);
        });
    }
}

