package com.catalog.models;

public class Result {
    private int id;
    private int orderId;
    private String resultType;
    private java.time.LocalDateTime issuedAt;
    private String filePath;
    private int metrologistId;
    private boolean isSigned;
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