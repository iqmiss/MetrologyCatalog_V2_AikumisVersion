package com.catalog.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendStatusUpdate(String toEmail, String fullName, String orderNumber, String newStatus) {
        String subject = "Обновление статуса заявки #" + orderNumber;
        String text = String.format(
            "Уважаемый(ая) %s,\n\n" +
            "Статус вашей заявки #%s изменён на: %s\n\n" +
            "С уважением,\nМетрологическая служба",
            fullName, orderNumber, translateStatus(newStatus)
        );
        send(toEmail, subject, text);
    }

    public void sendOrderCompleted(String toEmail, String fullName, String orderNumber) {
        String subject = "Заявка #" + orderNumber + " завершена";
        String text = String.format(
            "Уважаемый(ая) %s,\n\n" +
            "Ваша заявка #%s успешно завершена.\n" +
            "Вы можете скачать сертификат в личном кабинете.\n\n" +
            "С уважением,\nМетрологическая служба",
            fullName, orderNumber
        );
        send(toEmail, subject, text);
    }

    public void sendPasswordReset(String toEmail, String fullName, String resetLink) {
        String subject = "Восстановление пароля";
        String text = String.format(
            "Уважаемый(ая) %s,\n\n" +
            "Для сброса пароля перейдите по ссылке:\n%s\n\n" +
            "Ссылка действительна 24 часа.\n" +
            "Если вы не запрашивали сброс пароля — проигнорируйте это письмо.\n\n" +
            "С уважением,\nМетрологическая служба",
            fullName, resetLink
        );
        send(toEmail, subject, text);
    }

    public void sendContractReady(String toEmail, String fullName, String orderNumber) {
        String subject = "Договор по заявке #" + orderNumber + " готов к подписанию";
        String text = String.format(
            "Уважаемый(ая) %s,\n\n" +
            "Договор по вашей заявке #%s подготовлен и ожидает вашей подписи.\n" +
            "Войдите в личный кабинет для ознакомления и подписания.\n\n" +
            "С уважением,\nМетрологическая служба",
            fullName, orderNumber
        );
        send(toEmail, subject, text);
    }

    public void sendReturnedToRevision(String toEmail, String fullName, String orderNumber) {
        String subject = "Заявка #" + orderNumber + " возвращена на доработку";
        String text = String.format(
            "Уважаемый(ая) %s,\n\n" +
            "Ваша заявка #%s возвращена на доработку. " +
            "Пожалуйста, войдите в личный кабинет, ознакомьтесь с комментарием менеджера и внесите необходимые исправления.\n\n" +
            "С уважением,\nМетрологическая служба",
            fullName, orderNumber
        );
        send(toEmail, subject, text);
    }

    public void sendInvoiceReady(String toEmail, String fullName, String orderNumber) {
        String subject = "Счёт на оплату по заявке #" + orderNumber;
        String text = String.format(
            "Уважаемый(ая) %s,\n\n" +
            "Счёт на оплату по заявке #%s доступен в вашем личном кабинете.\n" +
            "Пожалуйста, произведите оплату и прикрепите подтверждение.\n\n" +
            "С уважением,\nМетрологическая служба",
            fullName, orderNumber
        );
        send(toEmail, subject, text);
    }

    public void sendAssignedToLab(String toEmail, String fullName, String orderNumber, String labName) {
        String subject = "Заявка #" + orderNumber + " направлена на исполнение";
        String text = String.format(
            "Уважаемый(ая) %s,\n\n" +
            "Ваша заявка #%s направлена на исполнение в %s.\n" +
            "Вы можете отслеживать статус в личном кабинете.\n\n" +
            "С уважением,\nМетрологическая служба",
            fullName, orderNumber, labName
        );
        send(toEmail, subject, text);
    }

    private void send(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Ошибка отправки email на " + to + ": " + e.getMessage());
        }
    }

    private String translateStatus(String status) {
        return switch (status) {
            case "pending_contract"  -> "Ожидает создания договора";
            case "revision"          -> "Возвращена на доработку";
            case "awaiting_approval" -> "На согласовании";
            case "awaiting_director" -> "У директора";
            case "awaiting_payment"  -> "Ожидает оплаты";
            case "awaiting_delivery" -> "Ожидает доставки";
            case "received_in_lab"   -> "Принято в лабораторию";
            case "in_work"           -> "В работе";
            case "under_review"      -> "На проверке";
            case "completed"         -> "Завершено";
            case "cancelled"         -> "Отменено";
            case "annulled"          -> "Аннулировано";
            case "terminated"        -> "Расторгнуто";
            default                  -> status;
        };
    }
}