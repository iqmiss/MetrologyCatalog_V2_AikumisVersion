package com.catalog.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @Column(name = "client_id", nullable = false)
    private int clientId;

    @Column(name = "service_id", nullable = false)
    private int serviceId;

    @Column(name = "lab_id", nullable = false)
    private int labId;

    @Column(name = "assigned_lab_id")
    private Integer assignedLabId;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(columnDefinition = "ENUM('pending_contract','revision','awaiting_approval','awaiting_director','awaiting_payment','pending_delivery','awaiting_delivery','received_in_lab','in_work','under_review','completed','cancelled','annulled','terminated')")
    private String status;

    // null = цена ещё не объявлена финансистом
    @Column(name = "price", columnDefinition = "DECIMAL(10,2)")
    private Double price;

    @Column(name = "due_date")
    private java.time.LocalDate dueDate;

    @Column(name = "metrologist_id")
    private Integer metrologistId;

    @Column(name = "payment_comment")
    private String paymentComment;

    @Column(name = "client_comment")
    private String clientComment;

    @Column(name = "manager_comment")
    private String managerComment;

    @Column(name = "invoice_sent", nullable = false)
    private boolean invoiceSent = false;

    @Column(name = "payment_receipt", columnDefinition = "MEDIUMTEXT")
    private String paymentReceipt;

    @Column(name = "payment_receipt_name")
    private String paymentReceiptName;

    @Column(name = "receipt_uploaded_at")
    private LocalDateTime receiptUploadedAt;

    @Column(name = "subservice_id")
private Integer subserviceId;

@Column(name = "secondary_status")
private String secondaryStatus;

@Column(name = "application_code")
private String applicationCode;

@Column(name = "service_address")
private String serviceAddress;

@Column(name = "responsible_department")
private String responsibleDepartment;

@Column(name = "signer_user_id")
private Integer signerUserId;

@Column(name = "client_edit_enabled")
private boolean clientEditEnabled = false;

@Column(name = "form_locked")
private boolean formLocked = false;

    public Order() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public int getClientId() { return clientId; }
    public void setClientId(int clientId) { this.clientId = clientId; }
    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }
    public int getLabId() { return labId; }
    public void setLabId(int labId) { this.labId = labId; }
    public Integer getAssignedLabId() { return assignedLabId; }
    public void setAssignedLabId(Integer assignedLabId) { this.assignedLabId = assignedLabId; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public java.time.LocalDate getDueDate() { return dueDate; }
    public void setDueDate(java.time.LocalDate dueDate) { this.dueDate = dueDate; }
    public Integer getMetrologistId() { return metrologistId; }
    public void setMetrologistId(Integer metrologistId) { this.metrologistId = metrologistId; }
    public String getPaymentComment() { return paymentComment; }
    public void setPaymentComment(String paymentComment) { this.paymentComment = paymentComment; }
    public String getClientComment() { return clientComment; }
    public void setClientComment(String clientComment) { this.clientComment = clientComment; }
    public String getManagerComment() { return managerComment; }
    public void setManagerComment(String managerComment) { this.managerComment = managerComment; }
    public boolean isInvoiceSent() { return invoiceSent; }
    public void setInvoiceSent(boolean invoiceSent) { this.invoiceSent = invoiceSent; }
    public String getPaymentReceipt() { return paymentReceipt; }
    public void setPaymentReceipt(String paymentReceipt) { this.paymentReceipt = paymentReceipt; }
    public String getPaymentReceiptName() { return paymentReceiptName; }
    public void setPaymentReceiptName(String paymentReceiptName) { this.paymentReceiptName = paymentReceiptName; }
    public LocalDateTime getReceiptUploadedAt() { return receiptUploadedAt; }
    public void setReceiptUploadedAt(LocalDateTime receiptUploadedAt) { this.receiptUploadedAt = receiptUploadedAt; }

    public Integer getSubserviceId() { return subserviceId; }
    public void setSubserviceId(Integer subserviceId) { this.subserviceId = subserviceId; }

    public String getSecondaryStatus() { return secondaryStatus; }
    public void setSecondaryStatus(String secondaryStatus) { this.secondaryStatus = secondaryStatus; }

    public String getApplicationCode() { return applicationCode; }
    public void setApplicationCode(String applicationCode) { this.applicationCode = applicationCode; }

    public String getServiceAddress() { return serviceAddress; }
    public void setServiceAddress(String serviceAddress) { this.serviceAddress = serviceAddress; }

    public String getResponsibleDepartment() { return responsibleDepartment; }
    public void setResponsibleDepartment(String responsibleDepartment) { this.responsibleDepartment = responsibleDepartment; }

    public Integer getSignerUserId() { return signerUserId; }
    public void setSignerUserId(Integer signerUserId) { this.signerUserId = signerUserId; }

    public boolean isClientEditEnabled() { return clientEditEnabled; }
    public void setClientEditEnabled(boolean clientEditEnabled) { this.clientEditEnabled = clientEditEnabled; }

    public boolean isFormLocked() { return formLocked; }
    public void setFormLocked(boolean formLocked) { this.formLocked = formLocked; }
        }