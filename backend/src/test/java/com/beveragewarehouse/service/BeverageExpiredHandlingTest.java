package com.beveragewarehouse.service;

import com.beveragewarehouse.dto.BeverageDTO;
import com.beveragewarehouse.dto.StockInRequestDTO;
import com.beveragewarehouse.dto.StockOutRequestDTO;
import com.beveragewarehouse.model.Beverage;
import com.beveragewarehouse.model.BeverageStatus;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 過期商品處理測試（業界標準流程）
 * 
 * 測試業界標準的過期商品處理機制：
 * 1. 自動隔離過期商品（QUARANTINED）
 * 2. 報廢流程（DISPOSED）
 * 3. 出庫時不能出庫過期商品
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BeverageExpiredHandlingTest {

    @Autowired
    private BeverageService beverageService;

    @Autowired
    private BeverageRepository beverageRepository;

    @BeforeEach
    void setUp() {
        beverageRepository.deleteAll();
    }

    @Test
    @DisplayName("業界標準：自動隔離過期商品")
    void testQuarantineExpiredBeverages() {
        // 建立已過期的商品
        StockInRequestDTO expiredStock = new StockInRequestDTO();
        expiredStock.setName("礦泉水");
        expiredStock.setQuantity(100);
        expiredStock.setProductionDate(LocalDate.now().minusYears(2));
        expiredStock.setExpiryDate(LocalDate.now().minusDays(1)); // 昨天過期
        beverageService.stockIn(expiredStock);

        // 建立未過期的商品
        StockInRequestDTO normalStock = new StockInRequestDTO();
        normalStock.setName("礦泉水");
        normalStock.setQuantity(50);
        normalStock.setProductionDate(LocalDate.now());
        normalStock.setExpiryDate(LocalDate.now().plusYears(1));
        beverageService.stockIn(normalStock);

        // 執行自動隔離
        int quarantinedCount = beverageService.quarantineExpiredBeverages();

        // 驗證：只有過期商品被隔離
        assertEquals(1, quarantinedCount, "應該隔離 1 個過期商品");

        // 驗證：過期商品狀態變為 QUARANTINED
        List<BeverageDTO> quarantined = beverageService.getQuarantinedBeverages();
        assertEquals(1, quarantined.size());
        assertEquals(BeverageStatus.QUARANTINED, quarantined.get(0).getStatus());

        // 驗證：正常商品狀態仍為 NORMAL
        List<Beverage> allBeverages = beverageRepository.findAll();
        Beverage normalBeverage = allBeverages.stream()
                .filter(b -> b.getExpiryDate().isAfter(LocalDate.now()))
                .findFirst()
                .orElseThrow();
        assertEquals(BeverageStatus.NORMAL, normalBeverage.getStatus());
    }

    @Test
    @DisplayName("業界標準：報廢隔離區中的商品")
    void testDisposeQuarantinedBeverage() {
        // 建立已過期的商品並隔離
        StockInRequestDTO expiredStock = new StockInRequestDTO();
        expiredStock.setName("礦泉水");
        expiredStock.setQuantity(100);
        expiredStock.setProductionDate(LocalDate.now().minusYears(2));
        expiredStock.setExpiryDate(LocalDate.now().minusDays(1));
        beverageService.stockIn(expiredStock);
        beverageService.quarantineExpiredBeverages();

        // 取得隔離區中的商品
        List<BeverageDTO> quarantined = beverageService.getQuarantinedBeverages();
        assertEquals(1, quarantined.size());
        Long quarantinedId = quarantined.get(0).getId();

        // 報廢商品
        String reason = "過期報廢，已超過有效期限";
        BeverageDTO disposed = beverageService.disposeBeverage(quarantinedId, reason);

        // 驗證：狀態變為 DISPOSED
        assertEquals(BeverageStatus.DISPOSED, disposed.getStatus());
        assertEquals(reason, disposed.getDisposalReason());
        assertNotNull(disposed.getDisposedAt());

        // 驗證：隔離區中沒有商品了
        List<BeverageDTO> remainingQuarantined = beverageService.getQuarantinedBeverages();
        assertEquals(0, remainingQuarantined.size());

        // 驗證：已報廢商品列表中有一筆
        List<BeverageDTO> disposedList = beverageService.getDisposedBeverages();
        assertEquals(1, disposedList.size());
        assertEquals(quarantinedId, disposedList.get(0).getId());
    }

    @Test
    @DisplayName("業界標準：不能報廢非隔離區的商品")
    void testDisposeNonQuarantinedBeverage_ShouldFail() {
        // 建立正常商品
        StockInRequestDTO normalStock = new StockInRequestDTO();
        normalStock.setName("礦泉水");
        normalStock.setQuantity(100);
        normalStock.setProductionDate(LocalDate.now());
        normalStock.setExpiryDate(LocalDate.now().plusYears(1));
        BeverageDTO normal = beverageService.stockIn(normalStock);

        // 嘗試報廢正常商品（應該失敗）
        assertThrows(RuntimeException.class, () -> {
            beverageService.disposeBeverage(normal.getId(), "測試報廢");
        }, "只能報廢隔離區中的商品");
    }

    @Test
    @DisplayName("業界標準：過期商品不能正常出庫")
    void testStockOut_ExpiredBeverage_ShouldNotBeAvailable() {
        // 建立已過期的商品並隔離
        StockInRequestDTO expiredStock = new StockInRequestDTO();
        expiredStock.setName("礦泉水");
        expiredStock.setQuantity(100);
        expiredStock.setProductionDate(LocalDate.now().minusYears(2));
        expiredStock.setExpiryDate(LocalDate.now().minusDays(1));
        beverageService.stockIn(expiredStock);
        beverageService.quarantineExpiredBeverages();

        // 建立未過期的商品
        StockInRequestDTO normalStock = new StockInRequestDTO();
        normalStock.setName("礦泉水");
        normalStock.setQuantity(50);
        normalStock.setProductionDate(LocalDate.now());
        normalStock.setExpiryDate(LocalDate.now().plusYears(1));
        beverageService.stockIn(normalStock);

        // 嘗試出庫（應該只出庫未過期的商品）
        StockOutRequestDTO stockOut = new StockOutRequestDTO();
        stockOut.setName("礦泉水");
        stockOut.setQuantity(50);

        // 應該成功（只出庫未過期的商品）
        assertDoesNotThrow(() -> {
            beverageService.stockOut(stockOut);
        });

        // 驗證：過期商品仍在隔離區中
        List<BeverageDTO> quarantined = beverageService.getQuarantinedBeverages();
        assertEquals(1, quarantined.size());
        assertEquals(100, quarantined.get(0).getQuantity());
    }

    @Test
    @DisplayName("業界標準：已報廢商品不能出庫")
    void testStockOut_DisposedBeverage_ShouldNotBeAvailable() {
        // 建立商品、過期、隔離、報廢
        StockInRequestDTO stock = new StockInRequestDTO();
        stock.setName("礦泉水");
        stock.setQuantity(100);
        stock.setProductionDate(LocalDate.now().minusYears(2));
        stock.setExpiryDate(LocalDate.now().minusDays(1));
        BeverageDTO created = beverageService.stockIn(stock);
        beverageService.quarantineExpiredBeverages();
        beverageService.disposeBeverage(created.getId(), "測試報廢");

        // 嘗試出庫（應該失敗，因為沒有可用的商品）
        StockOutRequestDTO stockOut = new StockOutRequestDTO();
        stockOut.setName("礦泉水");
        stockOut.setQuantity(50);

        assertThrows(RuntimeException.class, () -> {
            beverageService.stockOut(stockOut);
        }, "已報廢的商品不能出庫");
    }

    @Test
    @DisplayName("業界標準：查詢隔離區商品")
    void testGetQuarantinedBeverages() {
        // 建立多個過期商品
        for (int i = 0; i < 3; i++) {
            StockInRequestDTO expiredStock = new StockInRequestDTO();
            expiredStock.setName("礦泉水");
            expiredStock.setQuantity(100);
            expiredStock.setProductionDate(LocalDate.now().minusYears(2));
            expiredStock.setExpiryDate(LocalDate.now().minusDays(i + 1));
            beverageService.stockIn(expiredStock);
        }

        // 隔離過期商品
        int quarantinedCount = beverageService.quarantineExpiredBeverages();
        assertEquals(3, quarantinedCount);

        // 查詢隔離區商品
        List<BeverageDTO> quarantined = beverageService.getQuarantinedBeverages();
        assertEquals(3, quarantined.size());

        // 驗證所有商品都是 QUARANTINED 狀態
        quarantined.forEach(beverage -> {
            assertEquals(BeverageStatus.QUARANTINED, beverage.getStatus());
            assertTrue(beverage.getExpired());
        });
    }

    @Test
    @DisplayName("業界標準：查詢已報廢商品")
    void testGetDisposedBeverages() {
        // 建立並報廢多個商品
        for (int i = 0; i < 2; i++) {
            StockInRequestDTO expiredStock = new StockInRequestDTO();
            expiredStock.setName("礦泉水");
            expiredStock.setQuantity(100);
            expiredStock.setProductionDate(LocalDate.now().minusYears(2));
            expiredStock.setExpiryDate(LocalDate.now().minusDays(1));
            BeverageDTO created = beverageService.stockIn(expiredStock);
            beverageService.quarantineExpiredBeverages();
            beverageService.disposeBeverage(created.getId(), "過期報廢 " + i);
        }

        // 查詢已報廢商品
        List<BeverageDTO> disposed = beverageService.getDisposedBeverages();
        assertEquals(2, disposed.size());

        // 驗證所有商品都是 DISPOSED 狀態
        disposed.forEach(beverage -> {
            assertEquals(BeverageStatus.DISPOSED, beverage.getStatus());
            assertNotNull(beverage.getDisposalReason());
            assertNotNull(beverage.getDisposedAt());
        });
    }
}

