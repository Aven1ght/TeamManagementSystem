package ua.rud.teammanagementsystem.requests;

import ua.rud.teammanagementsystem.enums.TaskPriority;

public record TaskRequest(
        String title,
        String description,
        TaskPriority priority,
        Long project_id
) {
}
