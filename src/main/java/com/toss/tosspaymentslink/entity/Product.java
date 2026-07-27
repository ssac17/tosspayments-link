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
}
