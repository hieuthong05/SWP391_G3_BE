package BE.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class APIExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleBadRequest(MethodArgumentNotValidException exception){
        String message = "Thong tin ko chinh xac";

        for(FieldError fieldError : exception.getBindingResult().getFieldErrors()){
            message += fieldError.getField()+": "+fieldError.getDefaultMessage()+"\n";
        }

        return ResponseEntity.badRequest().body(message);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public  ResponseEntity<String> handleBadCredenttialsException(BadCredentialsException exception){
        return ResponseEntity.status(401).body("Invalid phone or password");
    }

    @ExceptionHandler(AuthenticationException.class)
    public  ResponseEntity<String> handleAuthenticationException(AuthenticationException exception){
        return ResponseEntity.status(401).body("Authentication failed: "+exception.getMessage());
    }
}
