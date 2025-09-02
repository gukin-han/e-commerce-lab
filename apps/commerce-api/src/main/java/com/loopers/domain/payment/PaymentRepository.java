package com.loopers.domain.payment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findByOrderId(Long orderId);

    Page<Payment> findPendingSince(ZonedDateTime cutoff, Pageable pageable);

    boolean existsByOrderId(Long orderId);
}
