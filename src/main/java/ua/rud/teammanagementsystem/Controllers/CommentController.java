package ua.rud.teammanagementsystem.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import ua.rud.teammanagementsystem.Requests.CommentRequest;
import ua.rud.teammanagementsystem.Responses.CommentResponse;
import ua.rud.teammanagementsystem.Services.CommentService;

@RequiredArgsConstructor
@RequestMapping("/comments")
@RestController
public class CommentController {
private final CommentService service;
    @GetMapping
    public Page<CommentResponse> getAllComments(Pageable pageable){
    return service.getAllComments(pageable);
}
    @GetMapping("/{id}")
    public CommentResponse getById(@PathVariable Long id){
    return service.getById(id);
    }
    @PostMapping
    public CommentResponse createComment(@RequestBody CommentRequest request){
        return service.createComment(request);
    }

}
