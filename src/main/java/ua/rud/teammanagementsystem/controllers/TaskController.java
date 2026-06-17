package ua.rud.teammanagementsystem.controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ua.rud.teammanagementsystem.requests.TaskChangeRequest;
import ua.rud.teammanagementsystem.requests.TaskRequest;
import ua.rud.teammanagementsystem.responses.TaskResponse;
import ua.rud.teammanagementsystem.services.TaskService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/tasks")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {
    private final TaskService service;
    private final Logger log = LoggerFactory.getLogger(TaskController.class);
    @GetMapping
    public Page<TaskResponse> getAllTasks(Pageable pageable){
        log.info("Called get all tasks");
        return service.getAllTasks(pageable);
    }
    @GetMapping("/{id}")
    public TaskResponse getById(@PathVariable Long id){
        log.info("Called get task with id {}", id);
        return service.getById(id);
    }
    @PostMapping
    public TaskResponse createTask(@RequestBody TaskRequest request){
        log.info("Called create new task {}", request.title());
        return service.createTask(request);
    }
    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id){
        log.info("Called delete task with id {}", id);
        service.deleteTask(id);
    }
    @PatchMapping("/{id}")
    public TaskResponse changeTask(@PathVariable Long id, @RequestBody TaskChangeRequest request){
        log.info("Called change task with id {}", id);
        return service.changeTask(id, request);
    }
    @PostMapping("/assign/{id}")
    public TaskResponse assignTask(@PathVariable Long id){
        log.info("Called assign task with id {} to current user", id);
        return service.assignTask(id);
    }

    @PostMapping("/finish/{id}")
    public TaskResponse finishTask(@PathVariable Long id){
        log.info("Called finish task for current user");
        return service.finishTask(id);
    }

}
