package ua.rud.teammanagementsystem.Services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
private final Logger log = LoggerFactory.getLogger(UserService.class);
private final CacheService cacheService;
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.info("All user got successfully");
        return repository.findAll(pageable).map(mapper::mapTo);
    }

    public UserResponse getUserById(Long id) {
        UserResponse cached = cacheService.get(id.toString(), UserResponse.class);
        if(cached != null){
            log.info("User with id {} got from cache successfully", id);
            return cached;
        }
        UserResponse response = mapper.mapTo(repository.findById(id).orElseThrow(()-> new NotFoundException("Wrong id")));
        cacheService.set(id.toString(), response, 10);
        log.info("User with id {} got successfully", id);
        return response;
    }

    @Transactional
    public void deleteUser(Long id) {
       User user =  repository.findById(id).orElseThrow(()-> new NotFoundException("Wrong id"));
       cacheService.delete(id.toString());
       log.info("User with id {} deleted successfully", id);
       repository.delete(user);
    }
}
