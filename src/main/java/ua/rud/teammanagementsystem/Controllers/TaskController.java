package ua.rud.teammanagementsystem.Controllers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ua.rud.teammanagementsystem.Requests.TaskRequest;
import ua.rud.teammanagementsystem.Responses.TaskResponse;
import ua.rud.teammanagementsystem.Services.TaskService;

@RequiredArgsConstructor
@RestController
@RequestMapping("/tasks")
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
        log.info("Called create new task {}", request.tittle());
        return service.createTask(request);
    }
    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id){
        log.info("Called delete task with id {}", id);
        service.deleteTask(id);
    }
    @PutMapping("/{id}")
    public TaskResponse changeTask(@PathVariable Long id, @RequestBody TaskRequest request){
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
