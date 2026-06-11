package ua.rud.teammanagementsystem.Requests;

import ua.rud.teammanagementsystem.Enums.TaskPriority;

public record TaskChangeRequest(
        String tittle,
        String description,
        TaskPriority priority,
        Long project_id,
        Long user_id
) {
}
