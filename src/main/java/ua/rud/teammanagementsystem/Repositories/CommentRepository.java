package ua.rud.teammanagementsystem.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.rud.teammanagementsystem.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
