package com.blinkit.clone.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Cart implements Serializable {
    private Long userId;
    private Long storeId;
    private Map<Long, Integer> items = new HashMap<>();   // productId -> quantity

}