package BE.controller;

import BE.model.EmailDetail;
import BE.service.EmailService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SecurityRequirement(name = "api")
@RequestMapping("/api/email")
public class EmailController {

    @Autowired
    EmailService emailService;

    @PostMapping
    public void sendMail(@Valid @RequestBody EmailDetail emailDetail)
    {
        emailService.sendMailTemplate(emailDetail);
    }
}
