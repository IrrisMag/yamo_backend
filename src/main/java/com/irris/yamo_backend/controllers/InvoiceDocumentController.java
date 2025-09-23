package com.irris.yamo_backend.controllers;

import com.irris.yamo_backend.entities.Invoice;
import com.irris.yamo_backend.repositories.InvoiceRepository;
import com.irris.yamo_backend.services.PdfService;
import com.irris.yamo_backend.services.WhatsAppNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/billing/invoices")
@RequiredArgsConstructor
public class InvoiceDocumentController {

    private final PdfService pdfService;
    private final InvoiceRepository invoiceRepository;
    private final WhatsAppNotificationService waService;

    @Value("${app.baseUrl:http://localhost:8080}")
    private String baseUrl;

    @PostMapping("/{invoiceNumber}/pdf")
    public ResponseEntity<Map<String, Object>> generatePdf(@PathVariable String invoiceNumber) {
        File pdf = pdfService.renderInvoicePdf(invoiceNumber);
        return ResponseEntity.ok(Map.of(
                "invoiceNumber", invoiceNumber,
                "file", pdf.getAbsolutePath(),
                "url", baseUrl + "/api/billing/invoices/pdf/" + invoiceNumber
        ));
    }

    @GetMapping(value = "/pdf/{invoiceNumber}", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<FileSystemResource> getPdf(@PathVariable String invoiceNumber) throws IOException {
        File pdf = pdfService.renderInvoicePdf(invoiceNumber); // regenerate or ensure exists
        FileSystemResource res = new FileSystemResource(pdf);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + pdf.getName())
                .contentLength(res.contentLength())
                .contentType(MediaType.APPLICATION_PDF)
                .body(res);
    }

    @PostMapping("/{invoiceNumber}/send-whatsapp")
    public ResponseEntity<Map<String, Object>> sendInvoiceViaWhatsapp(@PathVariable String invoiceNumber) {
        Invoice inv = invoiceRepository.findByInvoiceNumber(invoiceNumber).orElseThrow();
        File pdf = pdfService.renderInvoicePdf(invoiceNumber);
        String to = inv.getCustomer() != null && inv.getCustomer().getWhatsappPhone() != null && !inv.getCustomer().getWhatsappPhone().isBlank()
                ? inv.getCustomer().getWhatsappPhone()
                : (inv.getCustomer() != null ? inv.getCustomer().getPhone() : null);
        if (to == null || to.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", true, "message", "Customer has no WhatsApp or phone number"));
        }
        // Serve via static resource URL
        String url = baseUrl + "/api/billing/invoices/pdf/" + invoiceNumber;
        Map<String, Object> resp = waService.sendDocumentByUrl(to, url, pdf.getName(), "Votre facture " + invoiceNumber);
        return ResponseEntity.ok(resp);
    }
}
