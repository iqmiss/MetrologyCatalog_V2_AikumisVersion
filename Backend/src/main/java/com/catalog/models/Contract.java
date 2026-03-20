package com.catalog.models;

public class Contract {
    private int id;
    private int orderId;
    private String contractNumber;
    private java.time.LocalDateTime signedAt;
    private String filePath;
    private boolean isSigned;
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