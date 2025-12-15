package com.beveragewarehouse.controller;

import com.beveragewarehouse.dto.BeverageDTO;
import com.beveragewarehouse.dto.BeverageRequestDTO;
import com.beveragewarehouse.dto.StockInRequestDTO;
import com.beveragewarehouse.dto.StockOutRequestDTO;
import com.beveragewarehouse.service.BeverageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 飲料管理 REST API Controller
 * 
 * 提供完整的 CRUD 操作和庫存管理功能
 */
@RestController
@RequestMapping("/api/beverages")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BeverageController {
    
    private final BeverageService beverageService;
    
    /**
     * 取得所有飲料
     */
    @GetMapping
    public ResponseEntity<List<BeverageDTO>> getAllBeverages() {
        List<BeverageDTO> beverages = beverageService.getAllBeverages();
        return ResponseEntity.ok(beverages);
    }
    
    /**
     * 根據 ID 取得飲料
     */
    @GetMapping("/{id}")
    public ResponseEntity<BeverageDTO> getBeverageById(@PathVariable Long id) {
        BeverageDTO beverage = beverageService.getBeverageById(id);
        return ResponseEntity.ok(beverage);
    }
    
    /**
     * 新增飲料（入庫）
     */
    @PostMapping("/stock-in")
    public ResponseEntity<BeverageDTO> stockIn(@Valid @RequestBody StockInRequestDTO request) {
        BeverageDTO beverage = beverageService.stockIn(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(beverage);
    }
    
    /**
     * 出庫飲料
     */
    @PostMapping("/stock-out")
    public ResponseEntity<Map<String, String>> stockOut(@Valid @RequestBody StockOutRequestDTO request) {
        beverageService.stockOut(request);
        return ResponseEntity.ok(Map.of(
                "message", "成功出庫 " + request.getQuantity() + " 瓶 " + request.getName()
        ));
    }
    
    /**
     * 更新飲料資訊
     */
    @PutMapping("/{id}")
    public ResponseEntity<BeverageDTO> updateBeverage(
            @PathVariable Long id,
            @Valid @RequestBody BeverageRequestDTO request) {
        BeverageDTO beverage = beverageService.updateBeverage(id, request);
        return ResponseEntity.ok(beverage);
    }
    
    /**
     * 刪除飲料
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteBeverage(@PathVariable Long id) {
        beverageService.deleteBeverage(id);
        return ResponseEntity.ok(Map.of("message", "成功刪除飲料，ID: " + id));
    }
    
    /**
     * 取得所有已過期的飲料
     */
    @GetMapping("/expired")
    public ResponseEntity<List<BeverageDTO>> getExpiredBeverages() {
        List<BeverageDTO> beverages = beverageService.getExpiredBeverages();
        return ResponseEntity.ok(beverages);
    }
    
    /**
     * 取得所有即將過期的飲料（7 天內）
     */
    @GetMapping("/expiring-soon")
    public ResponseEntity<List<BeverageDTO>> getExpiringSoonBeverages() {
        List<BeverageDTO> beverages = beverageService.getExpiringSoonBeverages();
        return ResponseEntity.ok(beverages);
    }
    
    /**
     * 取得庫存統計
     */
    @GetMapping("/statistics")
    public ResponseEntity<BeverageService.BeverageStatisticsDTO> getStatistics() {
        BeverageService.BeverageStatisticsDTO statistics = beverageService.getStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * 自動隔離過期商品（業界標準流程）
     * 
     * 通常由定時任務（Scheduler）每日執行
     * 將所有過期且狀態為 NORMAL 的商品自動改為 QUARANTINED
     */
    @PostMapping("/quarantine-expired")
    public ResponseEntity<Map<String, Object>> quarantineExpiredBeverages() {
        int quarantinedCount = beverageService.quarantineExpiredBeverages();
        return ResponseEntity.ok(Map.of(
                "message", "成功隔離 " + quarantinedCount + " 個過期商品",
                "quarantinedCount", quarantinedCount
        ));
    }
    
    /**
     * 取得隔離區中的商品（QUARANTINED 狀態）
     */
    @GetMapping("/quarantined")
    public ResponseEntity<List<BeverageDTO>> getQuarantinedBeverages() {
        List<BeverageDTO> beverages = beverageService.getQuarantinedBeverages();
        return ResponseEntity.ok(beverages);
    }
    
    /**
     * 報廢商品（業界標準流程）
     * 
     * 將隔離區中的商品標記為已報廢
     * 通常需要審批流程，這裡簡化為直接報廢
     */
    @PostMapping("/{id}/dispose")
    public ResponseEntity<BeverageDTO> disposeBeverage(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String reason = request.getOrDefault("reason", "過期報廢");
        BeverageDTO beverage = beverageService.disposeBeverage(id, reason);
        return ResponseEntity.ok(beverage);
    }
    
    /**
     * 取得已報廢的商品列表（DISPOSED 狀態）
     */
    @GetMapping("/disposed")
    public ResponseEntity<List<BeverageDTO>> getDisposedBeverages() {
        List<BeverageDTO> beverages = beverageService.getDisposedBeverages();
        return ResponseEntity.ok(beverages);
    }
}

