package com.toss.tosspaymentslink.repository;

import com.toss.tosspaymentslink.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
