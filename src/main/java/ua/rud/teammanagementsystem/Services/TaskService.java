package ua.rud.teammanagementsystem.Services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ua.rud.teammanagementsystem.Enums.TaskStatus;
import ua.rud.teammanagementsystem.Exceptions.NotFoundException;
import ua.rud.teammanagementsystem.Mappers.TaskMapper;
import ua.rud.teammanagementsystem.Repositories.ProjectRepository;
import ua.rud.teammanagementsystem.Repositories.TaskRepository;
import ua.rud.teammanagementsystem.Repositories.UserRepository;
import ua.rud.teammanagementsystem.Requests.TaskRequest;
import ua.rud.teammanagementsystem.Responses.TaskResponse;
import ua.rud.teammanagementsystem.entity.Task;

import java.time.Duration;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {
    private final TaskMapper mapper;
    private final TaskRepository repository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final Logger log = LoggerFactory.getLogger(TaskService.class);
    private final RedisTemplate<String, Object> redisTemplate;
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
    Task task = new Task(
            null,
            request.tittle(),
            request.description(),
            TaskStatus.CREATED,
            request.priority(),
            LocalDate.now().plusDays(20),
            projectRepository.findById(request.project_id()).orElseThrow(()->new NotFoundException("Wrong project id")),
            userRepository.findById(request.user_id()).orElseThrow(()->new NotFoundException("Wrong user id"))
    );
    repository.save(task);
    log.info("Task {} created successfully", request.tittle());
    return mapper.mapTo(task);
    }

    public void deleteTask(Long id) {
        Task task = repository.findById(id).orElseThrow(()-> new NotFoundException("Wrong id"));
        cacheService.delete(id.toString());
        log.info("Task with id {} deleted successfully", id);
        repository.delete(task);
    }

    public TaskResponse changeTask(Long id, TaskRequest request) {
        Task task = repository.findById(id).orElseThrow(()->new NotFoundException("Wrong id"));
        Task changedTask = new Task(
                task.getId(),
                request.tittle(),
                request.description(),
                task.getStatus(),
                request.priority(),
                task.getDeadline(),
                projectRepository.findById(request.project_id()).orElseThrow(()->new NotFoundException("Wrong project id")),
                userRepository.findById(request.user_id()).orElseThrow(()->new NotFoundException("Wrong user id"))
        );
        repository.save(changedTask);
        cacheService.delete(id.toString());
        log.info("Task with id {} changed successfully", id);
        return mapper.mapTo(changedTask);
    }
}
