package BE.model.response;

import lombok.Data;

@Data
public class AIPartResponse {
    private String code;
    private int currentQuantity;
    private Integer currentMin;
    private Double avgDaily;
    private Integer lookbackDays;
    private Integer forecast;
    private Integer safetyStock;
    private Integer buffer;
    private Integer suggestedMin;
    private Integer suggestedMinBasedOnDays;

    public int getCurrentQuantity() {
        return currentQuantity;
    }

    public void setCurrentQuantity(int currentQuantity) {
        this.currentQuantity = currentQuantity;
    }

    public Integer getSuggestedMinBasedOnDays() {
        return suggestedMinBasedOnDays;
    }

    public void setSuggestedMinBasedOnDays(Integer suggestedMinBasedOnDays) {
        this.suggestedMinBasedOnDays = suggestedMinBasedOnDays;
    }
}

