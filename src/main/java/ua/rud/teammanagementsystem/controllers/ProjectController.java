package ua.rud.teammanagementsystem.controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ua.rud.teammanagementsystem.requests.ProjectRequest;
import ua.rud.teammanagementsystem.responses.ProjectResponse;
import ua.rud.teammanagementsystem.services.ProjectService;


@RestController
@RequiredArgsConstructor
@RequestMapping("/projects")
@SecurityRequirement(name = "bearerAuth")

public class ProjectController {
    private final ProjectService service;
    private final Logger log = LoggerFactory.getLogger(ProjectController.class);
    @GetMapping
    public Page<ProjectResponse> getAllProjects(Pageable pageable) {
        log.info("Called get all projects");
        return service.getAllProjects(pageable);
    }

    @GetMapping("/{id}")
    public ProjectResponse getProjectById(@PathVariable Long id){
        log.info("Called get project with id {}", id);
        return service.getById(id);
    }

    @PostMapping
    public ProjectResponse createProject(@RequestBody ProjectRequest request){
        log.info("Called create new project {}", request.name());
        return service.createProject(request);
    }
}
