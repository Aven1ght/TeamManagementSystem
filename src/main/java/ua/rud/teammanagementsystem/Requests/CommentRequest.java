package ua.rud.teammanagementsystem.Requests;

public record CommentRequest(String text, Long taskId, Long userId) {
}
