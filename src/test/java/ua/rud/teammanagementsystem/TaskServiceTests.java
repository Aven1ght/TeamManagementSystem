package ua.rud.teammanagementsystem;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ua.rud.teammanagementsystem.Enums.TaskPriority;
import ua.rud.teammanagementsystem.Enums.TaskStatus;
import ua.rud.teammanagementsystem.Exceptions.BadRequest;
import ua.rud.teammanagementsystem.Exceptions.ConflictRequest;
import ua.rud.teammanagementsystem.Exceptions.NotFoundException;
import ua.rud.teammanagementsystem.Mappers.TaskMapper;
import ua.rud.teammanagementsystem.Repositories.ProjectRepository;
import ua.rud.teammanagementsystem.Repositories.TaskRepository;
import ua.rud.teammanagementsystem.Repositories.UserRepository;
import ua.rud.teammanagementsystem.Requests.TaskChangeRequest;
import ua.rud.teammanagementsystem.Requests.TaskRequest;
import ua.rud.teammanagementsystem.Responses.TaskResponse;
import ua.rud.teammanagementsystem.Services.CacheService;
import ua.rud.teammanagementsystem.Services.TaskService;
import ua.rud.teammanagementsystem.entity.Project;
import ua.rud.teammanagementsystem.entity.Task;
import ua.rud.teammanagementsystem.entity.User;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class
TaskServiceTests {
    @Mock
    private TaskMapper mapper;
    @Mock
    private TaskRepository repository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private CacheService cacheService;
    @InjectMocks
    private TaskService service;

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void getAllTasksTest() {
        Pageable pageable;
        pageable = PageRequest.of(0, 10);

        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Task 1");

        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");

        List<Task> tasks = List.of(task1, task2);
        Page<Task> taskPage = new PageImpl<>(tasks, pageable, tasks.size());

        TaskResponse response1 = new TaskResponse(1L, "Task 1", null, null, null, null, null, null);
        TaskResponse response2 = new TaskResponse(2L, "Task 2", null, null, null, null, null, null);


        when(repository.findAll(pageable)).thenReturn(taskPage);
        when(mapper.mapTo(task1)).thenReturn(response1);
        when(mapper.mapTo(task2)).thenReturn(response2);


        Page<TaskResponse> result = service.getAllTasks(pageable);


        assertEquals(2, result.getTotalElements());
        assertEquals(response1, result.getContent().get(0));
        assertEquals(response2, result.getContent().get(1));

        verify(repository).findAll(pageable);
        verify(mapper, times(2)).mapTo(any(Task.class));
    }

    @Test
    public void getByIdTest_cache() {
        Long taskId = 1L;
        Project p = new Project();
        p.setId(1L);
        User u = new User();
        u.setId(1L);
        TaskResponse response = new TaskResponse(taskId, "test", "test", TaskStatus.ACTIVE, TaskPriority.LOW, LocalDate.now().plusDays(20), p.getId(), u.getId());

        when(cacheService.get(taskId.toString(), TaskResponse.class)).thenReturn(response);

        TaskResponse actual = service.getById(taskId);

        assertEquals(response, actual);

        verify(repository, never()).findById(anyLong());
        verify(mapper, never()).mapTo(any(Task.class));
        verify(cacheService, never()).set(anyString(), any(), anyInt());
    }

    @Test
    public void getByIdTest_db() {
        Project p = new Project();
        p.setId(1L);
        User u = new User();
        u.setId(1L);
        Task task = new Task(1L, "test", "test", TaskStatus.ACTIVE, TaskPriority.LOW, LocalDate.now().plusDays(20), p, u);
        TaskResponse response = new TaskResponse(1L, "test", "test", TaskStatus.ACTIVE, TaskPriority.LOW, LocalDate.now().plusDays(20), p.getId(), u.getId());

        when(cacheService.get("1", TaskResponse.class)).thenReturn(null);
        when(repository.findById(1L)).thenReturn(Optional.of(task));
        when(mapper.mapTo(any(Task.class))).thenReturn(response);

        TaskResponse actual = service.getById(1L);

        assertEquals(response, actual);

        verify(repository).findById(1L);
        verify(cacheService).set("1", response, 10);
    }

    @Test
    public void createTaskTest() {
        TaskRequest request = new TaskRequest("Test", "test", TaskPriority.HIGH, 1L);
        TaskResponse expectedResponse = new TaskResponse(1L, request.title(), request.description(), TaskStatus.CREATED, request.priority(), LocalDate.now().plusDays(20), 1L, 1L);
        Project project = new Project();
        User user = new User();
        project.setId(1L);
        user.setId(1L);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(mapper.mapTo(any())).thenReturn(expectedResponse);
        TaskResponse response = service.createTask(request);

        assertEquals(expectedResponse, response);

        verify(repository).save(argThat(t ->
                        t.getId() == null &&
                                t.getTitle().equals("Test") &&
                                t.getDescription().equals("test") &&
                                t.getPriority().equals(TaskPriority.HIGH) &&
                                t.getUser() == null &&
                                t.getProject().equals(project)
                )
        );
    }

    @Test
    public void deleteTaskTest() {
        Task task = new Task();
        task.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(task));
        service.deleteTask(1L);
        verify(repository).delete(task);
    }

    @Test
    public void changeTaskTest() {
        TaskChangeRequest request = new TaskChangeRequest("new title", "new desc", TaskPriority.LOW, 2L, 2L);
        Project oldProject = new Project();
        oldProject.setId(1L);
        User oldUser = new User();
        oldUser.setId(1L);
        Task task = new Task(
                1L,
                "old title",
                "old desc",
                TaskStatus.CREATED,
                TaskPriority.HIGH,
                LocalDate.now().plusDays(20),
                oldProject,
                oldUser
        );
        User newUser = new User();
        newUser.setId(request.user_id());
        Project newProject = new Project();
        newProject.setId(request.project_id());

        TaskResponse expectedResponse = new TaskResponse(
                task.getId(),
                request.title(),
                request.description(),
                task.getStatus(),
                request.priority(),
                task.getDeadline(),
                newProject.getId(),
                newUser.getId()
        );

        when(repository.findById(1L)).thenReturn(Optional.of(task));
        when(projectRepository.findById(request.project_id())).thenReturn(Optional.of(newProject));
        when(userRepository.findById(request.user_id())).thenReturn(Optional.of(newUser));
        when(mapper.mapTo(any(Task.class))).thenReturn(expectedResponse);

        TaskResponse actual = service.changeTask(1L, request);

        assertEquals(expectedResponse, actual);

        verify(repository).save(argThat(t ->
                t.getId().equals(1L) &&
                        t.getTitle().equals("new title") &&
                        t.getDescription().equals("new desc") &&
                        t.getStatus().equals(TaskStatus.CREATED) &&
                        t.getPriority().equals(TaskPriority.LOW) &&
                        t.getProject().equals(newProject) &&
                        t.getUser().equals(newUser)
        ));

        verify(cacheService).delete("1");
    }

    @Test
    public void assignTaskTest() {
        String testName = "test";
        Project project = new Project();
        project.setId(1L);
        User user = new User();
        user.setId(1L);
        Task task = new Task(
                1L,
                "test",
                "test",
                TaskStatus.CREATED,
                TaskPriority.HIGH,
                LocalDate.now().plusDays(20),
                project,
                null
        );
        TaskResponse expected = new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                TaskStatus.ACTIVE,
                task.getPriority(),
                task.getDeadline(),
                task.getProject().getId(),
                user.getId()
        );

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(testName);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(context);

        when(repository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findByUsername(testName)).thenReturn(Optional.of(user));
        when(mapper.mapTo(any(Task.class))).thenReturn(expected);

        TaskResponse actual = service.assignTask(1L);

        assertEquals(expected, actual);
    }

    @Test
    public void finishTaskTest() {
        Project p = new Project();
        p.setId(1L);
        User u = new User();
        u.setId(1L);

        Task task = new Task(1L, "test", "test", TaskStatus.ACTIVE, TaskPriority.LOW, LocalDate.now().plusDays(20), p, u);
        TaskResponse expected = new TaskResponse(task.getId(), task.getTitle(), task.getDescription(), TaskStatus.COMPLETED, task.getPriority(), task.getDeadline(), p.getId(), u.getId());
        when(repository.findById(1L)).thenReturn(Optional.of(task));
        when(mapper.mapTo(any(Task.class))).thenReturn(expected);

        TaskResponse actual = service.finishTask(1L);

        assertEquals(expected, actual);
    }

    @Test
    public void getAllTasksTest_emptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(repository.findAll(pageable)).thenReturn(emptyPage);

        Page<TaskResponse> result = service.getAllTasks(pageable);

        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getContent().size());

        verify(repository).findAll(pageable);
        verify(mapper, never()).mapTo(any(Task.class));
    }

    @Test
    public void getByIdTest_wrongId() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        NotFoundException e = assertThrows(NotFoundException.class, () -> service.getById(1L));

        assertEquals("Wrong id", e.getMessage());

        verify(mapper, never()).mapTo(any(Task.class));
        verify(cacheService, never()).set(anyString(), any(), anyInt());
    }

    @Test
    public void createTaskTest_requestTitleNull() {
        TaskRequest request = new TaskRequest(null, "test", TaskPriority.LOW, 1L);

        BadRequest exception = assertThrows(BadRequest.class, () -> service.createTask(request));
        assertEquals("You can't create new task without title", exception.getMessage());

        verify(repository, never()).save(any(Task.class));

    }

    @Test
    public void createTaskTest_wrongProjectId() {
        TaskRequest request = new TaskRequest("test", "test", TaskPriority.LOW, 10L);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> service.createTask(request));

        assertEquals("Wrong project id", exception.getMessage());

        verify(repository, never()).save(any(Task.class));
    }

    @Test
    public void createTaskTest_invalidPriority() {
        TaskRequest request = new TaskRequest("test", "test", null, 1L);
        BadRequest e = assertThrows(BadRequest.class, () -> service.createTask(request));

        assertEquals("You can't create task with this priority", e.getMessage());
        verify(repository, never()).save(any(Task.class));

    }

    @Test
    public void deleteTaskTest_wrongId() {
        Task task = new Task();
        task.setId(2L);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> service.deleteTask(1L));
        assertEquals("Wrong id", exception.getMessage());

        verify(cacheService, never()).delete("2");
        verify(repository, never()).delete(any(Task.class));
    }

    @Test
    public void changeTaskTest_wrongTaskId() {
        Task task = new Task();
        task.setId(2L);
        TaskChangeRequest request = new TaskChangeRequest("test", "test", TaskPriority.LOW, 1L, 1L);
        NotFoundException exception = assertThrows(NotFoundException.class, () -> service.changeTask(1L, request));

        assertEquals("Wrong id", exception.getMessage());
        verify(repository, never()).save(any(Task.class));
        verify(cacheService, never()).delete("1");
        verify(mapper, never()).mapTo(any(Task.class));
    }

    @Test
    public void changeTaskTest_wrongProjectId() {
        Task task = new Task();
        task.setId(1L);
        TaskChangeRequest request = new TaskChangeRequest("test", "test", TaskPriority.LOW, 2L, 2L);

        when(repository.findById(1L)).thenReturn(Optional.of(task));
        when(projectRepository.findById(2L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> service.changeTask(1L, request));
        assertEquals("Wrong project id", exception.getMessage());

        verify(repository, never()).save(any(Task.class));
        verify(cacheService, never()).delete("1");
        verify(mapper, never()).mapTo(any(Task.class));
    }

    @Test
    public void changeTaskTest_wrongUserId() {
        Task task = new Task();
        task.setId(1L);
        TaskChangeRequest request = new TaskChangeRequest("test", "test", TaskPriority.LOW, 2L, 2L);
        Project p = new Project();
        p.setId(2L);
        when(repository.findById(1L)).thenReturn(Optional.of(task));
        when(projectRepository.findById(2L)).thenReturn(Optional.of(p));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class, () -> service.changeTask(1L, request));

        assertEquals("Wrong user id", e.getMessage());

        verify(repository, never()).save(any(Task.class));
        verify(cacheService, never()).delete("1");
        verify(mapper, never()).mapTo(any(Task.class));
    }

    @Test
    public void assignTaskTest_wrongTaskId() {
        Task task = new Task();
        task.setId(2L);
        when(repository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class, () -> service.assignTask(1L));

        assertEquals("Wrong task id", e.getMessage());

        verify(mapper, never()).mapTo(any(Task.class));
    }

    @Test
    public void assignTaskTest_statusAlreadyActive() {
        Task task = new Task();
        task.setId(1L);
        task.setStatus(TaskStatus.ACTIVE);
        when(repository.findById(1L)).thenReturn(Optional.of(task));

        ConflictRequest e = assertThrows(ConflictRequest.class, () -> service.assignTask(1L));

        assertEquals("You can't take this task", e.getMessage());

        verify(mapper, never()).mapTo(any(Task.class));
    }

    @Test
    public void assignTaskTest_statusAlreadyCompleted() {
        Task task = new Task();
        task.setId(1L);
        task.setStatus(TaskStatus.COMPLETED);
        when(repository.findById(1L)).thenReturn(Optional.of(task));

        ConflictRequest e = assertThrows(ConflictRequest.class, () -> service.assignTask(1L));

        assertEquals("You can't take this task", e.getMessage());

        verify(mapper, never()).mapTo(any(Task.class));
    }

    @Test
    public void assignTaskTest_wrongUsername() {

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(null);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(context);

        Task task = new Task();
        task.setId(1L);
        task.setStatus(TaskStatus.CREATED);

        when(repository.findById(1L)).thenReturn(Optional.of(task));
        NotFoundException e = assertThrows(NotFoundException.class, () -> service.assignTask(1L));

        assertEquals("Wrong username", e.getMessage());

        verify(mapper, never()).mapTo(any(Task.class));
    }

    @Test
    public void finishTaskTest_wrongId() {
        Task task = new Task();
        task.setId(2L);
        when(repository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class, () -> service.finishTask(1L));

        assertEquals("Wrong task id", e.getMessage());
        verify(mapper, never()).mapTo(any(Task.class));
    }

    @Test
    public void finishTaskTest_statusCreated() {
        Task task = new Task();
        task.setId(1L);
        task.setStatus(TaskStatus.CREATED);
        when(repository.findById(1L)).thenReturn(Optional.of(task));

        ConflictRequest e = assertThrows(ConflictRequest.class, () -> service.finishTask(1L));

        assertEquals("You can't finish this task", e.getMessage());
        verify(mapper, never()).mapTo(any(Task.class));
    }

    @Test
    public void finishTaskTest_statusCompleted() {
        Task task = new Task();
        task.setId(1L);
        task.setStatus(TaskStatus.COMPLETED);
        when(repository.findById(1L)).thenReturn(Optional.of(task));

        ConflictRequest e = assertThrows(ConflictRequest.class, () -> service.finishTask(1L));

        assertEquals("You can't finish this task", e.getMessage());
        verify(mapper, never()).mapTo(any(Task.class));
    }
}