package com.catalog.service;

import com.catalog.models.Order;
import com.catalog.models.Result;
import com.catalog.models.Contract;
import com.catalog.models.User;
import com.catalog.repository.OrderRepository;
import com.catalog.repository.ResultRepository;
import com.catalog.repository.UserRepository;
import com.catalog.repository.ServiceRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@PropertySource(value = "classpath:executor.properties", encoding = "UTF-8", ignoreResourceNotFound = true)
public class PdfService {

    private final OrderRepository orderRepository;
    private final ResultRepository resultRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;

    @Value("${executor.name}")
    private String executorName;

    @Value("${executor.bin}")
    private String executorBin;

    @Value("${executor.address}")
    private String executorAddress;

    @Value("${executor.phone}")
    private String executorPhone;

    @Value("${executor.bank}")
    private String executorBank;

    public PdfService(OrderRepository orderRepository,
                      ResultRepository resultRepository,
                      UserRepository userRepository,
                      ServiceRepository serviceRepository) {
        this.orderRepository = orderRepository;
        this.resultRepository = resultRepository;
        this.userRepository = userRepository;
        this.serviceRepository = serviceRepository;
    }

    private PdfFont loadFont() throws Exception {
        byte[] fontBytes = getClass().getResourceAsStream("/fonts/FreeSans.ttf").readAllBytes();
        return PdfFontFactory.createFont(
            fontBytes,
            "Identity-H",
            PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED
        );
    }

    // ─── Договор ─────────────────────────────────────────────────────────────

    public byte[] generateContract(Order order, Contract contract) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            PdfFont font = loadFont();
            document.setFont(font);

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            User client = userRepository.findById(order.getClientId()).orElse(null);
            com.catalog.models.Service service = serviceRepository.findById(order.getServiceId()).orElse(null);

            String clientName  = client != null ? client.getFullName() : "—";
            String clientPhone = client != null && client.getPhone() != null ? client.getPhone() : "—";
            String clientEmail = client != null ? client.getEmail() : "—";
            String serviceName = service != null ? service.getName() : "—";
            String serviceDesc = service != null && service.getDescription() != null ? service.getDescription() : "—";
            String serviceStd  = service != null && service.getStandard() != null ? service.getStandard() : "—";

            document.add(p("ДОГОВОР НА ОКАЗАНИЕ МЕТРОЛОГИЧЕСКИХ УСЛУГ", font)
                    .setFontSize(14).setBold().setTextAlignment(TextAlignment.CENTER));
            document.add(p("№ " + contract.getContractNumber(), font)
                    .setFontSize(11).setTextAlignment(TextAlignment.CENTER));
            document.add(p("г. Астана,  " + LocalDate.now().format(dateFormatter), font)
                    .setFontSize(10).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph(" "));

            document.add(p("1. СТОРОНЫ ДОГОВОРА", font).setFontSize(12).setBold());
            document.add(new Paragraph(" "));

            Table partiesTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .setWidth(UnitValue.createPercentValue(100));
            partiesTable.addCell(cell(p("ИСПОЛНИТЕЛЬ", font).setBold().setTextAlignment(TextAlignment.CENTER)));
            partiesTable.addCell(cell(p("ЗАКАЗЧИК", font).setBold().setTextAlignment(TextAlignment.CENTER)));
            partiesTable.addCell(cell(p(executorName + "\nБИН: " + executorBin +
                    "\nАдрес: " + executorAddress + "\nТел: " + executorPhone, font).setFontSize(9)));
            partiesTable.addCell(cell(p(clientName + "\nТел: " + clientPhone +
                    "\nEmail: " + clientEmail, font).setFontSize(9)));
            document.add(partiesTable);
            document.add(new Paragraph(" "));

