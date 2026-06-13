package ua.rud.teammanagementsystem.Mappers;

import org.springframework.stereotype.Component;
import ua.rud.teammanagementsystem.Responses.TaskResponse;
import ua.rud.teammanagementsystem.entity.Task;

@Component
public class TaskMapper {
    public TaskResponse mapTo(Task t){
        return new TaskResponse(
                t.getId(),
                t.getTitle(),
                t.getDescription(),
                t.getStatus(),
                t.getPriority(),
                t.getDeadline(),
                t.getProject().getId(),
                null
        );
    }
}
