package com.beveragewarehouse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 智慧倉庫系統 - 飲料庫存管理
 * 
 * 功能：
 * - 飲料入庫（儲備）
 * - 飲料出庫（輸出）
 * - 過期檢查
 * - CRUD 操作
 */
@SpringBootApplication
@EnableScheduling
public class BeverageWarehouseApplication {
    public static void main(String[] args) {
        SpringApplication.run(BeverageWarehouseApplication.class, args);
    }
}

