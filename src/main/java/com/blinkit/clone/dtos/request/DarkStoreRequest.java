package com.blinkit.clone.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DarkStoreRequest {

    @NotBlank(message = "Store name is required")
    private String storeName;

    @NotNull(message = "Latitude is required")
    @Range(min = -90, max = 90, message = "Latitude must be between -90 and 90")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @Range(min = -180, max = 180, message = "Longitude must be between -180 and 180")
    private Double longitude;
}