package ua.rud.teammanagementsystem.Responses;

import ua.rud.teammanagementsystem.Enums.TaskPriority;
import ua.rud.teammanagementsystem.Enums.TaskStatus;

import java.time.LocalDate;

public record TaskResponse(Long id, String tittle, String description, TaskStatus status, TaskPriority priority, LocalDate deadline, Long project_id, Long user_id) {
}
