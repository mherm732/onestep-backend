package dto;

import java.time.LocalDateTime;
import java.util.UUID;
import model.Step;

public record StepDTO(
    UUID stepId,
    String stepDescription,
    LocalDateTime dateCreated,
    LocalDateTime dateCompleted,
    LocalDateTime dueDate,
    Boolean isAiGenerated,
    int stepOrder,
    String status
) {
    public static StepDTO from(Step step) {
        return new StepDTO(
            step.getStepId(),
            step.getStepDescription(),
            step.getDateCreated(),
            step.getDateCompleted(),
            step.getDueDate(),
            step.getIsAiGenerated(),
            step.getStepOrder(),
            step.getStatus().name()
        );
    }
}
