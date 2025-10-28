package com.ranafahad.NutriCheck.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ranafahad.NutriCheck.model.NutritionData;
import com.ranafahad.NutriCheck.model.OpenAIRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class OpenAIService {

    private final WebClient webClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public OpenAIService(@Value("${openai.api.key}") String apiKey) {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public NutritionData extractNutrition(String text) throws Exception {
        String prompt = """
            You are a food information extraction assistant.
            Extract allergens and nutritional values from the following text. Convert all the allergens to english if they are in another language, most likely Hungarian.
            A lot of the data will be in tables and in formats like kj/100g, g/100g or  be in the form of checked boxes, +/-(yes/no) lists, mixed in with highly technical info, etc.... be sure to extract all of it.
            
            Respond ONLY in this JSON structure:
            {
              "allergens": {
                "Gluten": true/false,
                "Egg": true/false,
                "Crustaceans": true/false,
                "Fish": true/false,
                "Peanut": true/false,
                "Soy": true/false,
                "Milk": true/false,
                "Tree nuts": true/false,
                "Celery": true/false,
                "Mustard": true/false
              },
              "nutrition": {
                "Energy": "string",
                "Fat": "string",
                "Carbohydrate": "string",
                "Sugar": "string",
                "Protein": "string",
                "Sodium": "string"
              }
            }

            Text:
            """ + text;

        OpenAIRequest requestBody = new OpenAIRequest("gpt-4o-mini", prompt);

        Mono<String> responseMono = webClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class);

        String response = responseMono.block();

        JsonNode node = mapper.readTree(response)
                .path("choices").get(0).path("message").path("content");

        // Convert the JSON string inside 'content' into a NutritionData object
        String content = node.asText();

        return mapper.readValue(content, NutritionData.class);

    }
}