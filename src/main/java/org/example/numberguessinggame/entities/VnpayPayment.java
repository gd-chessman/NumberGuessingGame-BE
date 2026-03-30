package org.example.numberguessinggame.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vnpay_payments")
@Getter
@Setter
@NoArgsConstructor
public class VnpayPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Must match vnp_TxnRef sent to VNPay (unique per merchant). */
    @Column(name = "txn_ref", nullable = false, unique = true, length = 100)
    private String txnRef;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Amount in VND (not multiplied by 100). */
    @Column(name = "amount_vnd", nullable = false)
    private long amountVnd;

    @Column(name = "turns_to_grant", nullable = false)
    private int turnsToGrant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VnpayPaymentStatus status = VnpayPaymentStatus.PENDING;

    @Column(name = "response_code", length = 10)
    private String responseCode;

    @Column(name = "transaction_no", length = 32)
    private String transactionNo;

    @Column(name = "bank_code", length = 20)
    private String bankCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
