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

    // Регистрационный номер — присваивается при подписи ген.директора
    @Column(name = "registration_number")
    private String registrationNumber;

    @Column(name = "file_path")
    private String filePath;

    // Файл договора загруженный менеджером (Base64)
    @Column(name = "contract_file", columnDefinition = "MEDIUMTEXT")
    private String contractFile;

    @Column(name = "contract_file_name")
    private String contractFileName;

    @Column(columnDefinition = "ENUM('draft','pending_approval','approved','signed','rejected','annulled','terminated')")
    private String status;

    // Параллельная тройка(руководитель, согласующий, финансист)
    @Column(name = "director_signed")
    private boolean directorSigned;
    @Column(name = "director_signed_at")
    private LocalDateTime directorSignedAt;
    @Column(name = "director_signed_by")
    private Integer directorSignedBy;

    @Column(name = "approver_signed")
    private boolean approverSigned;
    @Column(name = "approver_signed_at")
    private LocalDateTime approverSignedAt;
    @Column(name = "approver_signed_by")
    private Integer approverSignedBy;

    @Column(name = "financier_signed")
    private boolean financierSigned;
    @Column(name = "financier_signed_at")
    private LocalDateTime financierSignedAt;
    @Column(name = "financier_signed_by")
    private Integer financierSignedBy;

    // Клиент
    @Column(name = "client_signed")
    private boolean clientSigned;
    @Column(name = "client_signed_at")
    private LocalDateTime clientSignedAt;
    @Column(name = "client_signed_by")
    private Integer clientSignedBy;

    // Ген.директор (финальная подпись)
    @Column(name = "gen_director_signed")
    private boolean genDirectorSigned;
    @Column(name = "gen_director_signed_at")
    private LocalDateTime genDirectorSignedAt;
    @Column(name = "gen_director_signed_by")
    private Integer genDirectorSignedBy;

    @Column(name = "rejected_by_role")
    private String rejectedByRole;
    @Column(name = "rejected_reason", columnDefinition = "TEXT")
    private String rejectedReason;

    @Column(name = "annulled_at")
    private LocalDateTime annulledAt;
    @Column(name = "annulled_by")
    private Integer annulledBy;
    @Column(name = "annulled_reason")
    private String annulledReason;

    @Column(name = "terminated_at")
    private LocalDateTime terminatedAt;
    @Column(name = "terminated_by")
    private Integer terminatedBy;
    @Column(name = "terminated_reason")
    private String terminatedReason;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // Тройка(руководитель, согласующий, финансист) подписала
    public boolean isTrioSigned() {
        return directorSigned && approverSigned && financierSigned;
    }

    public boolean isFullySigned() {
        return isTrioSigned() && clientSigned && genDirectorSigned;
    }

    public Contract() {}

    public Contract(int orderId, String contractNumber) {
        this.orderId = orderId;
        this.contractNumber = contractNumber;
        this.status = "draft";
        this.directorSigned = false;
        this.approverSigned = false;
        this.financierSigned = false;
        this.clientSigned = false;
        this.genDirectorSigned = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public String getContractNumber() { return contractNumber; }
    public void setContractNumber(String contractNumber) { this.contractNumber = contractNumber; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getContractFile() { return contractFile; }
    public void setContractFile(String contractFile) { this.contractFile = contractFile; }
    public String getContractFileName() { return contractFileName; }
    public void setContractFileName(String contractFileName) { this.contractFileName = contractFileName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isDirectorSigned() { return directorSigned; }
    public void setDirectorSigned(boolean v) { this.directorSigned = v; }
    public LocalDateTime getDirectorSignedAt() { return directorSignedAt; }
    public void setDirectorSignedAt(LocalDateTime v) { this.directorSignedAt = v; }
    public Integer getDirectorSignedBy() { return directorSignedBy; }
    public void setDirectorSignedBy(Integer v) { this.directorSignedBy = v; }
    public boolean isApproverSigned() { return approverSigned; }
    public void setApproverSigned(boolean v) { this.approverSigned = v; }
    public LocalDateTime getApproverSignedAt() { return approverSignedAt; }
    public void setApproverSignedAt(LocalDateTime v) { this.approverSignedAt = v; }
    public Integer getApproverSignedBy() { return approverSignedBy; }
    public void setApproverSignedBy(Integer v) { this.approverSignedBy = v; }
    public boolean isFinancierSigned() { return financierSigned; }
    public void setFinancierSigned(boolean v) { this.financierSigned = v; }
    public LocalDateTime getFinancierSignedAt() { return financierSignedAt; }
    public void setFinancierSignedAt(LocalDateTime v) { this.financierSignedAt = v; }
    public Integer getFinancierSignedBy() { return financierSignedBy; }
    public void setFinancierSignedBy(Integer v) { this.financierSignedBy = v; }
    public boolean isClientSigned() { return clientSigned; }
    public void setClientSigned(boolean v) { this.clientSigned = v; }
    public LocalDateTime getClientSignedAt() { return clientSignedAt; }
    public void setClientSignedAt(LocalDateTime v) { this.clientSignedAt = v; }
    public Integer getClientSignedBy() { return clientSignedBy; }
    public void setClientSignedBy(Integer v) { this.clientSignedBy = v; }
    public boolean isGenDirectorSigned() { return genDirectorSigned; }
    public void setGenDirectorSigned(boolean v) { this.genDirectorSigned = v; }
    public LocalDateTime getGenDirectorSignedAt() { return genDirectorSignedAt; }
    public void setGenDirectorSignedAt(LocalDateTime v) { this.genDirectorSignedAt = v; }
    public Integer getGenDirectorSignedBy() { return genDirectorSignedBy; }
    public void setGenDirectorSignedBy(Integer v) { this.genDirectorSignedBy = v; }
    public String getRejectedByRole() { return rejectedByRole; }
    public void setRejectedByRole(String v) { this.rejectedByRole = v; }
    public String getRejectedReason() { return rejectedReason; }
    public void setRejectedReason(String v) { this.rejectedReason = v; }
    public LocalDateTime getAnnulledAt() { return annulledAt; }
    public void setAnnulledAt(LocalDateTime v) { this.annulledAt = v; }
    public Integer getAnnulledBy() { return annulledBy; }
    public void setAnnulledBy(Integer v) { this.annulledBy = v; }
    public String getAnnulledReason() { return annulledReason; }
    public void setAnnulledReason(String v) { this.annulledReason = v; }
    public LocalDateTime getTerminatedAt() { return terminatedAt; }
    public void setTerminatedAt(LocalDateTime v) { this.terminatedAt = v; }
    public Integer getTerminatedBy() { return terminatedBy; }
    public void setTerminatedBy(Integer v) { this.terminatedBy = v; }
    public String getTerminatedReason() { return terminatedReason; }
    public void setTerminatedReason(String v) { this.terminatedReason = v; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}