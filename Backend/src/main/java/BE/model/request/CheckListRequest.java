package BE.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckListRequest {

    @NotBlank(message = "CheckList name is required")
    private String checkListName;

    @NotBlank(message = "CheckList type is required")
    private String checkListType; // e.g., "Engine", "Brake", "Electrical", "Tire", "Body"

    private String description;
}
