package com.catalog.service;

import com.catalog.models.Order;
import com.catalog.models.Result;
import com.catalog.models.Contract;
import com.catalog.repository.OrderRepository;
import com.catalog.repository.ResultRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

// Сервис для генерации PDF документов через библиотеку iText 7
// Генерирует два типа документов: договор и сертификат/протокол/отчёт
@Service
public class PdfService {

    private final OrderRepository orderRepository;
    private final ResultRepository resultRepository;

    public PdfService(OrderRepository orderRepository, ResultRepository resultRepository) {
        this.orderRepository = orderRepository;
        this.resultRepository = resultRepository;
    }

    // Генерирует PDF договора на основе данных заявки и договора
    // Вызывается из ContractController.downloadContract()
    public byte[] generateContract(Order order, Contract contract) {
        // ByteArrayOutputStream хранит PDF в памяти вместо записи в файл
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf)) {

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            // Заголовок договора
            document.add(new Paragraph("ДОГОВОР НА ОКАЗАНИЕ МЕТРОЛОГИЧЕСКИХ УСЛУГ")
                    .setFontSize(16).setBold().setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("№ " + contract.getContractNumber())
                    .setFontSize(12).setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Дата: " + java.time.LocalDateTime.now().format(dateFormatter))
                    .setFontSize(11).setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(" "));

            // Раздел: стороны договора (исполнитель и заказчик)
            document.add(new Paragraph("СТОРОНЫ ДОГОВОРА").setFontSize(13).setBold());
            document.add(new Paragraph(" "));

            Table partiesTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .setWidth(UnitValue.createPercentValue(100));

            partiesTable.addCell(new Cell().add(new Paragraph("Исполнитель:").setBold()));
            partiesTable.addCell(new Cell().add(new Paragraph("Заказчик:")));
            partiesTable.addCell(new Cell().add(new Paragraph("Метрологическая служба")));
            partiesTable.addCell(new Cell().add(new Paragraph("Клиент ID: " + order.getClientId())));

            document.add(partiesTable);
            document.add(new Paragraph(" "));

            // Раздел: предмет договора (данные заявки)
            document.add(new Paragraph("ПРЕДМЕТ ДОГОВОРА").setFontSize(13).setBold());
            document.add(new Paragraph(" "));

            Table orderTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                    .setWidth(UnitValue.createPercentValue(100));

            addRow(orderTable, "Номер заявки:", order.getOrderNumber());
            addRow(orderTable, "Стоимость услуг:", String.format("%.2f тг", order.getTotalPrice()));
            if (order.getDueDate() != null) {
                addRow(orderTable, "Срок исполнения:", order.getDueDate().format(dateFormatter));
            }
            addRow(orderTable, "Статус оплаты:", "Ожидает оплаты");

            document.add(orderTable);
            document.add(new Paragraph(" "));

            // Раздел: подписи сторон с имитацией ЭЦП
            document.add(new Paragraph("ПОДПИСИ СТОРОН").setFontSize(13).setBold());
            document.add(new Paragraph(" "));

            Table signTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .setWidth(UnitValue.createPercentValue(100));

            // Если договор подписан — показываем дату подписания, иначе "Не подписано"
            String signStatus = contract.isSigned()
                    ? "✓ Подписано ЭЦП\n" + (contract.getSignedAt() != null ? contract.getSignedAt().format(dateTimeFormatter) : "")
                    : "Не подписано";

            signTable.addCell(new Cell().add(new Paragraph("Исполнитель: _______________")));
            signTable.addCell(new Cell().add(new Paragraph("Заказчик: " + signStatus)));

            document.add(signTable);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка генерации договора: " + e.getMessage(), e);
        }

        return baos.toByteArray();
    }

    // Генерирует PDF результата поверки (сертификат, протокол или отчёт)
    // Тип документа определяется полем result_type в таблице results
    // Вызывается из PdfController.downloadCertificate()
    public byte[] generateCertificate(int orderId) {
        // Получаем данные заявки и результата из БД
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) throw new RuntimeException("Order not found: " + orderId);

        Result result = resultRepository.findByOrderId(orderId).orElse(null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

            // Заголовок — название зависит от типа результата (Сертификат/Протокол/Отчёт)
            String docTitle = getDocumentTitle(result);
            document.add(new Paragraph(docTitle)
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("№ " + order.getOrderNumber())
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph(" "));

            // Раздел: основные данные заявки
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                    .setWidth(UnitValue.createPercentValue(100));

            addRow(infoTable, "Номер заказа:", order.getOrderNumber());
            addRow(infoTable, "Статус:", order.getStatus());
            addRow(infoTable, "Стоимость:", String.format("%.2f тг", order.getTotalPrice()));

            if (order.getDueDate() != null) {
                addRow(infoTable, "Срок исполнения:", order.getDueDate().format(dateFormatter));
            }

            document.add(infoTable);
            document.add(new Paragraph(" "));

            // Раздел: данные результата поверки
            if (result != null) {
                document.add(new Paragraph("Результат поверки")
                        .setFontSize(14)
                        .setBold());

                Table resultTable = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                        .setWidth(UnitValue.createPercentValue(100));

                addRow(resultTable, "Тип документа:", getDocumentTitle(result));
                addRow(resultTable, "Подписан:", result.isSigned() ? "Да" : "Нет");

                if (result.getIssuedAt() != null) {
                    addRow(resultTable, "Дата выдачи:", result.getIssuedAt().format(dateTimeFormatter));
                }
                if (result.isSigned() && result.getSignedAt() != null) {
                    addRow(resultTable, "Дата подписания:", result.getSignedAt().format(dateTimeFormatter));
                }

                document.add(resultTable);
            } else {
                // Если результат ещё не добавлен метрологом
                document.add(new Paragraph("Результат поверки ещё не добавлен.")
                        .setItalic()
                        .setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY));
            }

            document.add(new Paragraph(" "));

            // Раздел: подписи сторон
            document.add(new Paragraph("Подписи сторон")
                    .setFontSize(14)
                    .setBold());

            Table signTable = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .setWidth(UnitValue.createPercentValue(100));

            signTable.addCell(new Cell().add(new Paragraph("Метролог: _______________")));
            signTable.addCell(new Cell().add(new Paragraph("Клиент: _______________")));

            document.add(signTable);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка генерации PDF: " + e.getMessage(), e);
        }

        return baos.toByteArray();
    }

    // Вспомогательный метод для добавления строки в таблицу PDF
    // label — жирный заголовок колонки, value — значение
    private void addRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold()));
        table.addCell(new Cell().add(new Paragraph(value != null ? value : "—")));
    }

    // Определяет название документа по типу результата
    // certificate → Сертификат, protocol → Протокол, report → Отчёт
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