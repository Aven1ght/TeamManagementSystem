package ua.rud.teammanagementsystem.responses;

import ua.rud.teammanagementsystem.enums.TaskPriority;
import ua.rud.teammanagementsystem.enums.TaskStatus;

import java.time.LocalDate;

public record TaskResponse(Long id, String tittle, String description, TaskStatus status, TaskPriority priority, LocalDate deadline, Long project_id, Long user_id) {
}
