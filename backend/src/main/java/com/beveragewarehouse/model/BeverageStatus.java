package com.beveragewarehouse.model;

/**
 * 商品狀態枚舉
 * 
 * 業界標準的過期商品處理狀態：
 * - NORMAL: 正常商品，可以正常出庫
 * - QUARANTINED: 隔離中，已過期但尚未處理
 * - DISPOSED: 已報廢，已從庫存中移除
 */
public enum BeverageStatus {
    NORMAL,      // 正常商品
    QUARANTINED, // 隔離中（已過期）
    DISPOSED     // 已報廢
}

