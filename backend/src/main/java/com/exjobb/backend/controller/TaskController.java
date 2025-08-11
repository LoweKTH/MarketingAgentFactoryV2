package com.exjobb.backend.controller;

import com.exjobb.backend.dto.ScheduledTaskDTO;
import com.exjobb.backend.dto.UpdateTaskDTO;
import com.exjobb.backend.entity.User;
import com.exjobb.backend.service.user.TaskService;
import com.exjobb.backend.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService scheduledTaskService;

    private final UserService userService;

    public TaskController(TaskService scheduledTaskService, UserService userService) {
        this.scheduledTaskService = scheduledTaskService;
        this.userService = userService;
    }

    /**
     * GET /api/tasks : Get all scheduled tasks for the currently authenticated
     * user.
     * The user is identified from the Spring Security context.
     *
     * @param user The authenticated user principal.
     * @return the ResponseEntity with status 200 (OK) and the list of scheduled
     *         tasks.
     */
    @GetMapping
    public ResponseEntity<List<ScheduledTaskDTO>> getMyTasks(Authentication authentication) {
        String username = authentication.getName();
        System.out.println("Username: " + username);
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        List<ScheduledTaskDTO> tasks = scheduledTaskService.getTasksForUser(currentUser.getId());
        return ResponseEntity.ok(tasks);
    }

    /**
     * PUT /api/tasks/{taskId}/toggle : Toggles the active status of a task.
     *
     * @param taskId         The ID of the task to toggle.
     * @param authentication The authenticated user principal.
     * @return the ResponseEntity with status 200 (OK) and the updated task DTO.
     */
    @PutMapping("/{taskId}/toggle")
    public ResponseEntity<ScheduledTaskDTO> toggleTask(@PathVariable Long taskId, Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        ScheduledTaskDTO updatedTask = scheduledTaskService.toggleTaskStatus(taskId, currentUser.getId());
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId, Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        scheduledTaskService.deleteTask(taskId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /api/tasks/{taskId} : Updates a scheduled task's prompt and cron
     * expression.
     *
     * @param taskId         The ID of the task to update.
     * @param updateTaskDTO  The DTO containing the new prompt and cron expression.
     * @param authentication The authenticated user principal.
     * @return the ResponseEntity with status 200 (OK) and the updated task DTO.
     */
    @PutMapping("/{taskId}")
    public ResponseEntity<ScheduledTaskDTO> updateTask(@PathVariable Long taskId,
                                                       @Valid @RequestBody UpdateTaskDTO updateTaskDTO, Authentication authentication) {
        String username = authentication.getName();
        User currentUser = userService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        ScheduledTaskDTO updatedTask = scheduledTaskService.updateTask(taskId, updateTaskDTO, currentUser.getId());
        return ResponseEntity.ok(updatedTask);
    }
}
