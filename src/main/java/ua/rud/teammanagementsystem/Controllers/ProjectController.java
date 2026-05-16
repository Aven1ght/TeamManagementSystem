package ua.rud.teammanagementsystem.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ua.rud.teammanagementsystem.Requests.ProjectRequest;
import ua.rud.teammanagementsystem.Responses.ProjectResponse;
import ua.rud.teammanagementsystem.Services.ProjectService;


@RestController
@RequiredArgsConstructor
@RequestMapping("/projects")
public class ProjectController {
    private final ProjectService service;

    @GetMapping
    public Page<ProjectResponse> getAllProjects(Pageable pageable) {
        return service.getAllProjects(pageable);
    }

    @GetMapping("/{id}")
    public ProjectResponse getProjectById(@PathVariable Long id){
        return service.getById(id);
    }

    @PostMapping
    public ProjectResponse createProject(@RequestBody ProjectRequest request){
        return service.createProject(request);
    }
}
