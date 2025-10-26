package BE.controller;

import BE.service.DeepSeekService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final DeepSeekService deepSeekService;

    public ChatController(DeepSeekService deepSeekService) {
        this.deepSeekService = deepSeekService;
    }

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String reply = deepSeekService.getChatResponse(request.message());
        return new ChatResponse(reply);
    }

    public record ChatRequest(String message) {}
    public record ChatResponse(String reply) {}
}
