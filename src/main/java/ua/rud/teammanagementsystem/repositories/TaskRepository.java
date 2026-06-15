package ua.rud.teammanagementsystem.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.rud.teammanagementsystem.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
