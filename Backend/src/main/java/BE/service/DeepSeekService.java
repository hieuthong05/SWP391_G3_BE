package BE.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.json.JSONObject;

@Service
public class DeepSeekService {

    @Value("${DEEPSEEK_API_KEY:#{null}}")
    private String apiKey;

    private static final String API_URL = "https://api.deepseek.com/chat/completions";

    public String getChatResponse(String message) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException("❌ Missing API key! Please set DEEPSEEK_API_KEY environment variable.");
        }

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // ⚠️ Try model deepseek-chat first
        JSONObject body = new JSONObject();
        body.put("model", "deepseek-chat");
        body.put("messages", new org.json.JSONArray()
                .put(new JSONObject().put("role", "user").put("content", message))
        );

        HttpEntity<String> requestEntity = new HttpEntity<>(body.toString(), headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, requestEntity, String.class);

            System.out.println("✅ DeepSeek API response: " + response.getBody());

            JSONObject responseBody = new JSONObject(response.getBody());
            return responseBody.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

        } catch (HttpClientErrorException e) {
            System.err.println("❌ DeepSeek API returned " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
            throw new RuntimeException("DeepSeek API Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (RestClientException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to connect to DeepSeek API: " + e.getMessage());
        }
    }
}