            document.add(p("2. ПРЕДМЕТ ДОГОВОРА", font).setFontSize(12).setBold());
            document.add(p("Исполнитель обязуется оказать метрологические услуги, " +
                    "а Заказчик обязуется принять и оплатить их в соответствии с условиями настоящего договора.", font)
                    .setFontSize(10));
            document.add(new Paragraph(" "));

            Table serviceTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                    .setWidth(UnitValue.createPercentValue(100));
            addRow(serviceTable, "Вид услуги:",      serviceName,   font);
            addRow(serviceTable, "Описание:",        serviceDesc,   font);
            addRow(serviceTable, "Стандарт/ГОСТ:",   serviceStd,    font);
            addRow(serviceTable, "Номер заявки:",    order.getOrderNumber(), font);
            if (order.getDueDate() != null)
                addRow(serviceTable, "Срок исполнения:", order.getDueDate().format(dateFormatter), font);
            document.add(serviceTable);
            document.add(new Paragraph(" "));

            document.add(p("3. СТОИМОСТЬ И ПОРЯДОК ОПЛАТЫ", font).setFontSize(12).setBold());
            document.add(new Paragraph(" "));

            Table priceTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                    .setWidth(UnitValue.createPercentValue(100));
            addRow(priceTable, "Сумма договора:", order.getPrice() != null ? String.format("%.2f тенге", order.getPrice()) : "будет объявлена", font);
            addRow(priceTable, "НДС:", "Включён", font);
            addRow(priceTable, "Порядок оплаты:", "Оплата производится до начала оказания услуг", font);
            document.add(priceTable);
            document.add(new Paragraph(" "));

            document.add(p("4. РЕКВИЗИТЫ СТОРОН", font).setFontSize(12).setBold());
            document.add(new Paragraph(" "));

            Table reqTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .setWidth(UnitValue.createPercentValue(100));
            reqTable.addCell(cell(p("ИСПОЛНИТЕЛЬ", font).setBold().setTextAlignment(TextAlignment.CENTER)));
            reqTable.addCell(cell(p("ЗАКАЗЧИК", font).setBold().setTextAlignment(TextAlignment.CENTER)));
            reqTable.addCell(cell(p(executorName + "\nБИН: " + executorBin +
                    "\nАдрес: " + executorAddress + "\nТел: " + executorPhone +
                    "\nБанк: " + executorBank, font).setFontSize(9)));
            reqTable.addCell(cell(p(clientName + "\nТел: " + clientPhone +
                    "\nEmail: " + clientEmail, font).setFontSize(9)));
            document.add(reqTable);
            document.add(new Paragraph(" "));

            document.add(p("5. ПОДПИСИ СТОРОН", font).setFontSize(12).setBold());
            document.add(new Paragraph(" "));

            String clientStatus = contract.isClientSigned()
                    ? "Подписано ЭЦП\n" + (contract.getClientSignedAt() != null
                        ? contract.getClientSignedAt().format(dateTimeFormatter) : "")
                    : "Ожидает подписи";

            String directorStatus = contract.isDirectorSigned()
                    ? "Подписано ЭЦП\n" + (contract.getDirectorSignedAt() != null
                        ? contract.getDirectorSignedAt().format(dateTimeFormatter) : "")
                    : "Ожидает подписи";

            String approvalStatus = switch (contract.getStatus() != null ? contract.getStatus() : "draft") {
                case "draft"            -> "Черновик";
                case "pending_approval" -> "На согласовании";
                case "approved"         -> "Согласовано";
                case "signed"           -> "Подписан";
                case "rejected"         -> "Отклонён";
                case "annulled"         -> "Аннулирован";
                case "terminated"       -> "Расторгнут";
                default                 -> "—";
            };

