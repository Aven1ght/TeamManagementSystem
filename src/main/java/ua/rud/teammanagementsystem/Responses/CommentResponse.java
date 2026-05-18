package ua.rud.teammanagementsystem.Responses;

import java.time.LocalDate;

public record CommentResponse(Long id, String text, Long projectId, Long userId, LocalDate createdAt) {
}
