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
 * 用例測試（Use Case Testing）
 * 
 * ISTQB 理論應用：
 * - 基於使用者場景設計測試案例
 * 
 * 主要用例：
 * 1. 用例 1：入庫新商品
 * 2. 用例 2：出庫商品（FIFO 策略）
 * 3. 用例 3：查詢即將過期商品
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BeverageUseCaseTest {

    @Autowired
    private BeverageService beverageService;

    @Autowired
    private BeverageRepository beverageRepository;

    @BeforeEach
    void setUp() {
        beverageRepository.deleteAll();
    }

    @Test
    @DisplayName("用例測試 - 用例 1：倉庫管理員入庫新商品")
    void testUseCase1_StockIn_NewBeverage() {
        // 前置條件：倉庫管理員登入（模擬）
        // 主要流程：輸入商品資訊 → 確認入庫 → 系統更新庫存

        StockInRequestDTO request = new StockInRequestDTO();
        request.setName("礦泉水");
        request.setQuantity(100);
        request.setProductionDate(LocalDate.of(2024, 1, 1));
        request.setExpiryDate(LocalDate.of(2025, 1, 1));

        // 執行入庫操作
        BeverageDTO result = beverageService.stockIn(request);

        // 後置條件驗證：庫存增加，記錄入庫時間
        assertNotNull(result);
        assertEquals(100, result.getQuantity());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        // 驗證庫存統計
        var statistics = beverageService.getStatistics();
        assertEquals(1, statistics.getTotalItems());
        assertEquals(100, statistics.getTotalQuantity());
    }

    @Test
    @DisplayName("用例測試 - 用例 2：倉庫管理員出庫商品（FIFO 策略）")
    void testUseCase2_StockOut_FIFOStrategy() {
        // 前置條件：有可用庫存
        // 建立多批次庫存（不同過期日期）
        StockInRequestDTO batch1 = new StockInRequestDTO();
        batch1.setName("礦泉水");
        batch1.setQuantity(100);
        batch1.setProductionDate(LocalDate.of(2024, 1, 1));
        batch1.setExpiryDate(LocalDate.of(2025, 1, 1)); // 最早過期
        beverageService.stockIn(batch1);

        StockInRequestDTO batch2 = new StockInRequestDTO();
        batch2.setName("礦泉水");
        batch2.setQuantity(100);
        batch2.setProductionDate(LocalDate.of(2024, 2, 1));
        batch2.setExpiryDate(LocalDate.of(2025, 2, 1)); // 較晚過期
        beverageService.stockIn(batch2);

        // 主要流程：選擇商品 → 輸入數量 → 系統按 FIFO 出庫
        StockOutRequestDTO stockOut = new StockOutRequestDTO();
        stockOut.setName("礦泉水");
        stockOut.setQuantity(50);

        // 執行出庫操作
        assertDoesNotThrow(() -> {
            beverageService.stockOut(stockOut);
        });

        // 後置條件驗證：庫存減少，優先出庫最早過期的
        var beverages = beverageRepository.findByName("礦泉水");
        // 驗證第一批次（最早過期）應該被優先出庫
        var firstBatch = beverages.stream()
                .filter(b -> b.getExpiryDate().equals(LocalDate.of(2025, 1, 1)))
                .findFirst();

        assertTrue(firstBatch.isPresent());
        // 第一批次應該剩下 50 瓶（100 - 50）
        assertEquals(50, firstBatch.get().getQuantity());
    }

    @Test
    @DisplayName("用例測試 - 用例 3：倉庫管理員查詢即將過期商品")
    void testUseCase3_QueryExpiringSoonBeverages() {
        // 前置條件：有庫存
        // 建立不同過期日期的商品
        StockInRequestDTO expiringSoon = new StockInRequestDTO();
        expiringSoon.setName("礦泉水");
        expiringSoon.setQuantity(100);
        expiringSoon.setProductionDate(LocalDate.now());
        expiringSoon.setExpiryDate(LocalDate.now().plusDays(5)); // 5 天後過期
        beverageService.stockIn(expiringSoon);

        StockInRequestDTO normal = new StockInRequestDTO();
        normal.setName("礦泉水");
        normal.setQuantity(100);
        normal.setProductionDate(LocalDate.now());
        normal.setExpiryDate(LocalDate.now().plusYears(1)); // 1 年後過期
        beverageService.stockIn(normal);

        // 主要流程：查詢即將過期商品 → 系統列出 7 天內過期的商品
        List<BeverageDTO> expiringSoonList = beverageService.getExpiringSoonBeverages();

        // 後置條件驗證：顯示過期提醒
        assertFalse(expiringSoonList.isEmpty());
        expiringSoonList.forEach(beverage -> {
            assertTrue(beverage.getExpiringSoon());
            assertTrue(beverage.getDaysUntilExpiry() <= 7);
        });
    }

    @Test
    @DisplayName("用例測試 - 用例 4：倉庫管理員查看庫存統計")
    void testUseCase4_ViewInventoryStatistics() {
        // 建立不同狀態的庫存
        // 正常庫存
        StockInRequestDTO normal = new StockInRequestDTO();
        normal.setName("礦泉水");
        normal.setQuantity(100);
        normal.setProductionDate(LocalDate.now());
        normal.setExpiryDate(LocalDate.now().plusYears(1));
        beverageService.stockIn(normal);

        // 已過期庫存
        StockInRequestDTO expired = new StockInRequestDTO();
        expired.setName("礦泉水");
        expired.setQuantity(50);
        expired.setProductionDate(LocalDate.now().minusYears(1));
        expired.setExpiryDate(LocalDate.now().minusDays(1));
        beverageService.stockIn(expired);

        // 即將過期庫存
        StockInRequestDTO expiringSoon = new StockInRequestDTO();
        expiringSoon.setName("礦泉水");
        expiringSoon.setQuantity(30);
        expiringSoon.setProductionDate(LocalDate.now());
        expiringSoon.setExpiryDate(LocalDate.now().plusDays(5));
        beverageService.stockIn(expiringSoon);

        // 查詢統計
        var statistics = beverageService.getStatistics();

        // 驗證統計資料
        assertEquals(3, statistics.getTotalItems());
        assertEquals(180, statistics.getTotalQuantity()); // 100 + 50 + 30
        assertEquals(50, statistics.getExpiredQuantity());
        assertEquals(30, statistics.getExpiringSoonQuantity());
    }

    @Test
    @DisplayName("用例測試 - 用例 5：倉庫管理員批量入庫")
    void testUseCase5_BatchStockIn() {
        // 模擬批量入庫場景
        for (int i = 1; i <= 5; i++) {
            StockInRequestDTO request = new StockInRequestDTO();
            request.setName("礦泉水");
            request.setQuantity(100);
            request.setProductionDate(LocalDate.of(2024, i, 1));
            request.setExpiryDate(LocalDate.of(2025, i, 1));
            beverageService.stockIn(request);
        }

        // 驗證所有批次都已入庫
        var statistics = beverageService.getStatistics();
        assertEquals(5, statistics.getTotalItems());
        assertEquals(500, statistics.getTotalQuantity());
    }
}

