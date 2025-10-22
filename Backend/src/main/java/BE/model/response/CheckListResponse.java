package BE.model.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CheckListResponse {

    private Long checkListId;
    private String checkListName;
    private String checkListType;
    private String description;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
