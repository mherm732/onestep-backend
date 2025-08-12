package repository;

import model.Goal;
import model.Status;
import model.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoalRepository extends JpaRepository<Goal, UUID> {
	
	Optional<Goal> findById(UUID goalId);
	Optional<Goal> findByTitle(String title);
	Optional<Goal> findByTitleAndUser_UserId(String title, UUID userId);
 	List<Goal> findByUser_UserId(UUID userId);
	List<Goal> findByUser(User user);
	List<Goal> findByGoalStatus(Status goalStatus);

}


