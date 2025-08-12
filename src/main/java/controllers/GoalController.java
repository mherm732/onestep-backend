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

    @GetMapping("/user")
    public ResponseEntity<List<Goal>> getGoalsForAuthenticatedUser(Authentication authentication) {
       
    	if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        String email = authentication.getName();
        List<Goal> goals = goalService.getGoalsByEmail(email);
        return ResponseEntity.ok(goals);
    }
	
	@PostMapping("/create")
	public ResponseEntity<Goal> createGoal(@RequestBody Goal goal, Authentication authentication){
		String email = authentication.getName();
		System.out.println("Received JSON title: " + goal.getTitle());
		System.out.println("Received JSON description: " + goal.getGoalDescription());
		System.out.println("Received JSON status: " + goal.getGoalStatus());
		return ResponseEntity.ok(goalService.createGoalForEmail(email, goal));
	}
	
	@GetMapping("/user/{userId}")
	public ResponseEntity<List<Goal>> getGoalsByUser(@PathVariable UUID userId){
		return ResponseEntity.ok(goalService.getGoalsByUserId(userId));
	}
	
	@GetMapping("/completed")
	public ResponseEntity<List<Goal>> getCompletedGoals(){
		return ResponseEntity.ok(goalService.getCompletedGoalsForUser());
	}
	
	@GetMapping("/in-progress")
	public ResponseEntity<List<Goal>> getInProgressGoals() {
		return ResponseEntity.ok(goalService.getInProgressGoalsForUser());
	}
	
	@PutMapping("/update/{goalId}")
	public ResponseEntity<Goal> updateGoalById(@RequestBody Goal goal, @PathVariable UUID goalId){
		Goal updatedGoal = goalService.updateGoal(goalId, goal);
		return ResponseEntity.ok(updatedGoal);
	}
	
	@PutMapping("/update/complete/{goalId}")
	public ResponseEntity<Goal> markCompleteByGoalId(@PathVariable UUID goalId){
		Goal completedGoal = goalService.markGoalAsCompleted(goalId);
		return ResponseEntity.ok(completedGoal);
	}
	
	@DeleteMapping("/{goalId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<String> deleteGoalById(@PathVariable("goalId") UUID goalId, Authentication authentication) {
	    String email = authentication.getName();

	    Optional<Goal> optionalGoal = goalService.getGoalByGoalId(goalId);
	    
	    if (optionalGoal.isEmpty()) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Goal not found");
	    }

	    Goal goal = optionalGoal.get();

	    if (!goal.getUser().getEmail().equals(email)) {
	        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not own this goal");
	    }

	    stepService.deleteAllStepsByGoalId(goalId); 
	    goalService.deleteGoal(goalId);
	    return ResponseEntity.ok("Goal deleted.");
	}

	
}
