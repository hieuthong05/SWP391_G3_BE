package BE.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChecklistItemStatusResponse {
    private Long checkListId;
    private String checkListName;
    private String checkListType;
    private String status;//  "PASSED", "FAILED", "NOT_CHECKED"
    private String notes;
}