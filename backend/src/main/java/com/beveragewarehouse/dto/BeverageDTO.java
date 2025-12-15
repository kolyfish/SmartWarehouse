package com.beveragewarehouse.dto;

import com.beveragewarehouse.model.BeverageStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 飲料資料傳輸物件（DTO）
 * 
 * 用於 API 請求與回應
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BeverageDTO {
    
    private Long id;
    private String name;
    private Integer quantity;
    private LocalDate productionDate;
    private LocalDate expiryDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * 是否已過期（計算欄位）
     */
    private Boolean expired;
    
    /**
     * 距離過期還剩幾天（計算欄位）
     */
    private Long daysUntilExpiry;
    
    /**
     * 是否即將過期（計算欄位）
     */
    private Boolean expiringSoon;
    
    /**
     * 商品狀態（業界標準）
     * NORMAL: 正常商品
     * QUARANTINED: 隔離中（已過期）
     * DISPOSED: 已報廢
     */
    private BeverageStatus status;
    
    /**
     * 報廢原因（僅在 DISPOSED 狀態時使用）
     */
    private String disposalReason;
    
    /**
     * 報廢時間（僅在 DISPOSED 狀態時使用）
     */
    private LocalDateTime disposedAt;
}

