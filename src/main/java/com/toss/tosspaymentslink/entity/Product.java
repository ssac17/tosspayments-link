package com.toss.tosspaymentslink.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name; //상품명
    private int price; //가격
    private int stockQuantity; //재고 수량
    private String imageUrl; //이미지 경로

    public void decreaseStock(int quantity) {
        if(stockQuantity < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다. (현재 재고: " + this.stockQuantity + "개)");
        }
        this.stockQuantity -= quantity;
    }
}
