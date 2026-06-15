package ua.rud.teammanagementsystem.mappers;

import org.springframework.stereotype.Component;
import ua.rud.teammanagementsystem.responses.ProjectResponse;
import ua.rud.teammanagementsystem.entity.Project;

@Component
public class ProjectMapper {
public ProjectResponse mapTo(Project project){
    return new ProjectResponse(project.getId(), project.getName(), project.getDescription());
}

}
