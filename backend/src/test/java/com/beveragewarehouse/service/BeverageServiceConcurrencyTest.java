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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 高併發測試 - 驗證悲觀鎖機制
 * 
 * 測試目標：
 * 1. 驗證在高併發下，庫存扣減數量必須與訂單成功數精確匹配
 * 2. 不能出現負庫存
 * 3. 驗證 DB Lock（悲觀鎖）機制是否生效
 * 
 * 測試場景：
 * - 模擬 50-100 個執行緒同時對同一個熱門商品進行出庫操作
 * - 使用 Synchronizing Timer 概念（CountDownLatch）確保同時執行
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BeverageServiceConcurrencyTest {

    @Autowired
    private BeverageService beverageService;

    @Autowired
    private BeverageRepository beverageRepository;

    private static final String BEVERAGE_NAME = "礦泉水";
    private static final int INITIAL_STOCK = 1000; // 初始庫存
    private static final int THREAD_COUNT = 100; // 併發執行緒數
    private static final int QUANTITY_PER_THREAD = 5; // 每個執行緒出庫數量

    @BeforeEach
    void setUp() {
        // 清理資料庫
        beverageRepository.deleteAll();

        // 建立初始庫存
        StockInRequestDTO stockInRequest = new StockInRequestDTO();
        stockInRequest.setName(BEVERAGE_NAME);
        stockInRequest.setQuantity(INITIAL_STOCK);
        stockInRequest.setProductionDate(LocalDate.now());
        stockInRequest.setExpiryDate(LocalDate.now().plusYears(1));

        beverageService.stockIn(stockInRequest);
    }

    @Test
    @DisplayName("高併發出庫測試 - 驗證資料一致性")
    void testConcurrentStockOut_ShouldMaintainDataConsistency() throws InterruptedException {
        // 預期：所有執行緒總共出庫 THREAD_COUNT * QUANTITY_PER_THREAD 瓶
        int expectedTotalOut = THREAD_COUNT * QUANTITY_PER_THREAD;
        int expectedRemainingStock = INITIAL_STOCK - expectedTotalOut;

        // 使用 CountDownLatch 模擬 Synchronizing Timer（確保所有執行緒同時開始）
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(THREAD_COUNT);

        // 記錄成功和失敗的執行緒數
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<Exception> exceptions = new ArrayList<>();

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

        // 建立 100 個執行緒，每個執行緒嘗試出庫 5 瓶
        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.submit(() -> {
                try {
                    // 等待所有執行緒準備好
                    startLatch.await();

                    // 執行出庫操作
                    StockOutRequestDTO request = new StockOutRequestDTO();
                    request.setName(BEVERAGE_NAME);
                    request.setQuantity(QUANTITY_PER_THREAD);

                    beverageService.stockOut(request);
                    successCount.incrementAndGet();

                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    exceptions.add(e);
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // 等待所有執行緒準備好，然後同時開始（模擬 Synchronizing Timer）
        Thread.sleep(100); // 給執行緒一點時間準備
        startLatch.countDown(); // 同時開始

        // 等待所有執行緒完成
        endLatch.await();

        executorService.shutdown();

        // 驗證結果
        int actualRemainingStock = beverageRepository.findByName(BEVERAGE_NAME).stream()
                .mapToInt(Beverage::getQuantity)
                .sum();

        int actualTotalOut = INITIAL_STOCK - actualRemainingStock;

        // 驗收標準 1：庫存扣減數量必須與訂單成功數精確匹配
        assertEquals(
                successCount.get() * QUANTITY_PER_THREAD,
                actualTotalOut,
                "庫存扣減數量必須與訂單成功數精確匹配"
        );

        // 驗收標準 2：不能出現負庫存
        assertTrue(
                actualRemainingStock >= 0,
                "庫存不能為負數，實際庫存: " + actualRemainingStock
        );

        // 驗收標準 3：總庫存應該等於初始庫存減去成功出庫的數量
        assertEquals(
                expectedRemainingStock,
                actualRemainingStock,
                "最終庫存應該等於初始庫存減去成功出庫的數量"
        );

        // 輸出測試結果
        System.out.println("========================================");
        System.out.println("高併發測試結果：");
        System.out.println("初始庫存: " + INITIAL_STOCK);
        System.out.println("併發執行緒數: " + THREAD_COUNT);
        System.out.println("每個執行緒出庫數量: " + QUANTITY_PER_THREAD);
        System.out.println("成功出庫的執行緒數: " + successCount.get());
        System.out.println("失敗的執行緒數: " + failureCount.get());
        System.out.println("實際出庫總數: " + actualTotalOut);
        System.out.println("預期出庫總數: " + expectedTotalOut);
        System.out.println("最終庫存: " + actualRemainingStock);
        System.out.println("預期庫存: " + expectedRemainingStock);
        System.out.println("========================================");

        // 如果有失敗的執行緒，輸出錯誤訊息
        if (!exceptions.isEmpty()) {
            System.out.println("失敗原因（前 5 個）：");
            exceptions.stream().limit(5).forEach(e -> 
                System.out.println("  - " + e.getMessage())
            );
        }
    }

    @Test
    @DisplayName("高併發出庫測試 - 庫存不足場景")
    void testConcurrentStockOut_WithInsufficientStock() throws InterruptedException {
        // 設定：初始庫存 100，100 個執行緒各出庫 5 瓶（總需求 500 瓶）
        int smallStock = 100;
        int threadCount = 50;
        int quantityPerThread = 5;

        // 清理並重新建立庫存
        beverageRepository.deleteAll();
        StockInRequestDTO stockInRequest = new StockInRequestDTO();
        stockInRequest.setName(BEVERAGE_NAME);
        stockInRequest.setQuantity(smallStock);
        stockInRequest.setProductionDate(LocalDate.now());
        stockInRequest.setExpiryDate(LocalDate.now().plusYears(1));
        beverageService.stockIn(stockInRequest);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();

                    StockOutRequestDTO request = new StockOutRequestDTO();
                    request.setName(BEVERAGE_NAME);
                    request.setQuantity(quantityPerThread);

                    beverageService.stockOut(request);
                    successCount.incrementAndGet();

                } catch (Exception e) {
                    failureCount.incrementAndGet();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        Thread.sleep(100);
        startLatch.countDown();
        endLatch.await();
        executorService.shutdown();

        // 驗證：最終庫存應該為 0，且不能為負數
        int actualRemainingStock = beverageRepository.findByName(BEVERAGE_NAME).stream()
                .mapToInt(Beverage::getQuantity)
                .sum();

        assertTrue(actualRemainingStock >= 0, "庫存不能為負數");
        assertEquals(smallStock, successCount.get() * quantityPerThread, 
                "成功出庫的總數應該等於初始庫存");

        System.out.println("庫存不足場景測試：");
        System.out.println("初始庫存: " + smallStock);
        System.out.println("成功出庫執行緒: " + successCount.get());
        System.out.println("失敗執行緒: " + failureCount.get());
        System.out.println("最終庫存: " + actualRemainingStock);
    }
}

