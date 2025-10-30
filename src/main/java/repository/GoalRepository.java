package repository;

import model.Goal;
import model.Status;
import model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GoalRepository extends JpaRepository<Goal, UUID> {

	Optional<Goal> findById(UUID goalId);

	@Query("SELECT g FROM Goal g JOIN FETCH g.user WHERE g.goalId = :goalId")
	Optional<Goal> findByIdWithUser(@Param("goalId") UUID goalId);

	Optional<Goal> findByTitle(String title);
	Optional<Goal> findByTitleAndUser_UserId(String title, UUID userId);
 	List<Goal> findByUser_UserId(UUID userId);
	List<Goal> findByUser(User user);
	List<Goal> findByGoalStatus(Status goalStatus);
	List<Goal> findAllByUser_EmailOrderByDateCreatedDesc(String email);
	List<Goal> findAllByUser_EmailAndGoalStatusOrderByDateCreatedDesc(String email, Status goalStatus);
	int deleteByGoalIdAndUser_Email(UUID goalId, String email);
	Optional<Goal> findByTitleIgnoreCaseAndUser_UserId(String title, UUID userId);
	Optional<Goal> findByTitleIgnoreCaseAndUser_Email(String title, String email);
}


