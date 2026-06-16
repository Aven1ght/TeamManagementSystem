package ua.rud.teammanagementsystem.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ua.rud.teammanagementsystem.enums.TaskPriority;
import ua.rud.teammanagementsystem.enums.TaskStatus;
import ua.rud.teammanagementsystem.exceptions.BadRequest;
import ua.rud.teammanagementsystem.exceptions.ConflictRequest;
import ua.rud.teammanagementsystem.exceptions.NotFoundException;
import ua.rud.teammanagementsystem.mappers.TaskMapper;
import ua.rud.teammanagementsystem.repositories.ProjectRepository;
import ua.rud.teammanagementsystem.repositories.TaskRepository;
import ua.rud.teammanagementsystem.repositories.UserRepository;
import ua.rud.teammanagementsystem.requests.TaskChangeRequest;
import ua.rud.teammanagementsystem.requests.TaskRequest;
import ua.rud.teammanagementsystem.responses.TaskResponse;
import ua.rud.teammanagementsystem.entity.Task;
import ua.rud.teammanagementsystem.entity.User;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {
    private final TaskMapper mapper;
    private final TaskRepository repository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final Logger log = LoggerFactory.getLogger(TaskService.class);
    private final CacheService cacheService;

    public Page<TaskResponse> getAllTasks(Pageable pageable) {
        log.info("All tasks got successfully");
       return repository.findAll(pageable).map(mapper::mapTo);
    }

    public TaskResponse getById(Long id){
       TaskResponse cachedResponse = cacheService.get(id.toString(), TaskResponse.class);

       if(cachedResponse != null){
           log.info("Task with id {} got from cache successfully", id);
           return cachedResponse;
       }
        TaskResponse response = mapper.mapTo(repository.findById(id).orElseThrow(()-> new NotFoundException("Wrong id")));
        cacheService.set(id.toString(), response, 10);
        log.info("Task with id {} got successfully", id);
        return response;
    }

    public TaskResponse createTask(TaskRequest request) {
        if(request.title() == null){
            throw new BadRequest("You can't create new task without title");
        }
        if(
                request.priority() != TaskPriority.LOW &&
                request.priority() != TaskPriority.MEDIUM &&
                request.priority() != TaskPriority.HIGH
        ){
            throw new BadRequest("You can't create task with this priority");
        }
    Task task = new Task(
            null,
            request.title(),
            request.description(),
            TaskStatus.CREATED,
            request.priority(),
            LocalDate.now().plusDays(20),
            projectRepository.findById(request.project_id()).orElseThrow(()->new NotFoundException("Wrong project id")),
            null
    );
    repository.save(task);
    log.info("Task {} created successfully", request.title());
    return mapper.mapTo(task);
    }

    public void deleteTask(Long id) {
        Task task = repository.findById(id).orElseThrow(()-> new NotFoundException("Wrong id"));
        cacheService.delete(id.toString());
        log.info("Task with id {} deleted successfully", id);
        repository.delete(task);
    }

    public TaskResponse changeTask(Long id, TaskChangeRequest request) {
        Task task = repository.findById(id).orElseThrow(()->new NotFoundException("Wrong id"));
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority());
        task.setProject(projectRepository.findById(request.project_id()).orElseThrow(()->new NotFoundException("Wrong project id")));
        task.setUser(userRepository.findById(request.user_id()).orElseThrow(()-> new NotFoundException("Wrong user id")));


        cacheService.delete(id.toString());
        log.info("Task with id {} changed successfully", id);
        return mapper.mapTo(task);
    }

    public TaskResponse assignTask(Long id) {
       Task task = repository.findById(id).orElseThrow(()-> new NotFoundException("Wrong task id"));

       if(task.getStatus() != TaskStatus.CREATED){
           throw new ConflictRequest("You can't take this task");
       }
        String username = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getName)
                .orElseThrow(() -> new NotFoundException("User is not authenticated"));

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Wrong username"));

       task.setUser(currentUser);
       task.setStatus(TaskStatus.ACTIVE);
       return mapper.mapTo(task);
    }

    public TaskResponse finishTask(Long id) {
        Task task = repository.findById(id).orElseThrow(()-> new NotFoundException("Wrong task id"));
        if(task.getStatus() != TaskStatus.ACTIVE){
            throw new ConflictRequest("You can't finish this task");
        }
        if(LocalDate.now().isAfter(task.getDeadline())){
            task.setStatus(TaskStatus.OVERDATED);
        }else {
            task.setStatus(TaskStatus.COMPLETED);
        }

        return mapper.mapTo(task);

    }
}
