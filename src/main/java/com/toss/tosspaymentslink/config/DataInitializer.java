package com.toss.tosspaymentslink.config;

import com.toss.tosspaymentslink.domain.entity.Product;
import com.toss.tosspaymentslink.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;

    public DataInitializer(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if(productRepository.count() == 0) {
            log.info("초기 상품 데이터를 DB에 주입합니다...");
            List<Product> addProducts = readProductFile();
            productRepository.saveAll(addProducts);
            log.info("초기 데이터 입력 완료!");
        }
    }

    private List<Product> readProductFile() {
        List<Product> products = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource("products.csv");
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines()
                    .filter(line -> !line.startsWith("#") && !line.isBlank())
                    .map(this::parseToProduct)
                    .toList();
        } catch (IOException e) {
            log.error("products.csv 파일 읽기 실패: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    private Product parseToProduct(String line) {
        Product product = new Product();
        String[] split = line.split(",");
        product.setName(split[0].trim());
        product.setPrice(Integer.parseInt(split[1].trim()));
        product.setStockQuantity(Integer.parseInt(split[2].trim()));
        product.setImageUrl(split[3].trim());
        return product;
    }
}
