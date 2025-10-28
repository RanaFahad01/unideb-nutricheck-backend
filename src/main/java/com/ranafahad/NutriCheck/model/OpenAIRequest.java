package com.ranafahad.NutriCheck.model;

import java.util.List;
import java.util.Map;

public class OpenAIRequest {
    private String model;
    private Map<String, String> response_format;
    private List<Map<String, String>> messages;

    public OpenAIRequest(String model, String prompt) {
        this.model = model;
        this.response_format = Map.of("type", "json_object");
        this.messages = List.of(Map.of("role", "user", "content", prompt));
    }

    public String getModel() { return model; }
    public Map<String, String> getResponse_format() { return response_format; }
    public List<Map<String, String>> getMessages() { return messages; }
}