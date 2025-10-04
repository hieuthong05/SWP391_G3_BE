package BE.controller;

import BE.service.ReminderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    @Autowired
    private ReminderService reminderService;

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getReminders(@PathVariable Long customerId)
    {
        Map<String, List<String>> reminders = reminderService.getRemindersForCustomer(customerId);
        return ResponseEntity.ok(reminders);
    }
}
