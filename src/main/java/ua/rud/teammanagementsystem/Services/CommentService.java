package ua.rud.teammanagementsystem.Services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ua.rud.teammanagementsystem.Exceptions.BadRequest;
import ua.rud.teammanagementsystem.Exceptions.NotFoundException;
import ua.rud.teammanagementsystem.Mappers.CommentMapper;
import ua.rud.teammanagementsystem.Repositories.CommentRepository;
import ua.rud.teammanagementsystem.Repositories.TaskRepository;
import ua.rud.teammanagementsystem.Repositories.UserRepository;
import ua.rud.teammanagementsystem.Requests.CommentRequest;
import ua.rud.teammanagementsystem.Responses.CommentResponse;
import ua.rud.teammanagementsystem.entity.Comment;

import java.time.LocalDate;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {
private final CommentMapper mapper;
private final CommentRepository repository;
private final TaskRepository taskRepository;
private final UserRepository userRepository;
private final Logger log = LoggerFactory.getLogger(CommentService.class);
private final CacheService cacheService;
    public Page<CommentResponse> getAllComments(Pageable pageable) {
        log.info("All comments got successfully");
        return repository.findAll(pageable).map(mapper::mapTo);
    }

    public CommentResponse getById(Long id) {
        CommentResponse cached = cacheService.get(id.toString(), CommentResponse.class);
        if(cached != null){
            log.info("Comment with id {} got from cache successfully", id);
            return cached;
        }

        CommentResponse response = mapper.mapTo(repository.findById(id).orElseThrow(()->new NotFoundException("Wrong id")));
        cacheService.set(id.toString(), response, 10);
        log.info("Comment with id {} got successfully", id);
        return response;
    }

    public CommentResponse createComment(CommentRequest request) {
        if(request.text() == null){
            throw new BadRequest("You can't create comment without text");
        }
        Comment comment = new Comment(
                null,
                request.text(),
                taskRepository.findById(request.taskId()).orElseThrow(()->new NotFoundException("Wrong task id")),
                userRepository.findById(request.userId()).orElseThrow(()->new NotFoundException("Wrong user id")),
                LocalDate.now()
        );
        repository.save(comment);
        log.info("New comment {} created successfully", request.text());
        return mapper.mapTo(comment);
    }

    public void deleteComment(Long id) {
        Comment commentToDelete = repository.findById(id).orElseThrow(()-> new  NotFoundException("Wrong comment id"));
        repository.delete(commentToDelete);
        cacheService.delete(id.toString());
        log.info("Comment deleted successfully");
    }
}
