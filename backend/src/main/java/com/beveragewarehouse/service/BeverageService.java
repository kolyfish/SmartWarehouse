package com.beveragewarehouse.service;

import com.beveragewarehouse.dto.BeverageDTO;
import com.beveragewarehouse.dto.BeverageRequestDTO;
import com.beveragewarehouse.dto.StockInRequestDTO;
import com.beveragewarehouse.dto.StockOutRequestDTO;
import com.beveragewarehouse.model.Beverage;
import com.beveragewarehouse.model.BeverageStatus;
import com.beveragewarehouse.repository.BeverageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 飲料業務邏輯服務
 */
@Service
@RequiredArgsConstructor
public class BeverageService {
    
    private final BeverageRepository beverageRepository;
    
    /**
     * 取得所有飲料
     */
    public List<BeverageDTO> getAllBeverages() {
        return beverageRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 根據 ID 取得飲料
     */
    public BeverageDTO getBeverageById(Long id) {
        Beverage beverage = beverageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("飲料不存在，ID: " + id));
        return convertToDTO(beverage);
    }
    
    /**
     * 新增飲料（入庫）
     * 
     * 業務規則：一次入庫數量不能超過 100 瓶
     */
    @Transactional
    public BeverageDTO stockIn(StockInRequestDTO request) {
        // 業務邏輯驗證：一次入庫數量不能超過 100 瓶
        if (request.getQuantity() > 100) {
            throw new RuntimeException("一次入庫數量不能超過 100 瓶，當前數量: " + request.getQuantity());
        }
        
        Beverage beverage = new Beverage();
        beverage.setName(request.getName());
        beverage.setQuantity(request.getQuantity());
        beverage.setProductionDate(request.getProductionDate());
        beverage.setExpiryDate(request.getExpiryDate());
        
        Beverage saved = beverageRepository.save(beverage);
        return convertToDTO(saved);
    }
    
    /**
     * 出庫飲料（按照 FIFO 原則，優先出庫最早過期的）
     * 
     * 使用悲觀鎖（Pessimistic Lock）確保高併發下的資料一致性
     * 在高併發場景下，多個執行緒同時出庫時，會依序取得鎖並執行，避免負庫存
     */
    @Transactional
    public BeverageDTO stockOut(StockOutRequestDTO request) {
        LocalDate today = LocalDate.now();
        
        // 使用悲觀鎖查詢可用的飲料，按照過期日期排序（最早過期的優先）
        // 這會對查詢到的記錄加鎖，直到事務提交
        List<Beverage> availableBeverages = beverageRepository
                .findAvailableBeveragesByNameOrderByExpiryWithLock(request.getName(), today);
        
        if (availableBeverages.isEmpty()) {
            throw new RuntimeException("沒有可用的 " + request.getName() + " 庫存");
        }
        
        int remainingQuantity = request.getQuantity();
        Beverage firstBeverage = null;
        
        for (Beverage beverage : availableBeverages) {
            if (remainingQuantity <= 0) {
                break;
            }
            
            // 重新載入並加鎖，確保取得最新的庫存數量
            Beverage lockedBeverage = beverageRepository.findByIdWithLock(beverage.getId())
                    .orElseThrow(() -> new RuntimeException("飲料不存在，ID: " + beverage.getId()));
            
            int availableQuantity = lockedBeverage.getQuantity();
            
            if (availableQuantity <= 0) {
                // 庫存已被其他執行緒消耗完，跳過
                continue;
            }
            
            if (firstBeverage == null) {
                firstBeverage = lockedBeverage;
            }
            
            if (availableQuantity <= remainingQuantity) {
                // 這批飲料全部出庫
                remainingQuantity -= availableQuantity;
                beverageRepository.delete(lockedBeverage);
            } else {
                // 部分出庫
                lockedBeverage.setQuantity(availableQuantity - remainingQuantity);
                beverageRepository.save(lockedBeverage);
                remainingQuantity = 0;
            }
        }
        
        if (remainingQuantity > 0) {
            throw new RuntimeException("庫存不足，無法出庫 " + request.getQuantity() + " 瓶 " + request.getName());
        }
        
        return firstBeverage != null ? convertToDTO(firstBeverage) : convertToDTO(availableBeverages.get(0));
    }
    
    /**
     * 更新飲料資訊
     */
    @Transactional
    public BeverageDTO updateBeverage(Long id, BeverageRequestDTO request) {
        Beverage beverage = beverageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("飲料不存在，ID: " + id));
        
        beverage.setName(request.getName());
        beverage.setQuantity(request.getQuantity());
        beverage.setProductionDate(request.getProductionDate());
        beverage.setExpiryDate(request.getExpiryDate());
        
