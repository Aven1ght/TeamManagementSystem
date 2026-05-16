package ua.rud.teammanagementsystem.Controllers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
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

    @GetMapping
    public Page<TaskResponse> getAllTasks(Pageable pageable){
        return service.getAllTasks(pageable);
    }
    @GetMapping("/{id}")
    public TaskResponse getById(@PathVariable Long id){
        return service.getById(id);
    }
    @PostMapping
    public TaskResponse createTask(@RequestBody TaskRequest request){
        return service.createTask(request);
    }
    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id){
        service.deleteTask(id);
    }
    @PutMapping("/{id}")
    public TaskResponse changeTask(@PathVariable Long id, @RequestBody TaskRequest request){
        return service.changeTask(id, request);
    }

}
