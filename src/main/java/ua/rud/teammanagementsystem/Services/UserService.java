package ua.rud.teammanagementsystem.Services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ua.rud.teammanagementsystem.entity.User;
import ua.rud.teammanagementsystem.Exceptions.NotFoundException;
import ua.rud.teammanagementsystem.Mappers.UserMapper;
import ua.rud.teammanagementsystem.Repositories.UserRepository;
import ua.rud.teammanagementsystem.Responses.UserResponse;
@RequiredArgsConstructor
@Service
public class UserService {
private final UserMapper mapper;
private final UserRepository repository;

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::mapTo);
    }

    public UserResponse getUserById(Long id) {
    return mapper.mapTo(repository.findById(id).orElseThrow(()-> new NotFoundException("Wrong id")));
    }

    @Transactional
    public void deleteUser(Long id) {
       User user =  repository.findById(id).orElseThrow(()-> new NotFoundException("Wrong id"));
       repository.delete(user);
    }
}
