package ua.rud.teammanagementsystem.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ua.rud.teammanagementsystem.Requests.CommentRequest;
import ua.rud.teammanagementsystem.Responses.CommentResponse;
import ua.rud.teammanagementsystem.Services.CommentService;

@RequiredArgsConstructor
@RequestMapping("/comments")
@RestController
@SecurityRequirement(name = "bearerAuth")
public class CommentController {
private final CommentService service;
private final Logger log = LoggerFactory.getLogger(CommentController.class);
    @GetMapping
    public Page<CommentResponse> getAllComments(Pageable pageable){
    log.info("Called get all comments");
    return service.getAllComments(pageable);
}
    @GetMapping("/{id}")
    public CommentResponse getById(@PathVariable Long id){
    log.info("Called get comment with id {}", id);
    return service.getById(id);
    }
    @PostMapping
    public CommentResponse createComment(@RequestBody CommentRequest request){
        log.info("Called create new comment {}", request.text());
        return service.createComment(request);
    }
    @DeleteMapping("/{id}")
    public void deleteComment(@PathVariable Long id){
        log.info("Called delete comment with id {}", id);
        service.deleteComment(id);
    }

}
