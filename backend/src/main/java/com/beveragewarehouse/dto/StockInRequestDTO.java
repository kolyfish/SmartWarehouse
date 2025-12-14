package com.beveragewarehouse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 入庫請求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockInRequestDTO {
    
    @NotBlank(message = "飲料名稱不能為空")
    private String name;
    
    @NotNull(message = "數量不能為空")
    @Min(value = 1, message = "數量必須大於 0")
    private Integer quantity;
    
    @NotNull(message = "生產日期不能為空")
    private LocalDate productionDate;
    
    @NotNull(message = "有效期限不能為空")
    private LocalDate expiryDate;
}

