package com.toss.tosspaymentslink.service;

import com.toss.tosspaymentslink.domain.entity.Product;
import com.toss.tosspaymentslink.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }
}
