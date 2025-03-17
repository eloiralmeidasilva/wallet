package com.eloir.wallet.entity;

import com.eloir.wallet.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private BigDecimal finalBalance;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_wallet_id")
    private Wallet senderWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_wallet_id")
    private Wallet receiverWallet;

    @Column
    private String senderUserId;

    @Column
    private String receiverUserId;

    @Column
    private BigDecimal finalBalanceSender;

    @Column
    private BigDecimal finalBalanceReceiver;

    public Transaction(Wallet wallet, TransactionType type, BigDecimal amount, BigDecimal finalBalance) {
        this.wallet = wallet;
        this.type = type;
        this.amount = amount;
        this.finalBalance = finalBalance;
        this.createdAt = LocalDateTime.now();
    }

    public Transaction(Wallet senderWallet, Wallet receiverWallet, String senderUserId, String receiverUserId, BigDecimal amount, BigDecimal finalBalanceSender, BigDecimal finalBalanceReceiver) {
        this.type = TransactionType.TRANSFER;
        this.senderWallet = senderWallet;
        this.receiverWallet = receiverWallet;
        this.senderUserId = senderUserId;
        this.receiverUserId = receiverUserId;
        this.amount = amount;
        this.finalBalanceSender = finalBalanceSender;
        this.finalBalanceReceiver = finalBalanceReceiver;
        this.createdAt = LocalDateTime.now();
    }
}