package com.blinkit.clone.dtos.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String name;

    @Size(max = 1000, message = "Description must be under 1000 characters")
    private String description;

    @NotBlank(message = "Image URL is required")
    private String image;

    @NotNull(message = "MRP is required")
    @Positive(message = "MRP must be greater than 0")
    private Double mrp;

    @NotNull(message = "Selling price is required")
    @Positive(message = "Selling price must be greater than 0")
    private Double sellingPrice;

    @NotBlank(message = "Unit is required (e.g. '500 ml', '1 kg')")
    private String unit;


    @AssertTrue(message = "Selling price cannot be greater than MRP")
    public boolean isSellingPriceValid() {
        if (sellingPrice == null || mrp == null) return true;   // let @NotNull handle nulls separately
        return sellingPrice <= mrp;
    }


}
