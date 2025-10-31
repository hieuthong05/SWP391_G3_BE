package BE.model.response;

import lombok.Data;

@Data
public class AIPartResponse {
    private String code;
    private Integer currentMin;
    private Double avgDaily;
    private Integer lookbackDays;
    private Integer forecast;
    private Integer safetyStock;
    private Integer buffer;
    private Integer suggestedMin;
}
