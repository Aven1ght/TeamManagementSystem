package ua.rud.teammanagementsystem.Repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.rud.teammanagementsystem.entity.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
