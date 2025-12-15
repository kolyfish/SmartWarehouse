package com.beveragewarehouse.repository;

import com.beveragewarehouse.model.Beverage;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 飲料資料庫操作介面
 * 
 * 使用悲觀鎖（Pessimistic Locking）確保高併發下的資料一致性
 */
@Repository
public interface BeverageRepository extends JpaRepository<Beverage, Long> {
    
    /**
     * 根據名稱查詢飲料
     */
    List<Beverage> findByName(String name);
    
    /**
     * 根據 ID 查詢並加鎖（悲觀鎖）
     * 用於高併發場景下的資料一致性保證
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Beverage b WHERE b.id = :id")
    Optional<Beverage> findByIdWithLock(@Param("id") Long id);
    
    /**
     * 查詢所有未過期的飲料
     */
    @Query("SELECT b FROM Beverage b WHERE b.expiryDate >= :today")
    List<Beverage> findNonExpiredBeverages(LocalDate today);
    
    /**
     * 查詢所有已過期的飲料（包含所有狀態）
     */
    @Query("SELECT b FROM Beverage b WHERE b.expiryDate < :today")
    List<Beverage> findExpiredBeverages(LocalDate today);
    
    /**
     * 查詢隔離區中的商品（QUARANTINED 狀態）
     */
    @Query("SELECT b FROM Beverage b WHERE b.status = 'QUARANTINED'")
    List<Beverage> findQuarantinedBeverages();
    
    /**
     * 查詢已報廢的商品（DISPOSED 狀態）
     */
    @Query("SELECT b FROM Beverage b WHERE b.status = 'DISPOSED'")
    List<Beverage> findDisposedBeverages();
    
    /**
     * 查詢即將過期的飲料（7 天內）
     */
    @Query("SELECT b FROM Beverage b WHERE b.expiryDate BETWEEN :today AND :sevenDaysLater")
    List<Beverage> findExpiringSoonBeverages(LocalDate today, LocalDate sevenDaysLater);
    
    /**
     * 根據名稱和有效期限查詢（用於出庫時選擇最早過期的）
     * 使用悲觀鎖確保高併發下的資料一致性
     * 只查詢 NORMAL 狀態的商品（過期商品已隔離，不能出庫）
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Beverage b WHERE b.name = :name AND b.quantity > 0 AND b.expiryDate >= :today AND b.status = 'NORMAL' ORDER BY b.expiryDate ASC")
    List<Beverage> findAvailableBeveragesByNameOrderByExpiryWithLock(
            @Param("name") String name, 
            @Param("today") LocalDate today
    );
    
    /**
     * 根據名稱和有效期限查詢（不加鎖版本，用於查詢）
     */
    @Query("SELECT b FROM Beverage b WHERE b.name = :name AND b.quantity > 0 AND b.expiryDate >= :today ORDER BY b.expiryDate ASC")
    List<Beverage> findAvailableBeveragesByNameOrderByExpiry(
            @Param("name") String name, 
            @Param("today") LocalDate today
    );
}

