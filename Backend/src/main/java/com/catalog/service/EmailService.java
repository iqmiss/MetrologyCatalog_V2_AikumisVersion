package com.catalog.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

// Сервис для отправки email уведомлений через SMTP
// Использует Mailtrap для тестирования (настройки в application.properties)
@Service
public class EmailService {

    // JavaMailSender — Spring компонент для отправки писем через SMTP
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Отправляет уведомление клиенту при изменении статуса заявки
    // Вызывается из OrderController.updateOrderStatus() при каждой смене статуса
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

    // Отправляет специальное уведомление когда заявка полностью завершена
    // Сообщает клиенту что сертификат доступен для скачивания в личном кабинете
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

    // Отправляет ссылку для сброса пароля
    // Ссылка содержит UUID токен который действителен до перезапуска сервера
    public void sendPasswordReset(String toEmail, String fullName, String resetLink) {
        String subject = "Восстановление пароля";
        String text = String.format(
            "Уважаемый(ая) %s,\n\n" +
            "Для сброса пароля перейдите по ссылке:\n%s\n\n" +
            "Ссылка действительна до перезапуска сервера.\n" +
            "Если вы не запрашивали сброс пароля — проигнорируйте это письмо.\n\n" +
            "С уважением,\nМетрологическая служба",
            fullName, resetLink
        );
        send(toEmail, subject, text);
    }

    // Внутренний метод для формирования и отправки письма через SMTP
    // Все публичные методы используют его для отправки
    private void send(String to, String subject, String text) {
        try {
            System.out.println("Отправка email на: " + to);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
            System.out.println("Email отправлен успешно");
        } catch (Exception e) {
            System.err.println("Ошибка отправки email: " + e.getMessage());
        }
    }

    // Переводит технические названия статусов на русский язык для email
    private String translateStatus(String status) {
        return switch (status) {
            case "new"               -> "Новая";
            case "awaiting_payment"  -> "Ожидает оплаты";
            case "awaiting_delivery" -> "Ожидает доставки";
            case "received_in_lab"   -> "Принято в лабораторию";
            case "in_work"           -> "В работе";
            case "under_review"      -> "На проверке";
            case "completed"         -> "Завершено";
            default                  -> status;
        };
    }
}