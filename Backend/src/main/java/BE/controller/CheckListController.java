package BE.controller;

import BE.model.request.CheckListRequest;
import BE.model.response.CheckListResponse;
import BE.service.CheckListService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checklists")
@RequiredArgsConstructor
public class CheckListController {

    @Autowired
    private CheckListService checkListService;


     // Tạo checklist mới
     // POST /api/checklists

    @SecurityRequirement(name = "api")
    @PostMapping
    public ResponseEntity<Map<String, Object>> createCheckList(
            @Valid @RequestBody CheckListRequest request)
    {
        try {
            CheckListResponse checkList = checkListService.createCheckList(request);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "CheckList created successfully");
            response.put("data", checkList);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }


     // Lấy tất cả checklists
     // GET /api/checklists

    @SecurityRequirement(name = "api")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCheckLists() {
        List<CheckListResponse> checkLists = checkListService.getAllCheckLists();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Get all checklists successfully");
        response.put("totalCheckLists", checkLists.size());
        response.put("data", checkLists);

        return ResponseEntity.ok(response);
    }


//      Lấy active checklists
//      GET /api/checklists/active

    @SecurityRequirement(name = "api")
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveCheckLists() {
        List<CheckListResponse> checkLists = checkListService.getActiveCheckLists();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Get active checklists successfully");
        response.put("totalCheckLists", checkLists.size());
        response.put("data", checkLists);

        return ResponseEntity.ok(response);
    }


//      Lấy checklists theo type
//      GET /api/checklists/type/{type}

    @SecurityRequirement(name = "api")
    @GetMapping("/type/{type}")
    public ResponseEntity<Map<String, Object>> getCheckListsByType(
            @PathVariable String type) {
        List<CheckListResponse> checkLists = checkListService.getCheckListsByType(type);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Get checklists by type successfully");
        response.put("type", type);
        response.put("totalCheckLists", checkLists.size());
        response.put("data", checkLists);

        return ResponseEntity.ok(response);
    }


     // Lấy checklist theo ID
     // GET /api/checklists/{id}

    @SecurityRequirement(name = "api")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCheckListById(@PathVariable Long id) {
        try {
            CheckListResponse checkList = checkListService.getCheckListById(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Get checklist successfully");
            response.put("data", checkList);

            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }


//      Update checklist
//      PUT /api/checklists/{id}

    @SecurityRequirement(name = "api")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCheckList(
            @PathVariable Long id, @Valid @RequestBody CheckListRequest checkListRequest)
    {
        CheckListResponse checkList = checkListService.updateCheckList(id, checkListRequest);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "CheckList updated successfully");
        response.put("data", checkList);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @SecurityRequirement(name = "api")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteCheckList(@PathVariable Long id)
    {
        checkListService.deleteCheckList(id);
        return ResponseEntity.ok(Map.of("message", "Delete CheckList Successfully"));
    }

}
