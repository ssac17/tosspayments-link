package com.toss.tosspaymentslink.dto;

import com.toss.tosspaymentslink.domain.embeded.*;
import com.toss.tosspaymentslink.domain.entity.Cancels;
import com.toss.tosspaymentslink.domain.entity.Payment;

import java.time.OffsetDateTime;
import java.util.List;

public record PaymentResponseDto(
        String mId,
        String lastTransactionKey,
        String paymentKey,
        String orderId,
        String orderName,
        Integer taxExemptionAmount,
        String status,
        String requestedAt,
        String approvedAt,
        Boolean useEscrow,
        CardDto card,
        VirtualAccountDto virtualAccount,
        TransferDto transfer,
        MobilePhoneDto mobilePhone,
        String discount,
        List<CancelDto> cancels,
        String secret,
        String type,
        EasyPayDto easyPay,
        String country,
        FailureDto failure,
        Boolean isPartialCancelable,
        String receiptUrl,
        String checkoutUrl,
        String currency,
        Integer totalAmount,
        Integer balanceAmount,
        Integer suppliedAmount,
        Integer vat,
        Integer taxFreeAmount,
        String metadata,
        String method,
        String version
) {


    public static PaymentResponseDto from(Payment savedPayment) {
        if (savedPayment == null) return null;
        return new PaymentResponseDto(
                savedPayment.getMId(),
                savedPayment.getLastTransactionKey(),
                savedPayment.getPaymentKey(),
                savedPayment.getOrderId(),
                savedPayment.getOrderName(),
                savedPayment.getTaxExemptionAmount(),
                savedPayment.getStatus() != null ? savedPayment.getStatus().name() : null,
                savedPayment.getRequestedAt() != null ? savedPayment.getRequestedAt().toString() : null,
                savedPayment.getApprovedAt() != null ? savedPayment.getApprovedAt().toString() : null,
                savedPayment.getUseEscrow(),
                CardDto.from(savedPayment.getCard()),
                VirtualAccountDto.from(savedPayment.getVirtualAccount()),
                TransferDto.from(savedPayment.getTransfer()),
                MobilePhoneDto.from(savedPayment.getMobilePhone()),
                null,
                // cancels 리스트 변환 처리
                savedPayment.getCancels() != null
                        ? savedPayment.getCancels().stream().map(CancelDto::from).toList()
                        : null,
                savedPayment.getSecret(),
                savedPayment.getType() != null ? savedPayment.getType().name() : null,
                EasyPayDto.from(savedPayment.getEasyPay()),
                savedPayment.getCountry(),
                FailureDto.from(savedPayment.getFailure()),
                savedPayment.getIsPartialCancelable(),
                savedPayment.getReceiptUrl(),
                savedPayment.getCheckoutUrl(),
                savedPayment.getCurrency(),
                savedPayment.getTotalAmount(),
                savedPayment.getBalanceAmount(),
                savedPayment.getSuppliedAmount(),
                savedPayment.getVat(),
                savedPayment.getTaxFreeAmount(),
                savedPayment.getMetadata(),
                savedPayment.getMethod() != null ? savedPayment.getMethod().getDescription() : null, // "카드", "계좌이체" 등 한글명
                savedPayment.getVersion()
        );
    }

    record CardDto(
            String issuerCode,
            String acquirerCode,
            String number,
            Integer installmentPlanMonths,
            Boolean isInterestFree,
            String interestPayer,
            String approveNo,
            Boolean useCardPoint,
            String cardType,
            String ownerType,
            String acquireStatus,
            Integer amount
    ) {
        public static CardDto from(Card cardEntity) {
            if (cardEntity == null) return null;
            return new CardDto(
                    cardEntity.getIssuerCode(),
                    cardEntity.getAcquirerCode(),
                    cardEntity.getNumber(),
                    cardEntity.getInstallmentPlanMonths(),
                    cardEntity.getIsInterestFree(),
                    cardEntity.getInterestPayer(),
                    cardEntity.getApproveNo(),
                    cardEntity.getUseCardPoint(),
                    cardEntity.getCardType(),
                    cardEntity.getOwnerType(),
                    cardEntity.getAcquireStatus().name(),
                    cardEntity.getAmount()
            );
        }
    }

    record VirtualAccountDto(
            String accountType,
            String accountNumber,
            String backCode,
            String customerName,
            String depositorName,
            String  dueDate,
            String refundStatus,
            Boolean expired,
            String settlementStatus,
            String settlementDate
    ) {
        public static VirtualAccountDto from(VirtualAccount virtualAccountEntity) {
            if (virtualAccountEntity == null) return null;
            return new VirtualAccountDto(
                    virtualAccountEntity.getAccountType(),
                    virtualAccountEntity.getAccountNumber(),
                    virtualAccountEntity.getBackCode(),
                    virtualAccountEntity.getCustomerName(),
                    virtualAccountEntity.getDepositorName(),
                    virtualAccountEntity.getDueDate() != null ? virtualAccountEntity.getDueDate().toString() : null,
                    virtualAccountEntity.getRefundStatus(),
                    virtualAccountEntity.getExpired(),
                    virtualAccountEntity.getSettlementStatus(),
                    virtualAccountEntity.getSettlementDate() != null ? virtualAccountEntity.getSettlementDate().toString() : null
            );
        }
    }

    record TransferDto(
            String bankCode,
            String settlementStatus
    ) {
        public static TransferDto from(Transfer transferEntity) {
            if (transferEntity == null) return null;
            return new TransferDto(
                    transferEntity.getBankCode(),
                    transferEntity.getSettlementStatus()
            );
        }
    }

    record MobilePhoneDto(
            String customerMobilePhone,
            String settlementStatus,
            String receiptUrl
    ) {
        public static MobilePhoneDto from(MobilePhone mobilePhoneEntity) {
            if (mobilePhoneEntity == null) return null;
            return new MobilePhoneDto(
                    mobilePhoneEntity.getCustomerMobilePhone(),
                    mobilePhoneEntity.getSettlementStatus(),
                    mobilePhoneEntity.getReceiptUrl()
            );
        }
    }

    record CancelDto(
            Integer cancelAmount,
            String cancelReason,
            Integer taxFreeAmount,
            Integer taxExemptionAmount,
            Integer refundableAmount,
            Integer cardDiscountAmount,
            Integer transferDiscountAmount,
            Integer easyPayDiscountAmount,
            OffsetDateTime canceledAt,
            String transactionKey,
            String receiptKey,
            String cancelStatus,
            String cancelRequestId
    ) {
        public static CancelDto from(Cancels cancelsEntity) {
            if (cancelsEntity == null) return null;
            return new CancelDto(
                    cancelsEntity.getCancelAmount(),
                    cancelsEntity.getCancelReason(),
                    cancelsEntity.getTaxFreeAmount(),
                    cancelsEntity.getTaxExemptionAmount(),
                    cancelsEntity.getRefundableAmount(),
                    cancelsEntity.getCardDiscountAmount(),
                    cancelsEntity.getTransferDiscountAmount(),
                    cancelsEntity.getEasyPayDiscountAmount(),
                    cancelsEntity.getCanceledAt(),
                    cancelsEntity.getTransactionKey(),
                    cancelsEntity.getReceiptKey(),
                    cancelsEntity.getCancelStatus(),
                    cancelsEntity.getCancelRequestId()
            );
        }
    }

    record FailureDto(
            String code,
            String message
    ) {
        public static FailureDto from(Failure failureEntity) {
            if (failureEntity == null) return null;
            return new FailureDto(
                    failureEntity.getCode(),
                    failureEntity.getMessage()
            );
        }
    }

    record EasyPayDto(
            String provider,
            Integer amount,
            Integer discountAmount
    ) {
        public static EasyPayDto from(EasyPay easyPayEntity) {
            if (easyPayEntity == null) return null;
            return new EasyPayDto(
                    easyPayEntity.getProvider(),
                    easyPayEntity.getAmount(),
                    easyPayEntity.getDiscountAmount()
            );
        }
    }
}
