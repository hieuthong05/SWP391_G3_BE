package BE.model.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MaintenanceChecklistDTO {

    @NotNull(message = "Checklist ID is required")
    private Long checkListId;

    @NotBlank(message = "Status is required")

    // @Pattern(regexp = "^(PASSED|FAILED|RECOMMENDED_REPLACEMENT|NOT_CHECKED)$", message = "Invalid status")
    private String status; // "PASSED", "FAILED", "RECOMMENDED_REPLACEMENT", "NOT_CHECKED"

    private String notes;
}