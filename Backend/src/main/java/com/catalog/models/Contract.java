package com.catalog.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

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

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "client_signed")
    private boolean clientSigned;

    @Column(name = "client_signed_at")
    private LocalDateTime clientSignedAt;

    @Column(name = "client_signed_by")
    private Integer clientSignedBy;

    // Подпись менеджера
    @Column(name = "manager_signed")
    private boolean managerSigned;

    @Column(name = "manager_signed_at")
    private LocalDateTime managerSignedAt;

    @Column(name = "manager_signed_by")
    private Integer managerSignedBy;

    public boolean isFullySigned() {
        return clientSigned && managerSigned;
    }

    public Contract() {}

    public Contract(int orderId, String contractNumber) {
        this.orderId = orderId;
        this.contractNumber = contractNumber;
        this.clientSigned = false;
        this.managerSigned = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getContractNumber() { return contractNumber; }
    public void setContractNumber(String contractNumber) { this.contractNumber = contractNumber; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public boolean isClientSigned() { return clientSigned; }
    public void setClientSigned(boolean clientSigned) { this.clientSigned = clientSigned; }

    public LocalDateTime getClientSignedAt() { return clientSignedAt; }
    public void setClientSignedAt(LocalDateTime clientSignedAt) { this.clientSignedAt = clientSignedAt; }

    public Integer getClientSignedBy() { return clientSignedBy; }
    public void setClientSignedBy(Integer clientSignedBy) { this.clientSignedBy = clientSignedBy; }

    public boolean isManagerSigned() { return managerSigned; }
    public void setManagerSigned(boolean managerSigned) { this.managerSigned = managerSigned; }

    public LocalDateTime getManagerSignedAt() { return managerSignedAt; }
    public void setManagerSignedAt(LocalDateTime managerSignedAt) { this.managerSignedAt = managerSignedAt; }

    public Integer getManagerSignedBy() { return managerSignedBy; }
    public void setManagerSignedBy(Integer managerSignedBy) { this.managerSignedBy = managerSignedBy; }
}