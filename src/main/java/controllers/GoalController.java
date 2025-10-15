package controllers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import dto.GoalDTO;
import model.Goal;
import service.GoalService;
import service.StepService;

@RestController
@RequestMapping("/api/goals")
public class GoalController {
    
    @Autowired 
    private GoalService goalService;

    @Autowired 
    private StepService stepService;

 // controllers/GoalController.java
    @GetMapping("/user")
    public ResponseEntity<List<GoalDTO>> getGoalsForAuthenticatedUser(Authentication auth) {
      if (auth == null || !auth.isAuthenticated()) return ResponseEntity.status(401).build();
      var email = auth.getName();
      var goals = goalService.getGoalsByEmail(email).stream().map(GoalDTO::from).toList();
      return ResponseEntity.ok(goals);
    }

    @GetMapping("/completed")
    public ResponseEntity<List<GoalDTO>> getCompletedGoals(Authentication auth){
      var email = auth.getName();
      var goals = goalService.getCompletedGoalsForUser(email).stream().map(GoalDTO::from).toList();
      return ResponseEntity.ok(goals);
    }

    @GetMapping("/in-progress")
    public ResponseEntity<List<GoalDTO>> getInProgressGoals(Authentication auth) {
      var email = auth.getName();
      var goals = goalService.getInProgressGoalsForUser(email).stream().map(GoalDTO::from).toList();
      return ResponseEntity.ok(goals);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createGoal(@RequestBody Goal goal, Authentication auth){
      try {
        Goal created = goalService.createGoal(auth.getName(), goal);
        return ResponseEntity.status(HttpStatus.CREATED).body(GoalDTO.from(created));
      } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
      } catch (IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
      }
    }

    @DeleteMapping("/{goalId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteGoalById(@PathVariable UUID goalId, Authentication auth) {
      goalService.deleteOwnedGoal(auth.getName(), goalId); // move checks into service
      return ResponseEntity.noContent().build(); // 204
    }

	
}
