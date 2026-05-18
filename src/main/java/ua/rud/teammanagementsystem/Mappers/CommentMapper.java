package ua.rud.teammanagementsystem.Mappers;

import org.springframework.stereotype.Component;
import ua.rud.teammanagementsystem.Responses.CommentResponse;
import ua.rud.teammanagementsystem.entity.Comment;
@Component
public class CommentMapper {

    public CommentResponse mapTo(Comment comment){
        return new CommentResponse(
                comment.getId(),
                comment.getText(),
                comment.getTask().getId(),
                comment.getUser().getId(),
                comment.getCreated_at()
        );
    }
}
