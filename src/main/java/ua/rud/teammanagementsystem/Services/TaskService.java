package ua.rud.teammanagementsystem.Services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {
    private final TaskMapper mapper;
    private final TaskRepository repository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public Page<TaskResponse> getAllTasks(Pageable pageable) {
       return repository.findAll(pageable).map(mapper::mapTo);
    }

    public TaskResponse getById(Long id){
        Task task = repository.findById(id).orElseThrow(()-> new NotFoundException("Wrong id"));
        return mapper.mapTo(task);
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
    return mapper.mapTo(task);
    }

    public void deleteTask(Long id) {
        Task task = repository.findById(id).orElseThrow(()-> new NotFoundException("Wrong id"));
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
        return mapper.mapTo(changedTask);
    }
}
