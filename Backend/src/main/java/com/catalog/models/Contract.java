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

    // Статус договора
    @Column(columnDefinition = "ENUM('draft','pending_approval','approved','signed','rejected','annulled','terminated')")
    private String status;

    // Подпись клиента
    @Column(name = "client_signed")
    private boolean clientSigned;

    @Column(name = "client_signed_at")
    private LocalDateTime clientSignedAt;

    @Column(name = "client_signed_by")
    private Integer clientSignedBy;

    // Подпись директора
    @Column(name = "director_signed")
    private boolean directorSigned;

    @Column(name = "director_signed_at")
    private LocalDateTime directorSignedAt;

    @Column(name = "director_signed_by")
    private Integer directorSignedBy;

    // Аннулирование
    @Column(name = "annulled_at")
    private LocalDateTime annulledAt;

    @Column(name = "annulled_by")
    private Integer annulledBy;

    @Column(name = "annulled_reason")
    private String annulledReason;

    // Расторжение
    @Column(name = "terminated_at")
    private LocalDateTime terminatedAt;

    @Column(name = "terminated_by")
    private Integer terminatedBy;

    @Column(name = "terminated_reason")
    private String terminatedReason;

    // Договор полностью подписан когда оба подписали
    public boolean isFullySigned() {
        return clientSigned && directorSigned;
    }

    public Contract() {}

    public Contract(int orderId, String contractNumber) {
        this.orderId = orderId;
        this.contractNumber = contractNumber;
        this.status = "draft";
        this.clientSigned = false;
        this.directorSigned = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getContractNumber() { return contractNumber; }
    public void setContractNumber(String contractNumber) { this.contractNumber = contractNumber; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isClientSigned() { return clientSigned; }
    public void setClientSigned(boolean clientSigned) { this.clientSigned = clientSigned; }

    public LocalDateTime getClientSignedAt() { return clientSignedAt; }
    public void setClientSignedAt(LocalDateTime clientSignedAt) { this.clientSignedAt = clientSignedAt; }

    public Integer getClientSignedBy() { return clientSignedBy; }
    public void setClientSignedBy(Integer clientSignedBy) { this.clientSignedBy = clientSignedBy; }

    public boolean isDirectorSigned() { return directorSigned; }
    public void setDirectorSigned(boolean directorSigned) { this.directorSigned = directorSigned; }

    public LocalDateTime getDirectorSignedAt() { return directorSignedAt; }
    public void setDirectorSignedAt(LocalDateTime directorSignedAt) { this.directorSignedAt = directorSignedAt; }

    public Integer getDirectorSignedBy() { return directorSignedBy; }
    public void setDirectorSignedBy(Integer directorSignedBy) { this.directorSignedBy = directorSignedBy; }

    public LocalDateTime getAnnulledAt() { return annulledAt; }
    public void setAnnulledAt(LocalDateTime annulledAt) { this.annulledAt = annulledAt; }

    public Integer getAnnulledBy() { return annulledBy; }
    public void setAnnulledBy(Integer annulledBy) { this.annulledBy = annulledBy; }

    public String getAnnulledReason() { return annulledReason; }
    public void setAnnulledReason(String annulledReason) { this.annulledReason = annulledReason; }
    
    public LocalDateTime getTerminatedAt() { return terminatedAt; }
    public void setTerminatedAt(LocalDateTime terminatedAt) { this.terminatedAt = terminatedAt; }

    public Integer getTerminatedBy() { return terminatedBy; }
    public void setTerminatedBy(Integer terminatedBy) { this.terminatedBy = terminatedBy; }

    public String getTerminatedReason() { return terminatedReason; }
    public void setTerminatedReason(String terminatedReason) { this.terminatedReason = terminatedReason; }
}