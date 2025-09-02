package com.loopers.interfaces.api.payment;

import com.loopers.application.payment.dto.InitiateCommand;
import com.loopers.domain.payment.PayResult;
import com.loopers.domain.payment.PaymentMethod;
import lombok.Builder;

public class PaymentV1Dto {

    @Builder
    public record SyncPaymentCallbackResponse(String message) {
    }

    @Builder
    public record InitiateResponse(String status, String failureReason) {

        public static InitiateResponse from(PayResult result) {
            return InitiateResponse.builder()
                    .status(result.getStatus().name())
                    .failureReason(result.getReason().name())
                    .build();
        }
    }

    public record InitiateRequest(
            Long orderId,
            PaymentMethod method,
            String callbackUrl,
            String cardType,
            String cardNo
    ) {

        public InitiateCommand toCommand() {
            return InitiateCommand.builder()
                    .orderId(orderId)
                    .method(method)
                    .cardType(cardType)
                    .cardNo(cardNo)
                    .build();
        }
    }

    public record SyncPaymentCallbackRequest(
            String transactionKey,
            Long orderId,
            String cardType,
            String cardNo,
            Long amount,
            String status,
            String reason
    ) {

    }
}
