package com.example.webhook.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GenerateWebhookResponse {
    // fields depend on actual API - adjust names if needed
    private String webhook;
    @JsonProperty("accessToken")
    private String accessToken;

    public GenerateWebhookResponse() {}
    public String getWebhook() { return webhook; }
    public void setWebhook(String webhook) { this.webhook = webhook; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
}
