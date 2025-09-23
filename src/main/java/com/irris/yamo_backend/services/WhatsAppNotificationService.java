package com.irris.yamo_backend.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class WhatsAppNotificationService {
    private static final Logger log = LoggerFactory.getLogger(WhatsAppNotificationService.class);

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${whatsapp.phone-number-id:}")
    private String phoneNumberId;

    @Value("${whatsapp.token:}")
    private String token;

    private String endpointUrl() {
        if (phoneNumberId == null || phoneNumberId.isBlank()) {
            throw new IllegalStateException("Missing whatsapp.phone-number-id property");
        }
        return "https://graph.facebook.com/v20.0/" + phoneNumberId + "/messages";
    }

    private HttpHeaders authHeaders() {
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Missing whatsapp.token property");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public Map<String, Object> sendTextMessage(String toE164, String text) {
        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("to", toE164);
        body.put("type", "text");
        body.put("text", Map.of("body", text));
        return post(body);
    }

    public Map<String, Object> sendTemplateMessage(String toE164, String templateName, String languageCode, List<String> bodyParams) {
        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("to", toE164);
        body.put("type", "template");

        Map<String, Object> template = new HashMap<>();
        template.put("name", templateName);
        template.put("language", Map.of("code", languageCode));

        if (bodyParams != null && !bodyParams.isEmpty()) {
            List<Map<String, Object>> components = new ArrayList<>();
            List<Map<String, Object>> parameters = new ArrayList<>();
            for (String p : bodyParams) {
                parameters.add(Map.of("type", "text", "text", p));
            }
            components.add(Map.of("type", "body", "parameters", parameters));
            template.put("components", components);
        }

        body.put("template", template);
        return post(body);
    }

    public Map<String, Object> sendImageByUrl(String toE164, String imageUrl, String caption) {
        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("to", toE164);
        body.put("type", "image");
        Map<String, Object> image = new HashMap<>();
        image.put("link", imageUrl);
        if (caption != null && !caption.isBlank()) {
            image.put("caption", caption);
        }
        body.put("image", image);
        return post(body);
    }

    public Map<String, Object> sendDocumentByUrl(String toE164, String docUrl, String filename, String caption) {
        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("to", toE164);
        body.put("type", "document");
        Map<String, Object> document = new HashMap<>();
        document.put("link", docUrl);
        if (filename != null && !filename.isBlank()) {
            document.put("filename", filename);
        }
        if (caption != null && !caption.isBlank()) {
            document.put("caption", caption);
        }
        body.put("document", document);
        return post(body);
    }

    private Map<String, Object> post(Map<String, Object> body) {
        String url = endpointUrl();
        HttpHeaders headers = authHeaders();
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);
        try {
            Map<?, ?> resp = restTemplate.postForObject(url, req, Map.class);
            @SuppressWarnings("unchecked")
            Map<String, Object> cast = (Map<String, Object>) (resp == null ? Collections.emptyMap() : resp);
            return cast;
        } catch (Exception ex) {
            log.error("WhatsApp API call failed: {}", ex.getMessage(), ex);
            Map<String, Object> error = new HashMap<>();
            error.put("error", true);
            error.put("message", ex.getMessage());
            return error;
        }
    }
}
