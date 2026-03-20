package com.catalog.models;

import jakarta.persistence.*;

@Entity
@Table(name = "results")
public class Result {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "order_id", nullable = false)
    private int orderId;

    @Column(name = "result_type", columnDefinition = "ENUM('certificate','protocol','report')")
    private String resultType;

    @Column(name = "issued_at")
    private java.time.LocalDateTime issuedAt;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "metrologist_id", nullable = false)
    private int metrologistId;

    @Column(name = "is_signed")
    private boolean isSigned;

    @Column(name = "signed_at")
    private java.time.LocalDateTime signedAt;

    public Result() {}

    public Result(int orderId, String resultType, int metrologistId) {
        this.orderId = orderId;
        this.resultType = resultType;
        this.metrologistId = metrologistId;
        this.isSigned = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getResultType() { return resultType; }
    public void setResultType(String resultType) { this.resultType = resultType; }

    public java.time.LocalDateTime getIssuedAt() { return issuedAt; }
    public void setIssuedAt(java.time.LocalDateTime issuedAt) { this.issuedAt = issuedAt; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public int getMetrologistId() { return metrologistId; }
    public void setMetrologistId(int metrologistId) { this.metrologistId = metrologistId; }

    public boolean isSigned() { return isSigned; }
    public void setSigned(boolean signed) { isSigned = signed; }

    public java.time.LocalDateTime getSignedAt() { return signedAt; }
    public void setSignedAt(java.time.LocalDateTime signedAt) { this.signedAt = signedAt; }
}