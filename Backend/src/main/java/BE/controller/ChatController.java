package BE.controller;

import BE.service.OpenAIService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final OpenAIService openAIService;

    public ChatController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String reply = openAIService.getChatResponse(request.message());
        return new ChatResponse(reply);
    }

    public record ChatRequest(String message) {}
    public record ChatResponse(String reply) {}
}
