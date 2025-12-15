package com.beveragewarehouse.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 飲料實體類別
 * 
 * 儲存飲料庫存資訊，包含：
 * - 基本資訊（名稱、數量）
 * - 有效期管理
 * - 入庫/出庫時間
 */
@Entity
@Table(name = "beverages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Beverage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 飲料名稱（目前只支援礦泉水）
     */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * 庫存數量
     */
    @Column(nullable = false)
    private Integer quantity;
    
    /**
     * 生產日期
     */
    @Column(nullable = false)
    private LocalDate productionDate;
    
    /**
     * 有效期限
     */
    @Column(nullable = false)
    private LocalDate expiryDate;
    
    /**
     * 入庫時間
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 最後更新時間
     */
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 商品狀態
     * NORMAL: 正常商品，可以正常出庫
     * QUARANTINED: 隔離中，已過期但尚未處理
     * DISPOSED: 已報廢，已從庫存中移除
     */
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private BeverageStatus status = BeverageStatus.NORMAL;
    
    /**
     * 報廢原因（僅在 DISPOSED 狀態時使用）
     */
    @Column(length = 500)
    private String disposalReason;
    
    /**
     * 報廢時間（僅在 DISPOSED 狀態時使用）
     */
    private LocalDateTime disposedAt;
    
    /**
     * 是否已過期
     */
    @Transient
    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }
    
    /**
     * 距離過期還剩幾天
     */
    @Transient
    public long getDaysUntilExpiry() {
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }
    
    /**
     * 是否即將過期（7 天內）
     */
    @Transient
    public boolean isExpiringSoon() {
        long daysUntilExpiry = getDaysUntilExpiry();
        return daysUntilExpiry >= 0 && daysUntilExpiry <= 7;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

