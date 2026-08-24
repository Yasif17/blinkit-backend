package com.blinkit.clone.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String name;
    private String slug;
    private String description;
    private String image;

    private Double mrp;
    private Double sellingPrice;   // grocery apps almost always show a discount — MRP alone isn't enough
    private String unit;           // "500 ml", "1 kg", "6 pcs" — essential for grocery, easy to forget
    private Boolean active = true;
}
