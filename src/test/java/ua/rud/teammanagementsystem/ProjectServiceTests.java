package ua.rud.teammanagementsystem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ua.rud.teammanagementsystem.exceptions.BadRequest;
import ua.rud.teammanagementsystem.exceptions.NotFoundException;
import ua.rud.teammanagementsystem.mappers.ProjectMapper;
import ua.rud.teammanagementsystem.repositories.ProjectRepository;
import ua.rud.teammanagementsystem.requests.ProjectRequest;
import ua.rud.teammanagementsystem.responses.ProjectResponse;
import ua.rud.teammanagementsystem.services.CacheService;
import ua.rud.teammanagementsystem.services.ProjectService;
import ua.rud.teammanagementsystem.entity.Project;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ProjectServiceTests {
    @Mock
    private ProjectMapper mapper;
    @Mock
    private ProjectRepository repository;
    @Mock
    private CacheService cacheService;
    @InjectMocks
    private ProjectService service;

    @Test
    public void getAllProjectsTest(){
        Pageable pageable = PageRequest.of(0, 10);
        Project p1 = new Project();
        p1.setId(1L);
        p1.setName("test1");
        Project p2 = new Project();
        p2.setId(2L);
        p2.setName("test2");

        List<Project> projects = List.of(p1,p2);
        Page<Project> projectPage = new PageImpl<>(projects, pageable, projects.size());

        ProjectResponse response1 = new ProjectResponse(1L, "test1", null);
        ProjectResponse response2 = new ProjectResponse(2L, "test2", null);

        when(repository.findAll(pageable)).thenReturn(projectPage);
        when(mapper.mapTo(p1)).thenReturn(response1);
        when(mapper.mapTo(p2)).thenReturn(response2);

        Page<ProjectResponse> res = service.getAllProjects(pageable);
        assertEquals(2, res.getTotalElements());
        assertEquals(response1, res.getContent().get(0));
        assertEquals(response2, res.getContent().get(1));

        verify(repository).findAll(pageable);
        verify(mapper, times(2)).mapTo(any(Project.class));
    }

    @Test
    public void getByIdTest_cached(){
        ProjectResponse response = new ProjectResponse(1L, "test", "test");
        when(cacheService.get("1", ProjectResponse.class)).thenReturn(response);

        ProjectResponse actual = service.getById(1L);

        assertEquals(response, actual);

        verify(cacheService, never()).set(anyString(), any(), anyInt());
        verify(mapper, never()).mapTo(any(Project.class));
        verify(repository, never()).findById(1L);
    }

    @Test
    public void getByIdTest_db(){
        Project project = new Project();
        project.setId(1L);
        project.setName("test");
        ProjectResponse response = new ProjectResponse(1L, "test", "test");

        when(cacheService.get("1", ProjectResponse.class)).thenReturn(null);
        when(repository.findById(1L)).thenReturn(Optional.of(project));
        when(mapper.mapTo(any(Project.class))).thenReturn(response);

        ProjectResponse actual = service.getById(1L);

        assertEquals(response, actual);

        verify(cacheService).set("1", response, 10);
        verify(repository).findById(1L);
    }

    @Test
    public void createProject(){
        ProjectRequest request = new ProjectRequest("test", "desc");
        Project savedProject = new Project(1L, request.name(), request.description());
        ProjectResponse response = new ProjectResponse(savedProject.getId(), savedProject.getName(), savedProject.getDescription());

        when(repository.save(any(Project.class))).thenReturn(savedProject);
        when(mapper.mapTo(savedProject)).thenReturn(response);

        ProjectResponse actual = service.createProject(request);

        assertEquals(response, actual);

        verify(repository).save(argThat(p->
            p.getId() == null &&
            p.getName().equals("test") &&
            p.getDescription().equals("desc")
        ));
    }
    @Test
    public void deleteProjectTest(){
        Project p = new Project(); p.setId(1L);

        when(repository.findById(1L)).thenReturn(Optional.of(p));

        service.delete(1L);

        verify(repository).delete(p);
        verify(cacheService).delete(anyString());
    }
    @Test
    public void getAllProjects_emptyPage(){
        Pageable pageable = PageRequest.of(0, 10);
        Page<Project> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(repository.findAll(pageable)).thenReturn(emptyPage);

        Page<ProjectResponse> res = service.getAllProjects(pageable);

        assertEquals(0, res.getTotalElements());
        assertEquals(0, res.getContent().size());

        verify(repository).findAll(pageable);
        verify(mapper, never()).mapTo(any(Project.class));
    }
    @Test
    public void getProjectById_wrongId(){

        when(repository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException e = assertThrows(NotFoundException.class, ()->service.getById(1L));

        assertEquals("Wrong id", e.getMessage());

        verify(mapper, never()).mapTo(any(Project.class));
        verify(cacheService, never()).set(anyString(), any(), anyInt());
    }
    @Test
    public void createProjectTest_nameIsNull(){
        ProjectRequest request = new ProjectRequest(null, "desc");
        BadRequest e = assertThrows(BadRequest.class, ()->service.createProject(request));

        assertEquals("You can't create new project without name", e.getMessage());

        verify(repository, never()).save(any(Project.class));
    }
    @Test
    public void deleteProjectTest_wrongId(){
        when(repository.findById(1L)).thenReturn(Optional.empty());
        NotFoundException e = assertThrows(NotFoundException.class, ()->service.delete(1L));
        assertEquals("Wrong project id", e.getMessage());

        verify(repository, never()).delete(any());
        verify(cacheService, never()).delete(anyString());
    }
}