        Beverage updated = beverageRepository.save(beverage);
        return convertToDTO(updated);
    }
    
    /**
     * 刪除飲料
     */
    @Transactional
    public void deleteBeverage(Long id) {
        if (!beverageRepository.existsById(id)) {
            throw new RuntimeException("飲料不存在，ID: " + id);
        }
        beverageRepository.deleteById(id);
    }
    
    /**
     * 取得所有已過期的飲料
     */
    public List<BeverageDTO> getExpiredBeverages() {
        LocalDate today = LocalDate.now();
        return beverageRepository.findExpiredBeverages(today).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 取得所有即將過期的飲料（7 天內）
     */
    public List<BeverageDTO> getExpiringSoonBeverages() {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysLater = today.plusDays(7);
        return beverageRepository.findExpiringSoonBeverages(today, sevenDaysLater).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 取得庫存統計
     */
    public BeverageStatisticsDTO getStatistics() {
        List<Beverage> allBeverages = beverageRepository.findAll();
        LocalDate today = LocalDate.now();
        
        long totalQuantity = allBeverages.stream()
                .mapToLong(Beverage::getQuantity)
                .sum();
        
        long expiredQuantity = beverageRepository.findExpiredBeverages(today).stream()
                .mapToLong(Beverage::getQuantity)
                .sum();
        
        long expiringSoonQuantity = beverageRepository.findExpiringSoonBeverages(today, today.plusDays(7)).stream()
                .mapToLong(Beverage::getQuantity)
                .sum();
        
        return new BeverageStatisticsDTO(
                allBeverages.size(),
                totalQuantity,
                expiredQuantity,
                expiringSoonQuantity
        );
    }
    
    /**
     * 自動隔離過期商品（業界標準流程）
     * 
     * 通常由定時任務（Scheduler）每日執行
     * 將所有過期且狀態為 NORMAL 的商品自動改為 QUARANTINED
     */
    @Transactional
    public int quarantineExpiredBeverages() {
        LocalDate today = LocalDate.now();
        List<Beverage> expiredBeverages = beverageRepository.findExpiredBeverages(today);
        
        int quarantinedCount = 0;
        for (Beverage beverage : expiredBeverages) {
            if (beverage.getStatus() == BeverageStatus.NORMAL) {
                beverage.setStatus(BeverageStatus.QUARANTINED);
                beverageRepository.save(beverage);
                quarantinedCount++;
            }
        }
        
        return quarantinedCount;
    }
    
    /**
     * 取得隔離區中的商品（QUARANTINED 狀態）
     */
    public List<BeverageDTO> getQuarantinedBeverages() {
        return beverageRepository.findQuarantinedBeverages().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 報廢商品（業界標準流程）
     * 
     * 將隔離區中的商品標記為已報廢
     * 通常需要審批流程，這裡簡化為直接報廢
     */
    @Transactional
    public BeverageDTO disposeBeverage(Long id, String reason) {
        Beverage beverage = beverageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在，ID: " + id));
        
        if (beverage.getStatus() != BeverageStatus.QUARANTINED) {
            throw new RuntimeException("只能報廢隔離區中的商品，當前狀態: " + beverage.getStatus());
        }
        
        beverage.setStatus(BeverageStatus.DISPOSED);
        beverage.setDisposalReason(reason);
        beverage.setDisposedAt(LocalDateTime.now());
        
        Beverage disposed = beverageRepository.save(beverage);
        return convertToDTO(disposed);
    }
    
    /**
     * 取得已報廢的商品列表（DISPOSED 狀態）
     */
    public List<BeverageDTO> getDisposedBeverages() {
        return beverageRepository.findDisposedBeverages().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 轉換 Entity 為 DTO
     */
    private BeverageDTO convertToDTO(Beverage beverage) {
        BeverageDTO dto = new BeverageDTO();
        dto.setId(beverage.getId());
        dto.setName(beverage.getName());
        dto.setQuantity(beverage.getQuantity());
        dto.setProductionDate(beverage.getProductionDate());
        dto.setExpiryDate(beverage.getExpiryDate());
        dto.setCreatedAt(beverage.getCreatedAt());
        dto.setUpdatedAt(beverage.getUpdatedAt());
        dto.setExpired(beverage.isExpired());
        dto.setDaysUntilExpiry(beverage.getDaysUntilExpiry());
        dto.setExpiringSoon(beverage.isExpiringSoon());
        dto.setStatus(beverage.getStatus());
        dto.setDisposalReason(beverage.getDisposalReason());
        dto.setDisposedAt(beverage.getDisposedAt());
        return dto;
    }
    
    /**
     * 庫存統計 DTO（內部類別）
     */
    public static class BeverageStatisticsDTO {
        private final long totalItems;
        private final long totalQuantity;
        private final long expiredQuantity;
        private final long expiringSoonQuantity;
        
        public BeverageStatisticsDTO(long totalItems, long totalQuantity, long expiredQuantity, long expiringSoonQuantity) {
            this.totalItems = totalItems;
            this.totalQuantity = totalQuantity;
            this.expiredQuantity = expiredQuantity;
            this.expiringSoonQuantity = expiringSoonQuantity;
        }
        
        public long getTotalItems() { return totalItems; }
        public long getTotalQuantity() { return totalQuantity; }
        public long getExpiredQuantity() { return expiredQuantity; }
        public long getExpiringSoonQuantity() { return expiringSoonQuantity; }
    }
}

