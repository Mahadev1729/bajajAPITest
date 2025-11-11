package com.example.webhook.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.webhook.model.GenerateWebhookRequest;
import com.example.webhook.model.GenerateWebhookResponse;
import com.example.webhook.model.FinalQueryRequest;
import com.example.webhook.entity.SolutionRecord;
import com.example.webhook.repository.SolutionRecordRepository;

@Service
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final SolutionRecordRepository repo;

    // Endpoint from assignment
    private static final String GENERATE_URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

    public WebhookService(SolutionRecordRepository repo) {
        this.repo = repo;
    }

    /**
     * Main execution flow that runs automatically on application startup.
     */
    public void executeFlow() {
        try {
            // 1️⃣ Send POST to generateWebhook endpoint
            String name = "Your Name";        // ⬅️ replace with your real name
            String regNo = "REG12345";        // ⬅️ replace with your registration number
            String email = "you@example.com"; // ⬅️ replace with your email ID

            GenerateWebhookRequest requestBody = new GenerateWebhookRequest(name, regNo, email);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<GenerateWebhookRequest> requestEntity = new HttpEntity<>(requestBody, headers);

            log.info("🔹 Sending request to generate webhook...");
            ResponseEntity<GenerateWebhookResponse> response = restTemplate.exchange(
                    GENERATE_URL,
                    HttpMethod.POST,
                    requestEntity,
                    GenerateWebhookResponse.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("❌ Failed to generate webhook. Status: {}", response.getStatusCode());
                return;
            }

            GenerateWebhookResponse responseBody = response.getBody();
            String webhookUrl = responseBody.getWebhook();
            String accessToken = responseBody.getAccessToken();

            log.info("✅ Webhook generated successfully!");
            log.info("➡️ Webhook URL: {}", webhookUrl);
            log.info("➡️ Access Token: {}", accessToken);

            // 2️⃣ Determine assigned question based on regNo last two digits
            int lastTwoDigits = extractLastTwoDigits(regNo);
            boolean isOdd = (lastTwoDigits % 2) != 0;
            int questionNumber = isOdd ? 1 : 2;

            log.info("📘 Last two digits: {} → Assigned Question {}", lastTwoDigits, questionNumber);

            // 3️⃣ Generate final SQL query based on question
            String finalQuery = solveSqlForQuestion(questionNumber);
            log.info("🧠 Final SQL generated:\n{}", finalQuery);

            // 4️⃣ Save locally into H2 database
            SolutionRecord record = new SolutionRecord(regNo, finalQuery);
            repo.save(record);
            log.info("💾 Saved SQL locally into H2 DB with record ID: {}", record.getId());

            // 5️⃣ Send finalQuery to webhook URL with Authorization header
            FinalQueryRequest finalRequest = new FinalQueryRequest(finalQuery);

            HttpHeaders webhookHeaders = new HttpHeaders();
            webhookHeaders.setContentType(MediaType.APPLICATION_JSON);
            webhookHeaders.set("Authorization", accessToken); // As per assignment (no Bearer prefix)

            HttpEntity<FinalQueryRequest> webhookEntity = new HttpEntity<>(finalRequest, webhookHeaders);

            log.info("🚀 Sending finalQuery to webhook...");
            ResponseEntity<String> webhookResponse = restTemplate.exchange(
                    webhookUrl,
                    HttpMethod.POST,
                    webhookEntity,
                    String.class
            );

            if (webhookResponse.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Webhook POST successful! Status: {}", webhookResponse.getStatusCode());
            } else {
                log.warn("⚠️ Webhook POST returned non-success status: {}", webhookResponse.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ Error during webhook flow execution", e);
        }
    }

    /**
     * Extract last two digits from registration number.
     */
    private int extractLastTwoDigits(String regNo) {
        String digits = regNo.replaceAll("\\D+", "");
        if (digits.isEmpty()) return 0;
        if (digits.length() == 1) return Integer.parseInt(digits);
        return Integer.parseInt(digits.substring(digits.length() - 2));
    }

    /**
     * Return final SQL query string based on question number.
     */
    private String solveSqlForQuestion(int questionNumber) {
        if (questionNumber == 1) {
            // 🔹 Question 1: Highest salary not credited on 1st of any month
            return """
                SELECT 
                    p.AMOUNT AS SALARY,
                    CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME,
                    TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE,
                    d.DEPARTMENT_NAME
                FROM PAYMENTS p
                JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID
                JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID
                WHERE DAY(p.PAYMENT_TIME) <> 1
                  AND p.AMOUNT = (
                      SELECT MAX(AMOUNT)
                      FROM PAYMENTS
                      WHERE DAY(PAYMENT_TIME) <> 1
                  );
                """;
        } else {
            // 🔹 Question 2: Younger employees count per department
            return """
                SELECT 
                    e1.EMP_ID,
                    e1.FIRST_NAME,
                    e1.LAST_NAME,
                    d.DEPARTMENT_NAME,
                    COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT
                FROM EMPLOYEE e1
                JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID
                LEFT JOIN EMPLOYEE e2 
                       ON e1.DEPARTMENT = e2.DEPARTMENT
                      AND e2.DOB > e1.DOB
                GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME
                ORDER BY e1.EMP_ID DESC;
                """;
        }
    }
}
