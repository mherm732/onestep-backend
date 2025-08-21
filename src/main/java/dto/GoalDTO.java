package dto;

import java.util.UUID;
import model.Goal;

public record GoalDTO(UUID goalId, String title, String goalDescription, String goalStatus) {
	 public static GoalDTO from(Goal g) {
	    return new GoalDTO(g.getGoalId(), g.getTitle(), g.getGoalDescription(), g.getGoalStatus().name());
	  }
}