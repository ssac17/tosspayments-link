package com.toss.tosspaymentslink.repository;

import com.toss.tosspaymentslink.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayRepository extends JpaRepository<Payment, Long> {
}
