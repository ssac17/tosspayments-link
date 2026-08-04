package com.toss.tosspaymentslink.domain.entity;

import com.toss.tosspaymentslink.domain.embeded.*;
import com.toss.tosspaymentslink.domain.enums.PayMethod;
import com.toss.tosspaymentslink.domain.enums.PaymentStatus;
import com.toss.tosspaymentslink.domain.enums.Type;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter @Getter @Builder @ToString
@NoArgsConstructor @AllArgsConstructor
public class Payment {
    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String version; //객체의 응답 버전
    private String paymentKey; //결제 고유키

    @Enumerated(EnumType.STRING)
    private Type type; //결제 타입 정보
    private String orderId; //주문 번호
    private String orderName; // 구매 상품
    private String mId; //가맹점 식별코드
    private String currency; //결제한 통화

    @Enumerated(EnumType.STRING)
    private PayMethod method; //결제수단

    private Integer totalAmount; //총 결제금액
    private Integer balanceAmount; //부분 취소 후 남은 금액

    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // 결제 상태

    private OffsetDateTime requestedAt; // 결제 요청 시간
    private OffsetDateTime approvedAt; // 결제 승인 시간
    private Boolean useEscrow; //에스크로 사용 여부
    private String lastTransactionKey; //마지막 거래의 키값입니다, 결제 승인 후 부분 취소를 두 번 했다면 마지막 부분 취소 거래의 키값이 할당됩니다
    private Integer suppliedAmount; //공급가액
    private Integer vat; //부가세
    private Boolean cultureExpense; //문화비 결제 여부
    private Integer taxFreeAmount; //결제 금액 중 면세 금액
    private Integer taxExemptionAmount; //과세를 제외한 결제 금액

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL,  orphanRemoval = true)
    @Builder.Default
    private List<Cancels> cancels = new ArrayList<>(); //취소 정보

    private Boolean isPartialCancelable; //부분 취소 가능 여부

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "card_amount")),
            @AttributeOverride(name = "approveNo", column = @Column(name = "card_approve_no"))
    })
    private Card card; //결제 수단이 카드인 경우, 카드 결제 상세 정보

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "accountNumber", column = @Column(name = "virtual_account_number"))
    })
    private VirtualAccount virtualAccount; //결제 수단이 가상계좌인 경우, 가상계좌 결제 상세 정보

    private String secret; //웹훅을 검증하기 위한 시크릿 키값

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "receiptUrl", column = @Column(name = "mobile_phone_receipt_url")),
            @AttributeOverride(name = "settlementStatus", column = @Column(name = "mobile_phone_settlement_status"))
    })
    private MobilePhone mobilePhone; //모바일폰 결제 상세 정보

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "settlementStatus", column = @Column(name = "gift_certificate_settlement_status"))
    })
    private GiftCertificate giftCertificate; //상품권 결제 상세 정보

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "settlementStatus", column = @Column(name = "bank_transfer_settlement_status")),
            @AttributeOverride(name = "bankCode", column = @Column(name = "bank_transfer_bank_code")),
    })
    private Transfer transfer; //계좌이체 결제 상세 정보

    @Column(columnDefinition = "TEXT")
    private String metadata; //결제 시 전달한 메타데이터

    private String receiptUrl; //결제 영수증 URL
    private String checkoutUrl; //결제창 젇보 URL

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "easy_pay_amount")),
            @AttributeOverride(name = "discountAmount", column = @Column(name = "easy_pay_discount_amount"))
    })
    private EasyPay easyPay;

    private String country; //결제 요청을 보낸 국가 코드

    @Embedded
    private Failure failure; //결제 승인에 실패하면 응답

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "cash_receipt_amount")),
            @AttributeOverride(name = "receiptUrl", column = @Column(name = "cash_receipt_receipt_url")),
            @AttributeOverride(name = "taxFreeAmount", column = @Column(name = "cash_receipt_tax_free_amount")),
            @AttributeOverride(name ="type", column = @Column(name = "cash_receipt_type"))
    })
    private CashReceipt cashReceipt; //현금 영수증 발급 정보

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CashReceipts> cashReceipts = new ArrayList<>(); //현금영수증 발행 및 취소 이력

    private Integer discountAmount; //결제 금액 중 할인 금액
}
