package ua.rud.teammanagementsystem.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.rud.teammanagementsystem.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
