package ua.rud.teammanagementsystem.requests;

import ua.rud.teammanagementsystem.enums.TaskPriority;

public record TaskChangeRequest(
        String title,
        String description,
        TaskPriority priority,
        Long project_id,
        Long user_id
) {
}
