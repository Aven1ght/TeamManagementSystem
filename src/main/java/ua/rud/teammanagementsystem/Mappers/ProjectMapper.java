package ua.rud.teammanagementsystem.Mappers;

import org.springframework.stereotype.Component;
import ua.rud.teammanagementsystem.Responses.ProjectResponse;
import ua.rud.teammanagementsystem.entity.Project;

@Component
public class ProjectMapper {
public ProjectResponse mapTo(Project project){
    return new ProjectResponse(project.getId(), project.getName(), project.getDescription());
}

}
