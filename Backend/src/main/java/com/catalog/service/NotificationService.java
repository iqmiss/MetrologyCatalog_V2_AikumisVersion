package com.catalog.service;

import com.catalog.models.Notification;
import com.catalog.repository.NotificationRepository;
import com.catalog.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Централизованный сервис уведомлений.
 * Вызывается из контроллеров при ключевых переходах статусов.
 *
 * Типы уведомлений:
 *   order_status       — изменение статуса заявки
 *   document_ready     — документ готов к скачиванию
 *   approval_required  — требуется согласование/подпись
 *   payment_received   — оплата получена
 *   assigned_to_lab    — заявка направлена в лабораторию
 *   receipt_uploaded   — клиент загрузил чек
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository,
                               EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    // ─── Базовый метод ────────────────────────────────────────────────────────

    public void create(int userId, Integer orderId, String message, String type) {
        Notification n = new Notification(userId, message, type);
        n.setOrderId(orderId);
        notificationRepository.save(n);
    }

    // ─── Хелперы для конкретных событий ──────────────────────────────────────

    /** Клиент создал заявку → уведомляем всех менеджеров */
    public void notifyManagersNewOrder(String orderNumber) {
        userRepository.findByRole("manager").forEach(manager ->
            create(manager.getId(), null,
                "Новая заявка " + orderNumber + " ожидает обработки",
                "order_status")
        );
    }

    /** Менеджер вернул заявку на доработку → уведомляем клиента */
    public void notifyClientRevision(int clientId, Integer orderId, String orderNumber) {
        create(clientId, orderId,
            "Заявка " + orderNumber + " возвращена на доработку. Проверьте комментарий менеджера.",
            "order_status");
        userRepository.findById(clientId).ifPresent(client ->
            emailService.sendReturnedToRevision(client.getEmail(), client.getFullName(), orderNumber));
    }

    /** Клиент повторно отправил заявку → уведомляем менеджеров */
    public void notifyManagersResubmit(String orderNumber) {
        userRepository.findByRole("manager").forEach(manager ->
            create(manager.getId(), null,
                "Заявка " + orderNumber + " исправлена клиентом и ожидает повторной проверки",
                "order_status")
        );
    }

    /** Менеджер создал договор → уведомляем согласующих */
    public void notifyApproversContractReady(Integer orderId, String orderNumber) {
        userRepository.findByRole("approver").forEach(approver ->
            create(approver.getId(), orderId,
                "Договор по заявке " + orderNumber + " ожидает вашего согласования",
                "approval_required")
        );
    }

    /**
     * Параллельное согласование: уведомляем все три роли одновременно —
     * согласующего, финансиста и директора
     */
    public void notifyParallelApprovers(Integer orderId, String orderNumber) {
        String message = "Договор по заявке " + orderNumber + " ожидает вашей подписи";
        userRepository.findByRole("approver").forEach(u ->
            create(u.getId(), orderId, message, "approval_required")
        );
        userRepository.findByRole("financier").forEach(u ->
            create(u.getId(), orderId, message, "approval_required")
        );
        userRepository.findByRole("director").forEach(u ->
            create(u.getId(), orderId, message, "approval_required")
        );
    }

    /** Согласующий одобрил → уведомляем директора */
    public void notifyDirectorApproved(Integer orderId, String orderNumber) {
        userRepository.findByRole("director").forEach(director ->
            create(director.getId(), orderId,
                "Договор по заявке " + orderNumber + " согласован и ожидает вашей подписи",
                "approval_required")
        );
    }

    /** Согласующий отклонил → уведомляем менеджеров */
    public void notifyManagersRejected(Integer orderId, String orderNumber, String reason) {
        userRepository.findByRole("manager").forEach(manager ->
            create(manager.getId(), orderId,
                "Договор по заявке " + orderNumber + " отклонён. Причина: " + reason,
                "order_status")
        );
    }

    /** Директор подписал → уведомляем финансистов */
    public void notifyFinanciersDirectorSigned(Integer orderId, String orderNumber) {
        userRepository.findByRole("financier").forEach(financier ->
            create(financier.getId(), orderId,
                "Договор по заявке " + orderNumber + " подписан директором. Сформируйте счёт на оплату.",
                "document_ready")
        );
    }

    /** Менеджер отправил счёт → уведомляем клиента */
    public void notifyClientInvoiceSent(int clientId, Integer orderId, String orderNumber) {
        create(clientId, orderId,
            "Счёт на оплату по заявке " + orderNumber + " доступен в личном кабинете",
            "document_ready");
        userRepository.findById(clientId).ifPresent(client ->
            emailService.sendInvoiceReady(client.getEmail(), client.getFullName(), orderNumber));
    }

    /** Клиент загрузил чек → уведомляем финансистов */
    public void notifyFinanciersReceiptUploaded(Integer orderId, String orderNumber) {
        userRepository.findByRole("financier").forEach(financier ->
            create(financier.getId(), orderId,
                "Клиент загрузил чек об оплате по заявке " + orderNumber + ". Подтвердите оплату.",
                "receipt_uploaded")
        );
    }

    /** Финансист подтвердил оплату → уведомляем директора и менеджеров */
    public void notifyPaymentConfirmed(Integer orderId, String orderNumber) {
        List<String> roles = List.of("director", "manager");
        roles.forEach(role ->
            userRepository.findByRole(role).forEach(u ->
                create(u.getId(), orderId,
                    "Оплата по заявке " + orderNumber + " подтверждена. Направьте на исполнение.",
                    "payment_received")
            )
        );
    }

    /** Директор направил в лабораторию → уведомляем метрологов этой лаборатории + клиента */
    public void notifyAssignedToLab(int clientId, Integer orderId, String orderNumber, String labName) {
        create(clientId, orderId,
            "Ваша заявка " + orderNumber + " направлена на исполнение в " + labName,
            "assigned_to_lab");
        userRepository.findById(clientId).ifPresent(client ->
            emailService.sendAssignedToLab(client.getEmail(), client.getFullName(), orderNumber, labName));
        userRepository.findByRole("metrolog").forEach(metrolog ->
            create(metrolog.getId(), orderId,
                "Новая заявка " + orderNumber + " направлена в вашу лабораторию (" + labName + ")",
                "assigned_to_lab")
        );
    }

    /** Метролог взял в работу / изменил статус → уведомляем клиента */
    public void notifyClientStatusChanged(int clientId, Integer orderId, String orderNumber, String statusLabel) {
        create(clientId, orderId,
            "Статус вашей заявки " + orderNumber + " изменён: " + statusLabel,
            "order_status");
    }

    /** Заявка завершена → уведомляем клиента */
    public void notifyClientCompleted(int clientId, Integer orderId, String orderNumber) {
        create(clientId, orderId,
            "Заявка " + orderNumber + " выполнена. Скачайте документы в личном кабинете.",
            "document_ready");
        userRepository.findById(clientId).ifPresent(client ->
            emailService.sendOrderCompleted(client.getEmail(), client.getFullName(), orderNumber));
    }

    /** Тройка подписала → уведомляем клиента что его очередь */
    public void notifyClientTrioSigned(int clientId, Integer orderId, String orderNumber) {
        create(clientId, orderId,
            "Договор по заявке " + orderNumber + " подписан организацией. Теперь ваша очередь подписать.",
            "document_ready");
        userRepository.findById(clientId).ifPresent(client ->
            emailService.sendContractReady(client.getEmail(), client.getFullName(), orderNumber));
    }

    /** Клиент подписал → уведомляем ген.директора что его очередь */
    public void notifyGenDirectorForSigning(Integer orderId, String orderNumber) {
        userRepository.findByRole("gen_director").forEach(u ->
            create(u.getId(), orderId,
                "Договор по заявке " + orderNumber + " подписан клиентом. Ожидается ваша финальная подпись.",
                "approval_required")
        );
    }


    /** Ген.директор подписал → финансист формирует счёт */
    public void notifyFinanciersContractSigned(Integer orderId, String orderNumber) {
        userRepository.findByRole("financier").forEach(u ->
            create(u.getId(), orderId,
                "Договор по заявке " + orderNumber + " полностью подписан. Сформируйте счёт на оплату.",
                "document_ready")
        );
    }

    /** Финансист сформировал счёт → менеджер отправляет клиенту */
    public void notifyManagerInvoiceReady(Integer orderId, String orderNumber) {
        userRepository.findByRole("manager").forEach(u ->
            create(u.getId(), orderId,
                "Счёт по заявке " + orderNumber + " сформирован финансистом. Отправьте клиенту.",
                "document_ready")
        );
    }

    /** Финансист подтвердил оплату → менеджер уведомляет руководителя */
    public void notifyManagerPaymentConfirmed(Integer orderId, String orderNumber) {
        userRepository.findByRole("manager").forEach(u ->
            create(u.getId(), orderId,
                "Оплата по заявке " + orderNumber + " подтверждена финансистом.",
                "payment_received")
        );
    }

    /** Менеджер уведомляет руководителя — направить на исполнение */
    public void notifyDirectorToAssign(Integer orderId, String orderNumber) {
        userRepository.findByRole("director").forEach(u ->
            create(u.getId(), orderId,
                "Оплата по заявке " + orderNumber + " получена. Направьте заявку на исполнение.",
                "payment_received")
        );
    }

}