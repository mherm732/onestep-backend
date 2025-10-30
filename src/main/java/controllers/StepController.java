package controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
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

import dto.StepDTO;
import model.Step;
import service.StepService;

@RestController 
@RequestMapping("api/goals/steps")
public class StepController {
	
	@Autowired 
	private StepService stepService;
	
	@PreAuthorize("isAuthenticated()")
	@PostMapping("/create/{goalId}")
	public ResponseEntity<?> createStep(@RequestBody Step step, @PathVariable UUID goalId, Authentication authentication){
		try {
			String email = authentication.getName();
			System.out.println("User is creating a step: " + email);
			System.out.println("Incoming step description: " + step.getStepDescription());
			System.out.println("Incoming step isAiGenerated: " + step.getIsAiGenerated());

			// Validate step description
			if (step.getStepDescription() == null || step.getStepDescription().trim().isEmpty()) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body("Step description is required and cannot be empty");
			}

			Step newStep = stepService.createStep(email, goalId, step);

			System.out.println("Step created successfully. ID: " + newStep.getStepId());
			System.out.println("Status: " + newStep.getStatus());
			System.out.println("Description: " + newStep.getStepDescription());

			stepService.updateStepCountForGoal(goalId);

			StepDTO dto = StepDTO.from(newStep);
			System.out.println("DTO created successfully");

			return ResponseEntity.ok(dto);
		} catch (Exception e) {
			System.err.println("Error creating step: " + e.getClass().getName() + ": " + e.getMessage());
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
					"Failed to create step: " + e.getMessage());
		}
	}
	
	@GetMapping("/goal/{goalId}")
	public ResponseEntity<List<StepDTO>> getStepsByGoal(@PathVariable UUID goalId){
		List<Step> steps = stepService.getStepsByGoalId(goalId);
		List<StepDTO> stepDTOs = steps.stream().map(StepDTO::from).toList();
		return ResponseEntity.ok(stepDTOs);
	}
	
	@GetMapping("/{goalId}/current")
	public ResponseEntity<?> getCurrentStep(@PathVariable UUID goalId, Authentication authentication) {
		try {
			String email = authentication.getName();
		    Step current = stepService.getCurrentStepForGoal(goalId, email);
		    return ResponseEntity.ok(StepDTO.from(current));
		} catch (Exception e) {
			System.err.println("Error fetching current step " + e.getClass().getName() + ":" + e.getMessage());
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to fetch current step: " + e.getMessage());
		}
	}

	@GetMapping("/completed")
	public ResponseEntity<List<StepDTO>> getCompletedSteps() {
		List<Step> steps = stepService.getCompletedStepsForUser();
		List<StepDTO> stepDTOs = steps.stream().map(StepDTO::from).toList();
		return ResponseEntity.ok(stepDTOs);
	}
	
	@PutMapping("/update/{stepId}")
	public ResponseEntity<StepDTO> updateStepById(@RequestBody Step step, @PathVariable UUID stepId) {
		Step updatedStep = stepService.updateStep(stepId, step);
		return ResponseEntity.ok(StepDTO.from(updatedStep));
	}
	
	@PutMapping("/update/mark-complete/{stepId}")
	public ResponseEntity<StepDTO> markCompleteByStepId(@PathVariable UUID stepId){
		Step completedStep = stepService.markStepAsCompleted(stepId);
		return ResponseEntity.ok(StepDTO.from(completedStep));
	}
	
	@PutMapping("/skip/{stepId}")
	public ResponseEntity<StepDTO> skipCurrentStep(@PathVariable UUID stepId){
		Step skippedStep = stepService.markStepAsSkipped(stepId);
		return ResponseEntity.ok(StepDTO.from(skippedStep));
	}
	
	@DeleteMapping("/delete/{stepId}")
	public ResponseEntity<String> deleteStepById(@PathVariable("stepId") UUID stepId){
		UUID goalId = stepService.getGoalIdByStepId(stepId);
		stepService.deleteStep(stepId);
		stepService.updateStepCountForGoal(goalId);
		return ResponseEntity.ok("Step deleted.");
	}
	
 }
