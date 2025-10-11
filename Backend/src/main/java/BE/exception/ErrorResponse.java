package BE.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private Map<String, String> fieldErrors;
    private LocalDateTime timestamp;
}
