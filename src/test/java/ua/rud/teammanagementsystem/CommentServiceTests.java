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
import ua.rud.teammanagementsystem.exceptions.BadRequest;
import ua.rud.teammanagementsystem.exceptions.NotFoundException;
import ua.rud.teammanagementsystem.mappers.CommentMapper;
import ua.rud.teammanagementsystem.repositories.CommentRepository;
import ua.rud.teammanagementsystem.repositories.TaskRepository;
import ua.rud.teammanagementsystem.repositories.UserRepository;
import ua.rud.teammanagementsystem.requests.CommentRequest;
import ua.rud.teammanagementsystem.responses.CommentResponse;
import ua.rud.teammanagementsystem.services.CacheService;
import ua.rud.teammanagementsystem.services.CommentService;
import ua.rud.teammanagementsystem.entity.Comment;
import ua.rud.teammanagementsystem.entity.Task;
import ua.rud.teammanagementsystem.entity.User;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTests {

@Mock
    private CommentMapper mapper;
@Mock
    private CommentRepository repository;
@Mock
    private TaskRepository taskRepository;
@Mock
    private UserRepository userRepository;
@Mock
    private CacheService cacheService;
@InjectMocks
    private CommentService service;

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

@Test
    public void getAllCommentsTest(){
    Task task1 = new Task(); task1.setId(1L);
    User user1 = new User(); user1.setId(1L);
    Comment comment1 = new Comment(1L, "text", task1, user1, LocalDate.now());

    Task task2 = new Task(); task2.setId(2L);
    User user2 = new User(); user2.setId(2L);
    Comment comment2 = new Comment(2L, "text", task2, user2, LocalDate.now());

    CommentResponse response1 = new CommentResponse(1L, "text", 1L, 1L,LocalDate.now());
    CommentResponse response2 = new CommentResponse(2L, "text", 2L, 2L,LocalDate.now());

    Pageable pageable = PageRequest.of(0, 10);
    List<Comment> comments = List.of(comment1, comment2);
    Page<Comment> commentsPage = new PageImpl<>(comments, pageable, comments.size());

    when(repository.findAll(pageable)).thenReturn(commentsPage);
    when(mapper.mapTo(comment1)).thenReturn(response1);
    when(mapper.mapTo(comment2)).thenReturn(response2);

    Page<CommentResponse> res = service.getAllComments(pageable);

    assertEquals(2, res.getTotalElements());
    assertEquals(response1, res.getContent().get(0));
    assertEquals(response2, res.getContent().get(1));

    verify(repository).findAll(pageable);
    verify(mapper, times(2)).mapTo(any(Comment.class));
}
@Test
    public void getCommentByIdTest_cache(){
    CommentResponse response = new CommentResponse(1L, "text", 1L, 1L,  LocalDate.now());

    when(cacheService.get("1", CommentResponse.class)).thenReturn(response);

    CommentResponse res = service.getById(1L);
    assertEquals(response, res);
    verify(cacheService, never()).set(anyString(), any(), anyInt());
    verify(mapper, never()).mapTo(any(Comment.class));
    verify(repository, never()).findById(anyLong());
}
@Test
    public void getCommentById_db(){
    Comment comment = new Comment(); comment.setId(1L);
    CommentResponse response = new CommentResponse(1L, "text", 1L, 1L, LocalDate.now());

    when(repository.findById(1L)).thenReturn(Optional.of(comment));
    when(mapper.mapTo(any(Comment.class))).thenReturn(response);

    CommentResponse res = service.getById(1L);

    assertEquals(response, res);
    verify(cacheService).set("1", response, 10);
    verify(repository).findById(1L);
}

@Test
    public void createCommentTest() {
    String testName = "test";
    CommentRequest request = new CommentRequest("text", 1L);
    Task task = new Task();
    task.setId(1L);
    User user = new User();
    user.setId(1L);
    Comment comment = new Comment(null, request.text(), task, null, LocalDate.now());
    Comment saved = new Comment(1L, comment.getText(), task, null, comment.getCreated_at());
    CommentResponse response = new CommentResponse(saved.getId(), saved.getText(), 1L, user.getId(), saved.getCreated_at());

    Authentication authentication = mock(Authentication.class);
    when(authentication.getName()).thenReturn(testName);

    SecurityContext context = mock(SecurityContext.class);
    when(context.getAuthentication()).thenReturn(authentication);

    SecurityContextHolder.setContext(context);

    when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
    when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
    when(repository.save(any(Comment.class))).thenReturn(saved);
    when(mapper.mapTo(saved)).thenReturn(response);

    CommentResponse res = service.createComment(request);

    assertEquals(response, res);
    verify(repository).save(argThat(c ->
            c.getId() == null &&
                    c.getText().equals("text") &&
                    c.getTask().equals(task) &&
                    c.getUser().equals(user) &&
                    c.getCreated_at() !=null
    ));
}
    @Test
    public void deleteCommentTest(){
        Comment comment = new Comment(); comment.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(comment));

        service.deleteComment(1L);


        verify(repository).delete(argThat(c-> c.getId().equals(1L)));
        verify(cacheService).delete("1");
    }

    @Test
    public void getAllCommentTest_emptyPage(){
        Pageable pageable = PageRequest.of(0, 10);
        Page<Comment> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(repository.findAll(pageable)).thenReturn(emptyPage);

        Page<CommentResponse> res = service.getAllComments(pageable);

        assertEquals(0, res.getTotalElements());
        assertEquals(0, res.getContent().size());

        verify(repository).findAll(pageable);
        verify(mapper, times(0)).mapTo(any(Comment.class));
    }

    @Test
    public void getCommentByIdTest_wrongId(){
    when(repository.findById(1L)).thenReturn(Optional.empty());
    NotFoundException e = assertThrows(NotFoundException.class, ()->service.getById(1L));
    assertEquals("Wrong id", e.getMessage());

    verify(mapper, never()).mapTo(any(Comment.class));
    verify(cacheService, never()).set(anyString(), any(), anyInt());
}

@Test
    public void createCommentTest_textIsNull(){
    CommentRequest request = new CommentRequest(null, 1L);

    BadRequest e = assertThrows(BadRequest.class, ()->service.createComment(request));

    assertEquals("You can't create comment without text", e.getMessage());
    verify(repository, never()).save(any(Comment.class));
    verify(mapper, never()).mapTo(any(Comment.class));
}

@Test
    public void deleteCommentTest_wrongId(){
    Comment comment = new Comment(); comment.setId(2L);
    when(repository.findById(1L)).thenReturn(Optional.empty());

    NotFoundException e = assertThrows(NotFoundException.class, ()->service.deleteComment(1L));

    assertEquals("Wrong comment id", e.getMessage());
    verify(repository, never()).delete(any(Comment.class));
    verify(cacheService, never()).delete(anyString());
}

}

