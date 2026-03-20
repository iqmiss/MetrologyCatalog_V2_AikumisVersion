package com.catalog.models;

import jakarta.persistence.*;

@Entity
@Table(name = "contracts")
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "order_id", nullable = false, unique = true)
    private int orderId;

    @Column(name = "contract_number", nullable = false, unique = true)
    private String contractNumber;

    @Column(name = "signed_at")
    private java.time.LocalDateTime signedAt;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "is_signed")
    private boolean isSigned;

    @Column(name = "signed_by")
    private Integer signedBy;

    public Contract() {}

    public Contract(int orderId, String contractNumber) {
        this.orderId = orderId;
        this.contractNumber = contractNumber;
        this.isSigned = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getContractNumber() { return contractNumber; }
    public void setContractNumber(String contractNumber) { this.contractNumber = contractNumber; }

    public java.time.LocalDateTime getSignedAt() { return signedAt; }
    public void setSignedAt(java.time.LocalDateTime signedAt) { this.signedAt = signedAt; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public boolean isSigned() { return isSigned; }
    public void setSigned(boolean signed) { isSigned = signed; }

    public Integer getSignedBy() { return signedBy; }
    public void setSignedBy(Integer signedBy) { this.signedBy = signedBy; }
}