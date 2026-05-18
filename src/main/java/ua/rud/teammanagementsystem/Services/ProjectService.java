package ua.rud.teammanagementsystem.Services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ua.rud.teammanagementsystem.Exceptions.NotFoundException;
import ua.rud.teammanagementsystem.Mappers.ProjectMapper;
import ua.rud.teammanagementsystem.Repositories.ProjectRepository;
import ua.rud.teammanagementsystem.Requests.ProjectRequest;
import ua.rud.teammanagementsystem.Responses.ProjectResponse;
import ua.rud.teammanagementsystem.entity.Project;


@Service
@Transactional
@RequiredArgsConstructor
public class ProjectService {
private final ProjectMapper mapper;
private final ProjectRepository repository;
Logger log = LoggerFactory.getLogger(ProjectService.class);

    public Page<ProjectResponse> getAllProjects(Pageable pageable) {
        log.info("All project got successfully");
        return repository.findAll(pageable).map(mapper::mapTo);
    }

    public ProjectResponse getById(Long id) {
        Project project = repository.findById(id).orElseThrow(()-> new NotFoundException("Wrong id"));
        log.info("Project with id {} got successfully", id);
        return mapper.mapTo(project);
    }

    public ProjectResponse createProject(ProjectRequest request) {
        Project project = new Project(null, request.name(), request.description());
        repository.save(project);
        log.info("New project {} created successfully", request.name());
        return mapper.mapTo(project);
    }
}
