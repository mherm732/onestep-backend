package repository;

import model.Goal;
import model.Step;
import model.StepStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StepRepository extends JpaRepository<Step, UUID> {
    List<Step> findByGoal_GoalId(UUID goalId);
    Optional<Step> findByStepDescriptionAndGoal_GoalId(String stepDescription, UUID goalId);
	List<Step> findByStatus(StepStatus status);
	int deleteByGoal_GoalId(UUID goalId);
}