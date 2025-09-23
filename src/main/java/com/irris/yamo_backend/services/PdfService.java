package com.irris.yamo_backend.services;

import com.irris.yamo_backend.entities.Invoice;
import com.irris.yamo_backend.repositories.InvoiceRepository;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PdfService {

    private final InvoiceRepository invoiceRepository;

    @Value("${app.baseUrl:http://localhost:8080}")
    private String baseUrl;

    // Folder where PDFs will be written and statically served by Spring if configured
    @Value("${pdf.output.dir:./src/main/resources/static/invoices}")
    private String pdfOutputDir;

    public File renderInvoicePdf(String invoiceNumber) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber).orElseThrow();
        String html = buildInvoiceHtml(invoice);
        try {
            File dir = new File(pdfOutputDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File out = new File(dir, invoiceNumber + ".pdf");
            try (FileOutputStream os = new FileOutputStream(out)) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(html, baseUrl);
                builder.toStream(os);
                builder.run();
            }
            return out;
        } catch (Exception e) {
            throw new RuntimeException("Failed to render PDF: " + e.getMessage(), e);
        }
    }

    private String buildInvoiceHtml(Invoice inv) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String issue = inv.getIssueDate() == null ? "" : inv.getIssueDate().format(dtf);
        String due = inv.getDueDate() == null ? "" : inv.getDueDate().toString();
        String customerName = inv.getCustomer() == null ? "" : inv.getCustomer().getName() + " " + (inv.getCustomer().getSurname()==null?"":inv.getCustomer().getSurname());
        double total = inv.getTotalAmount() == null ? 0.0 : inv.getTotalAmount();
        double paid = inv.getPaidAmount() == null ? 0.0 : inv.getPaidAmount();
        double balance = inv.getBalanceAmount() == null ? (total - paid) : inv.getBalanceAmount();
        String status = inv.getStatus() == null ? "" : inv.getStatus().name();

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><style>")
          .append("body{font-family:sans-serif;color:#222} .header{display:flex;justify-content:space-between;align-items:center} .title{font-size:20px;font-weight:bold} .section{margin-top:16px} table{width:100%;border-collapse:collapse} td,th{border:1px solid #ddd;padding:8px} .right{text-align:right} .muted{color:#777}")
          .append("</style></head><body>");
        sb.append("<div class='header'><div class='title'>FACTURE ")
          .append(inv.getInvoiceNumber())
          .append("</div><div class='muted'>Statut: ")
          .append(status)
          .append("</div></div>");
        sb.append("<div class='section'><strong>Client:</strong> ")
          .append(customerName)
          .append("<br/><span class='muted'>Date: ")
          .append(issue)
          .append(" | Échéance: ")
          .append(due)
          .append("</span></div>");
        sb.append("<div class='section'><table><thead><tr><th>Description</th><th class='right'>Montant (XAF)</th></tr></thead><tbody>");
        sb.append("<tr><td>Services de pressing</td><td class='right'>").append(String.format("%.0f", total)).append("</td></tr>");
        sb.append("</tbody><tfoot>");
        sb.append("<tr><th class='right'>Payé</th><th class='right'>").append(String.format("%.0f", paid)).append("</th></tr>");
        sb.append("<tr><th class='right'>Reste à payer</th><th class='right'>").append(String.format("%.0f", balance)).append("</th></tr>");
        sb.append("</tfoot></table></div>");
        sb.append("<div class='section muted'>Merci pour votre confiance.</div>");
        sb.append("</body></html>");
        return sb.toString();
    }
}
