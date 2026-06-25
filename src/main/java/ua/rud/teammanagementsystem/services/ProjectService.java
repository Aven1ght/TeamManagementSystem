package ua.rud.teammanagementsystem.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ua.rud.teammanagementsystem.exceptions.BadRequest;
import ua.rud.teammanagementsystem.exceptions.NotFoundException;
import ua.rud.teammanagementsystem.mappers.ProjectMapper;
import ua.rud.teammanagementsystem.repositories.ProjectRepository;
import ua.rud.teammanagementsystem.requests.ProjectRequest;
import ua.rud.teammanagementsystem.responses.ProjectResponse;
import ua.rud.teammanagementsystem.entity.Project;



@Service
@Transactional
@RequiredArgsConstructor
public class ProjectService {
private final ProjectMapper mapper;
private final ProjectRepository repository;
private final Logger log = LoggerFactory.getLogger(ProjectService.class);
private final CacheService cacheService;
    public Page<ProjectResponse> getAllProjects(Pageable pageable) {
        log.info("All project got successfully");
        return repository.findAll(pageable).map(mapper::mapTo);
    }

    public ProjectResponse getById(Long id) {
        ProjectResponse cached = cacheService.get(id.toString(), ProjectResponse.class);
        if(cached != null){
            log.info("Project with id {} got from cache successfully", id);
            return cached;
        }

        ProjectResponse response = mapper.mapTo(repository.findById(id).orElseThrow(()-> new NotFoundException("Wrong id")));
        cacheService.set(id.toString(), response, 10);
        log.info("Project with id {} got successfully", id);
        return response;
    }

    public ProjectResponse createProject(ProjectRequest request) {
        if(request.name() == null){
            throw new BadRequest("You can't create new project without name");
        }
        Project project = new Project(null, request.name(), request.description());
        Project savedProject = repository.save(project);
        log.info("New project {} created successfully", request.name());
        return mapper.mapTo(savedProject);
    }

    public void delete(Long id) {
        Project projectToDelete = repository.findById(id).orElseThrow(()-> new NotFoundException("Wrong project id"));
        repository.delete(projectToDelete);
        cacheService.delete(id.toString());
        log.info("Project with id {} deleted successfully", id);
    }
}