            Table signTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .setWidth(UnitValue.createPercentValue(100));
            signTable.addCell(cell(p("Директор:\n" + directorStatus + "\n\n" + executorName, font).setFontSize(9)));
            signTable.addCell(cell(p("Заказчик:\n" + clientStatus + "\n\n" + clientName, font).setFontSize(9)));
            document.add(signTable);
            document.add(new Paragraph(" "));
            document.add(p("Статус договора: " + approvalStatus, font).setFontSize(10));

        } catch (Exception e) {
            throw new RuntimeException("Ошибка генерации договора: " + e.getMessage(), e);
        }

        return baos.toByteArray();
    }

    // ─── Счёт на оплату ───────────────────────────────────────────────────────

    public byte[] generateInvoice(int orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Заявка не найдена: " + orderId));

        User client = userRepository.findById(order.getClientId()).orElse(null);
        com.catalog.models.Service service = serviceRepository.findById(order.getServiceId()).orElse(null);

        String clientName  = client != null ? client.getFullName() : "—";
        String clientPhone = client != null && client.getPhone() != null ? client.getPhone() : "—";
        String clientEmail = client != null ? client.getEmail() : "—";
        String serviceName = service != null ? service.getName() : "—";

        // Сумма счёта: если финансист указал отдельную — используем её, иначе сумму договора
        double invoiceTotal = order.getPrice() != null && order.getPrice() > 0
                ? order.getPrice() : 0.0;

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String today = LocalDate.now().format(dateFormatter);
        // Номер счёта: INV-{orderId}-{дата без точек}
        String invoiceNumber = "INV-" + orderId + "-" + LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            PdfFont font = loadFont();
            document.setFont(font);

            // ── Заголовок ──
            document.add(p("СЧЁТ НА ОПЛАТУ", font)
                    .setFontSize(16).setBold().setTextAlignment(TextAlignment.CENTER));
            document.add(p("№ " + invoiceNumber + "  от  " + today, font)
                    .setFontSize(11).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph(" "));

            // ── Реквизиты поставщика ──
            document.add(p("Поставщик:", font).setFontSize(10).setBold());
            document.add(p(executorName, font).setFontSize(10));
            document.add(p("БИН: " + executorBin + "    Адрес: " + executorAddress, font).setFontSize(9));
            document.add(p("Тел: " + executorPhone + "    Банк: " + executorBank, font).setFontSize(9));
            document.add(new Paragraph(" "));

            // ── Реквизиты покупателя ──
            document.add(p("Покупатель:", font).setFontSize(10).setBold());
            document.add(p(clientName, font).setFontSize(10));
            document.add(p("Тел: " + clientPhone + "    Email: " + clientEmail, font).setFontSize(9));
            document.add(new Paragraph(" "));

            // ── Таблица услуг ──
            Table table = new Table(UnitValue.createPercentArray(new float[]{10, 45, 15, 15, 15}))
                    .setWidth(UnitValue.createPercentValue(100));

            // Заголовок таблицы
            String[] headers = {"№", "Наименование услуги", "Ед.изм.", "Кол-во", "Сумма"};
            for (String h : headers) {
                table.addHeaderCell(
                    new Cell().add(p(h, font).setFontSize(9).setBold())
                        .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                        .setPadding(4)
                );
            }

            // Строка услуги
            String[] row = {
                "1",
                serviceName + "\n(заявка № " + order.getOrderNumber() + ")",
                "усл.",
                "1",
                String.format("%.2f ₸", invoiceTotal)
            };
            for (String val : row) {
                table.addCell(new Cell().add(p(val, font).setFontSize(9)).setPadding(4));
            }

            document.add(table);
            document.add(new Paragraph(" "));

            // ── Итоговая строка ──
            document.add(p("Итого к оплате: " + String.format("%.2f тенге", invoiceTotal), font)
                    .setFontSize(12).setBold().setTextAlignment(TextAlignment.RIGHT));
            document.add(p("НДС: включён в стоимость", font)
                    .setFontSize(9).setTextAlignment(TextAlignment.RIGHT)
                    .setFontColor(ColorConstants.GRAY));
            document.add(new Paragraph(" "));

            // ── Реквизиты для оплаты ──
            document.add(p("Реквизиты для оплаты:", font).setFontSize(10).setBold());
            document.add(p("Банк: " + executorBank, font).setFontSize(9));
            document.add(p("Назначение платежа: Оплата по договору, заявка № " + order.getOrderNumber(), font).setFontSize(9));
            document.add(new Paragraph(" "));

            // ── Подпись ──
            Table signTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .setWidth(UnitValue.createPercentValue(100));
            signTable.addCell(new Cell()
                .add(p("Финансовый директор: _______________", font).setFontSize(9))
                .setBorder(Border.NO_BORDER));
            signTable.addCell(new Cell()
                .add(p("М.П.", font).setFontSize(9).setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER));
            document.add(signTable);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка генерации счёта: " + e.getMessage(), e);
        }

        return baos.toByteArray();
    }

    // ─── Сертификат / протокол / отчёт ───────────────────────────────────────

    public byte[] generateCertificate(int orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) throw new RuntimeException("Order not found: " + orderId);

        Result result = resultRepository.findByOrderId(orderId).orElse(null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            PdfFont font = loadFont();
            document.setFont(font);

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            String docTitle = getDocumentTitle(result);
            document.add(p(docTitle, font).setFontSize(18).setBold().setTextAlignment(TextAlignment.CENTER));
            document.add(p("№ " + order.getOrderNumber(), font).setFontSize(12).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph(" "));

            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                    .setWidth(UnitValue.createPercentValue(100));
            addRow(infoTable, "Номер заказа:", order.getOrderNumber(), font);
            addRow(infoTable, "Статус:", order.getStatus(), font);
            addRow(infoTable, "Стоимость:", String.format("%.2f тг", order.getPrice() != null ? order.getPrice() : 0.0), font);
            if (order.getDueDate() != null)
                addRow(infoTable, "Срок исполнения:", order.getDueDate().format(dateFormatter), font);
            document.add(infoTable);
            document.add(new Paragraph(" "));

            if (result != null) {
                document.add(p("Результат поверки", font).setFontSize(14).setBold());
                Table resultTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                        .setWidth(UnitValue.createPercentValue(100));
                addRow(resultTable, "Тип документа:", getDocumentTitle(result), font);
                addRow(resultTable, "Подписан:", result.isSigned() ? "Да" : "Нет", font);
                if (result.getIssuedAt() != null)
                    addRow(resultTable, "Дата выдачи:", result.getIssuedAt().format(dateTimeFormatter), font);
                if (result.isSigned() && result.getSignedAt() != null)
                    addRow(resultTable, "Дата подписания:", result.getSignedAt().format(dateTimeFormatter), font);
                document.add(resultTable);
            } else {
                document.add(p("Результат поверки ещё не добавлен.", font)
                        .setItalic().setFontColor(ColorConstants.GRAY));
            }

            document.add(new Paragraph(" "));
            document.add(p("Подписи сторон", font).setFontSize(14).setBold());
            Table signTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .setWidth(UnitValue.createPercentValue(100));
            signTable.addCell(cell(p("Метролог: _______________", font)));
            signTable.addCell(cell(p("Клиент: _______________", font)));
            document.add(signTable);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка генерации PDF: " + e.getMessage(), e);
        }

        return baos.toByteArray();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Paragraph p(String text, PdfFont font) {
        return new Paragraph(text).setFont(font);
    }

    private Cell cell(Paragraph paragraph) {
        return new Cell().add(paragraph);
    }

    private void addRow(Table table, String label, String value, PdfFont font) {
        table.addCell(cell(p(label, font).setBold()));
        table.addCell(cell(p(value != null ? value : "—", font)));
    }

    private String getDocumentTitle(Result result) {
        if (result == null) return "Документ";
        return switch (result.getResultType()) {
            case "certificate" -> "Сертификат";
            case "protocol"    -> "Протокол";
            case "report"      -> "Отчёт";
            default            -> "Документ";
        };
    }
}