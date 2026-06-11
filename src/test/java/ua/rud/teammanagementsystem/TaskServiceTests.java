package ua.rud.teammanagementsystem;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ua.rud.teammanagementsystem.Enums.TaskPriority;
import ua.rud.teammanagementsystem.Enums.TaskStatus;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TaskServiceTests {
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
public void getByIdTest_Cache(){
        Long taskId = 1L;
    Project p = new Project(); p.setId(1L);
    User u = new User(); u.setId(1L);
    TaskResponse response = new TaskResponse(taskId, "test", "test", TaskStatus.ACTIVE, TaskPriority.LOW, LocalDate.now().plusDays(20), p.getId(), u.getId());

    when(cacheService.get(taskId.toString(), TaskResponse.class)).thenReturn(response);

    TaskResponse actual = service.getById(taskId);

    assertEquals(response, actual);

    verify(repository, never()).findById(anyLong());
    verify(mapper, never()).mapTo(any(Task.class));
    verify(cacheService, never()).set(anyString(), any(), anyInt());
    }

    @Test
    public void getByIdTest_Db(){
        Project p = new Project(); p.setId(1L);
        User u = new User(); u.setId(1L);
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
    public void createTaskTest(){
    TaskRequest request = new TaskRequest("Test", "test", TaskPriority.HIGH, 1L);
    TaskResponse expectedResponse = new TaskResponse(1L, request.tittle(), request.description(), TaskStatus.CREATED, request.priority(), LocalDate.now().plusDays(20), 1L, 1L);
    Project project = new Project();
    User user = new User();
    project.setId(1L);
    user.setId(1L);
    Task task = new Task(null, request.tittle(), request.description(), TaskStatus.CREATED, request.priority(), LocalDate.now().plusDays(20), project, user);
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
        public void deleteTaskTest(){
            Task task = new Task();
            task.setId(1L);
            when(repository.findById(1L)).thenReturn(Optional.of(task));
            service.deleteTask(1L);
            verify(repository).delete(task);
        }

    @Test
    public void changeTaskTest(){
        TaskChangeRequest request = new TaskChangeRequest("new title", "new desc", TaskPriority.LOW, 2L, 2L);
        Project oldProject = new Project(); oldProject.setId(1L);
        User oldUser = new User(); oldUser.setId(1L);
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
        User newUser = new User(); newUser.setId(request.user_id());
        Project newProject = new Project(); newProject.setId(request.project_id());
        Task newTask = new Task(
                task.getId(),
                request.tittle(),
                request.description(),
                task.getStatus(),
                request.priority(),
                task.getDeadline(),
                newProject,
                newUser
                );
    TaskResponse expectedResponse = new TaskResponse(
            task.getId(),
            request.tittle(),
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

        verify(repository).save(argThat(t->
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
    public void assignTaskTest(){
        String testName = "test";
        Project project = new Project(); project.setId(1L);
        User user = new User(); user.setId(1L);
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
public void finishTaskTest(){
        Project p = new Project(); p.setId(1L);
        User u = new User(); u.setId(1L);

        Task task = new Task(1L, "test", "test", TaskStatus.ACTIVE, TaskPriority.LOW, LocalDate.now().plusDays(20), p, u);
        TaskResponse expected = new TaskResponse(task.getId(), task.getTitle(), task.getDescription(), TaskStatus.COMPLETED, task.getPriority(), task.getDeadline(), p.getId(), u.getId());
        when(repository.findById(1L)).thenReturn(Optional.of(task));
        when(mapper.mapTo(any(Task.class))).thenReturn(expected);

        TaskResponse actual = service.finishTask(1L);

        assertEquals(expected, actual);
    }
}

