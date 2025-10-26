package BE.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletion;

@Service
public class OpenAIService {

    private final OpenAIClient client;

    public OpenAIService(@Value("${OPENAI_API_KEY}") String apiKey) {
        this.client = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }

    public String getChatResponse(String userMessage) {
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model("gpt-4o-mini")
                .addUserMessage(userMessage)
                .build();

        ChatCompletion response = client.chat().completions().create(params);
        return response.choices().get(0).message().content().orElse("");
    }
}
