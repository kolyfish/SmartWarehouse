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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * V 模型完整測試案例
 * 
 * 展示從單元測試到驗收測試的完整 V 模型應用
 * 
 * V 模型層級：
 * 1. 單元測試：等價類、邊界值、決策表
 * 2. 整合測試：狀態轉換
 * 3. 系統測試：用例測試、高併發
 * 4. 驗收測試：驗收標準驗證
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BeverageServiceVModelTest {

    @Autowired
    private BeverageService beverageService;

    @Autowired
    private BeverageRepository beverageRepository;

    @BeforeEach
    void setUp() {
        beverageRepository.deleteAll();
    }

    // ==================== V 模型第一層：單元測試 ====================

    @Test
    @DisplayName("V 模型 - 單元測試：等價類劃分 - 有效輸入")
    void testVModel_Unit_EquivalencePartitioning_Valid() {
        // 有效等價類：正常庫存數量（1-10000）
        StockInRequestDTO request = new StockInRequestDTO();
        request.setName("礦泉水");
        request.setQuantity(500); // 代表 1-10000 之間的所有整數
        request.setProductionDate(LocalDate.now());
        request.setExpiryDate(LocalDate.now().plusYears(1));

        var result = beverageService.stockIn(request);

        assertNotNull(result);
        assertEquals(500, result.getQuantity());
    }

    @Test
    @DisplayName("V 模型 - 單元測試：邊界值分析 - 下邊界（0, 1, 2）")
    void testVModel_Unit_BoundaryValue_LowerBound() {
        // 下邊界測試：0, 1, 2

        // 測試 0（無效）
        StockInRequestDTO request0 = new StockInRequestDTO();
        request0.setName("礦泉水");
        request0.setQuantity(0);
        request0.setProductionDate(LocalDate.now());
        request0.setExpiryDate(LocalDate.now().plusYears(1));
        assertThrows(Exception.class, () -> beverageService.stockIn(request0));

        // 測試 1（有效，最小值）
        StockInRequestDTO request1 = new StockInRequestDTO();
        request1.setName("礦泉水");
        request1.setQuantity(1); // 邊界值：最小值
        request1.setProductionDate(LocalDate.now());
        request1.setExpiryDate(LocalDate.now().plusYears(1));
        assertDoesNotThrow(() -> beverageService.stockIn(request1));

        // 測試 2（有效，最小值+1）
        StockInRequestDTO request2 = new StockInRequestDTO();
        request2.setName("礦泉水");
        request2.setQuantity(2); // 邊界值：最小值+1
        request2.setProductionDate(LocalDate.now());
        request2.setExpiryDate(LocalDate.now().plusYears(1));
        assertDoesNotThrow(() -> beverageService.stockIn(request2));
    }

    @Test
    @DisplayName("V 模型 - 單元測試：決策表 - 規則 1（成功出庫）")
    void testVModel_Unit_DecisionTable_Rule1() {
        // 決策表規則 1：庫存 > 0, 請求 <= 庫存, 未過期 → 成功出庫
        StockInRequestDTO stockIn = new StockInRequestDTO();
        stockIn.setName("礦泉水");
        stockIn.setQuantity(100); // 庫存 > 0
        stockIn.setProductionDate(LocalDate.now());
        stockIn.setExpiryDate(LocalDate.now().plusYears(1)); // 未過期
        beverageService.stockIn(stockIn);

        StockOutRequestDTO stockOut = new StockOutRequestDTO();
        stockOut.setName("礦泉水");
        stockOut.setQuantity(50); // 請求 <= 庫存

        // 預期：成功出庫
        assertDoesNotThrow(() -> {
            beverageService.stockOut(stockOut);
        });
    }

    // ==================== V 模型第二層：整合測試 ====================

    @Test
    @DisplayName("V 模型 - 整合測試：狀態轉換 - 在庫 → 出庫 → 已出庫")
    void testVModel_Integration_StateTransition() {
        // 整合測試：Service + Repository 的狀態轉換

        // 步驟 1：入庫（狀態：在庫）
        StockInRequestDTO stockIn = new StockInRequestDTO();
        stockIn.setName("礦泉水");
        stockIn.setQuantity(100);
        stockIn.setProductionDate(LocalDate.now());
        stockIn.setExpiryDate(LocalDate.now().plusYears(1));
        var inStockBeverage = beverageService.stockIn(stockIn);

        // 驗證狀態：在庫
        assertTrue(inStockBeverage.getQuantity() > 0);
        assertFalse(inStockBeverage.getExpired());

        // 步驟 2：出庫全部（狀態：已出庫）
        StockOutRequestDTO stockOut = new StockOutRequestDTO();
        stockOut.setName("礦泉水");
        stockOut.setQuantity(100);

        beverageService.stockOut(stockOut);

        // 驗證狀態轉換：已出庫
        List<Beverage> remainingBeverages = beverageRepository.findByName("礦泉水");
        int totalQuantity = remainingBeverages.stream()
                .mapToInt(Beverage::getQuantity)
                .sum();
        assertEquals(0, totalQuantity, "狀態應轉換為：已出庫");
    }

    // ==================== V 模型第三層：系統測試 ====================

    @Test
    @DisplayName("V 模型 - 系統測試：用例測試 - 基本流（Happy Path）")
    void testVModel_System_UseCase_HappyPath() {
        // 系統測試：完整的端到端流程

        // 基本流：倉庫管理員順利完成入庫和出庫
        // 1. 入庫新商品
        StockInRequestDTO stockIn = new StockInRequestDTO();
        stockIn.setName("礦泉水");
        stockIn.setQuantity(100);
        stockIn.setProductionDate(LocalDate.now());
        stockIn.setExpiryDate(LocalDate.now().plusYears(1));
        var result = beverageService.stockIn(stockIn);

        // 驗證入庫成功
        assertNotNull(result);
        assertEquals(100, result.getQuantity());

        // 2. 出庫商品
        StockOutRequestDTO stockOut = new StockOutRequestDTO();
        stockOut.setName("礦泉水");
        stockOut.setQuantity(50);

        assertDoesNotThrow(() -> {
            beverageService.stockOut(stockOut);
        });

        // 3. 查詢庫存統計
        var statistics = beverageService.getStatistics();
        assertEquals(50, statistics.getTotalQuantity());
    }

    @Test
    @DisplayName("V 模型 - 系統測試：用例測試 - 替代流（Alternative Path）")
    void testVModel_System_UseCase_AlternativePath() {
        // 替代流：庫存不足場景

        // 建立少量庫存
        StockInRequestDTO stockIn = new StockInRequestDTO();
        stockIn.setName("礦泉水");
        stockIn.setQuantity(100);
        stockIn.setProductionDate(LocalDate.now());
        stockIn.setExpiryDate(LocalDate.now().plusYears(1));
        beverageService.stockIn(stockIn);

        // 嘗試出庫超過庫存的數量
        StockOutRequestDTO stockOut = new StockOutRequestDTO();
        stockOut.setName("礦泉水");
        stockOut.setQuantity(200); // 超過庫存

        // 預期：失敗，顯示錯誤訊息
        assertThrows(RuntimeException.class, () -> {
            beverageService.stockOut(stockOut);
        });
    }

    // ==================== V 模型第四層：驗收測試 ====================

    @Test
    @DisplayName("V 模型 - 驗收測試：驗收標準 1 - 庫存扣減精確匹配")
    void testVModel_Acceptance_AC1_InventoryPrecision() {
        // User Story：作為倉儲管理員，我希望出庫時庫存扣減必須精確

        // AC1：庫存扣減數量 = 訂單成功數 × 每單數量

        // 建立初始庫存
        StockInRequestDTO stockIn = new StockInRequestDTO();
        stockIn.setName("礦泉水");
        stockIn.setQuantity(1000);
        stockIn.setProductionDate(LocalDate.now());
        stockIn.setExpiryDate(LocalDate.now().plusYears(1));
        beverageService.stockIn(stockIn);

        // 執行出庫
        StockOutRequestDTO stockOut = new StockOutRequestDTO();
        stockOut.setName("礦泉水");
        stockOut.setQuantity(50);

        beverageService.stockOut(stockOut);

        // 驗證 AC1：庫存扣減精確匹配
        var statistics = beverageService.getStatistics();
        assertEquals(950, statistics.getTotalQuantity(), 
                "驗收標準 1：庫存扣減必須精確（1000 - 50 = 950）");
    }

    @Test
    @DisplayName("V 模型 - 驗收測試：驗收標準 2 - 無負庫存")
    void testVModel_Acceptance_AC2_NoNegativeStock() {
        // AC2：不能出現負庫存

        // 建立初始庫存
        StockInRequestDTO stockIn = new StockInRequestDTO();
        stockIn.setName("礦泉水");
        stockIn.setQuantity(100);
        stockIn.setProductionDate(LocalDate.now());
        stockIn.setExpiryDate(LocalDate.now().plusYears(1));
        beverageService.stockIn(stockIn);

        // 嘗試出庫超過庫存的數量
        StockOutRequestDTO stockOut = new StockOutRequestDTO();
        stockOut.setName("礦泉水");
        stockOut.setQuantity(200);

        // 應該失敗，不會出現負庫存
        assertThrows(RuntimeException.class, () -> {
            beverageService.stockOut(stockOut);
        });

        // 驗證庫存仍然 >= 0
        var statistics = beverageService.getStatistics();
        assertTrue(statistics.getTotalQuantity() >= 0, 
                "驗收標準 2：不能出現負庫存");
    }

    @Test
    @DisplayName("V 模型 - 驗收測試：驗收標準 3 - 資料一致性")
    void testVModel_Acceptance_AC3_DataConsistency() {
        // AC3：初始庫存 - 成功出庫總數 = 最終庫存

        int initialStock = 1000;

        // 建立初始庫存
        StockInRequestDTO stockIn = new StockInRequestDTO();
        stockIn.setName("礦泉水");
        stockIn.setQuantity(initialStock);
        stockIn.setProductionDate(LocalDate.now());
        stockIn.setExpiryDate(LocalDate.now().plusYears(1));
        beverageService.stockIn(stockIn);

        // 執行多次出庫
        int totalOut = 0;
        for (int i = 0; i < 5; i++) {
            StockOutRequestDTO stockOut = new StockOutRequestDTO();
            stockOut.setName("礦泉水");
            stockOut.setQuantity(50);
            beverageService.stockOut(stockOut);
            totalOut += 50;
        }

        // 驗證 AC3：資料一致性
        var statistics = beverageService.getStatistics();
        int finalStock = (int) statistics.getTotalQuantity();
        assertEquals(initialStock - totalOut, finalStock, 
                "驗收標準 3：初始庫存 - 成功出庫總數 = 最終庫存");
    }
}

