package service;

import model.Goal;
import model.Status;
import model.User;
import repository.GoalRepository;
import repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GoalService {

  @Autowired private GoalRepository goalRepository;
  @Autowired private UserRepository userRepository;

  public Goal createGoal(String email, Goal goal) {
    var user = userRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("User not found"));

    var title = goal.getTitle().trim();
    if (goalRepository.findByTitleIgnoreCaseAndUser_Email(title, email).isPresent()) {
      throw new IllegalStateException("Goal with that title already exists");
    }

    var g = new Goal();
    g.setTitle(title);
    g.setGoalDescription(goal.getGoalDescription());
    g.setUser(user);
    g.setGoalStatus(Status.IN_PROGRESS);
    return goalRepository.save(g);
  }

  @Transactional(readOnly = true)
  public Optional<Goal> getGoalByGoalId(UUID goalId) {
    return goalRepository.findById(goalId);
  }

  @Transactional(readOnly = true)
  public List<Goal> getGoalsByUserId(UUID userId){
    return goalRepository.findAllByUser_EmailOrderByDateCreatedDesc(
        userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"))
            .getEmail());
  }

  @Transactional(readOnly = true)
  public List<Goal> getGoalsByEmail(String email) {
    return goalRepository.findAllByUser_EmailOrderByDateCreatedDesc(email);
  }

  public Goal updateGoal(UUID goalId, Goal patch) {
    var g = goalRepository.findById(goalId)
        .orElseThrow(() -> new IllegalArgumentException("Goal not found"));
    g.setTitle(patch.getTitle());
    g.setGoalDescription(patch.getGoalDescription());
    return goalRepository.save(g);
  }

  public Goal markGoalAsCompleted(UUID goalId) {
    var g = goalRepository.findById(goalId)
        .orElseThrow(() -> new IllegalArgumentException("Goal not found"));
    g.setGoalStatus(Status.COMPLETED);
    return goalRepository.save(g);
  }

  public void deleteOwnedGoal(String email, UUID goalId) {
    int rows = goalRepository.deleteByGoalIdAndUser_Email(goalId, email);
    if (rows == 0) throw new IllegalArgumentException("Not found or not owned");
  }

  @Transactional(readOnly = true)
  public List<Goal> getCompletedGoalsForUser(String email) {
    return goalRepository.findAllByUser_EmailAndGoalStatusOrderByDateCreatedDesc(email, Status.COMPLETED);
  }

  @Transactional(readOnly = true)
  public List<Goal> getInProgressGoalsForUser(String email) {
    return goalRepository.findAllByUser_EmailAndGoalStatusOrderByDateCreatedDesc(email, Status.IN_PROGRESS);
  }
}
