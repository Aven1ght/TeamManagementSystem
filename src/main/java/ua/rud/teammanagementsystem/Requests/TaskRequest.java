package ua.rud.teammanagementsystem.Requests;

import ua.rud.teammanagementsystem.Enums.TaskPriority;

public record TaskRequest(
        String title,
        String description,
        TaskPriority priority,
        Long project_id
) {
}
