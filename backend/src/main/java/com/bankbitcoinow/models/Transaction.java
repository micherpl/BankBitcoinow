package com.bankbitcoinow.models;

import org.bitcoinj.core.Coin;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.sql.Timestamp;


@Entity
@Table(name = "transaction")
public class Transaction {
    private Long id;
    private String hash;
    private String sourceAddress;
    private String destinationAddress;
    private BigDecimal amount;
    private Timestamp createdAt;
    private TransactionStatus status;
    private int confirmations;
    private byte[] blockchainData;
    private Address address;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_seq_gen")
    @SequenceGenerator(name = "transaction_seq_gen", sequenceName = "transaction_id_seq")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getSourceAddress() {
        return sourceAddress;
    }

    public void setSourceAddress(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Transient
    public void setAmount(Coin coin) {
        setAmount(new BigDecimal(coin.getValue()).movePointLeft(Coin.SMALLEST_UNIT_EXPONENT));
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Convert(converter = TransactionStatusConverter.class)
    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public int getConfirmations() {
        return confirmations;
    }

    public void setConfirmations(int confirmations) {
        this.confirmations = confirmations;
    }

    public byte[] getBlockchainData() {
        return blockchainData;
    }

    public void setBlockchainData(byte[] blockchainData) {
        this.blockchainData = blockchainData;
    }

    @ManyToOne
    @JoinColumn(name = "address_id")
    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @Transient
    public boolean isIncoming() {
        return address != null && address.getAddress().equals(destinationAddress);
    }

    @Transient
    public boolean isOutgoing() {
        return address != null && address.getAddress().equals(sourceAddress);
    }
}
