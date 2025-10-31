/*
package BE.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONArray;
import org.json.JSONObject;

@Service
public class DeepSeekService {

    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    @Value("${OPENROUTER_API_KEY}")
    private String apiKey;

    public String getChatResponse(String message) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("❌ Missing OpenRouter API key! Set OPENROUTER_API_KEY environment variable.");
        }

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        headers.set("HTTP-Referer", "http://localhost:5173");
        headers.set("X-Title", "EV Service Center ChatBot");

        JSONObject body = new JSONObject();
        body.put("model", "deepseek/deepseek-r1:free");

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "user").put("content", message));
        body.put("messages", messages);

        HttpEntity<String> requestEntity = new HttpEntity<>(body.toString(), headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, requestEntity, String.class);

            JSONObject json = new JSONObject(response.getBody());
            return json
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("⚠️ Error connecting to OpenRouter/DeepSeek: " + e.getMessage());
        }
    }
}
*/