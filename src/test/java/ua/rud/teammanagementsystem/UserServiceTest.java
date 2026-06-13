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
import ua.rud.teammanagementsystem.Enums.Role;
import ua.rud.teammanagementsystem.Exceptions.BadRequest;
import ua.rud.teammanagementsystem.Exceptions.NotFoundException;
import ua.rud.teammanagementsystem.Responses.UserResponse;
import ua.rud.teammanagementsystem.Services.CacheService;
import ua.rud.teammanagementsystem.Mappers.UserMapper;
import ua.rud.teammanagementsystem.Repositories.UserRepository;
import ua.rud.teammanagementsystem.Services.CacheService;
import ua.rud.teammanagementsystem.Services.UserService;
import ua.rud.teammanagementsystem.entity.User;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
@Mock
    private UserMapper mapper;
@Mock
    private UserRepository repository;
@Mock
    private CacheService cacheService;
@InjectMocks
    private UserService service;

@Test
    public void getAllUsersTest(){
    User user1 = new User(1L, "test1", "testEmail", Role.USER, "pass");
    User user2 = new User(2L, "test2", "testEmail", Role.USER, "pass");

    UserResponse response1 = new UserResponse(user1.getId(), user1.getUsername(), user1.getPassword());
    UserResponse response2 = new UserResponse(user2.getId(), user2.getUsername(), user2.getPassword());

    Pageable pageable = PageRequest.of(0, 10);
    List<User> users = List.of(user1, user2);
    Page<User> userPage = new PageImpl<>(users, pageable, users.size());

    when(mapper.mapTo(user1)).thenReturn(response1);
    when(mapper.mapTo(user2)).thenReturn(response2);
    when(repository.findAll(pageable)).thenReturn(userPage);

    Page<UserResponse> res = service.getAllUsers(pageable);

    assertEquals(2, res.getTotalElements());
    assertEquals(response1, res.getContent().get(0));
    assertEquals(response2, res.getContent().get(1));

    verify(repository).findAll(pageable);
    verify(mapper, times(2)).mapTo(any(User.class));
}

@Test
    public void getUserByIdTest_cache(){
    UserResponse response = new UserResponse(1L, "test", "testEmail");

    when(cacheService.get("1", UserResponse.class)).thenReturn(response);

    UserResponse res = service.getUserById(1L);

    assertEquals(response, res);
    verify(cacheService, never()).set(anyString(), any(), anyInt());
    verify(mapper, never()).mapTo(any(User.class));
    verify(repository, never()).findById(anyLong());
}

@Test
    public void getUserById_db(){
    User user = new User(1L, "test", "testEmail", Role.USER, "pass");
    UserResponse response = new UserResponse(user.getId(), user.getUsername(), user.getEmail());
    when(repository.findById(1L)).thenReturn(Optional.of(user));
    when(mapper.mapTo(user)).thenReturn(response);

    UserResponse res = service.getUserById(1L);
    assertEquals(response, res);

    verify(cacheService).set("1", response, 10);
    verify(mapper).mapTo(any(User.class));
}

@Test
    public void deleteUserTest(){
    User user = new User(); user.setId(1L);

    when(repository.findById(1L)).thenReturn(Optional.of(user));

    service.deleteUser(1L);

    verify(cacheService).delete("1");
    verify(repository).delete(user);
}

@Test
    public void getAllUsers_emptyPage(){
    Pageable pageable = PageRequest.of(0, 10);
    Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

    when(repository.findAll(pageable)).thenReturn(emptyPage);

    Page<UserResponse> res = service.getAllUsers(pageable);

    assertEquals(0, res.getTotalElements());
    assertEquals(0, res.getContent().size());

    verify(mapper, never()).mapTo(any(User.class));
    verify(repository).findAll(pageable);
}
@Test
    public void getByIdTest_wrongId(){
    when(repository.findById(1L)).thenReturn(Optional.empty());
    NotFoundException e = assertThrows(NotFoundException.class, ()-> service.getUserById(1L));

    assertEquals("Wrong id", e.getMessage());
    verify(cacheService, never()).set(anyString(), any(), anyLong());
}

@Test
    public void deleteUserTest_wrongId(){
    when(repository.findById(2L)).thenReturn(Optional.empty());

    NotFoundException e = assertThrows(NotFoundException.class, ()->service.deleteUser(2L));

    assertEquals("Wrong id", e.getMessage());

    verify(repository, never()).delete(any(User.class));
    verify(cacheService, never()).delete(anyString());

}

}
