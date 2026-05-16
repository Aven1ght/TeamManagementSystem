package ua.rud.teammanagementsystem.entity;

import jakarta.persistence.*;
import lombok.*;
import ua.rud.teammanagementsystem.Enums.TaskPriority;
import ua.rud.teammanagementsystem.Enums.TaskStatus;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tasks")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long id;

    @Column(name = "task_title")
    private String title;

    @Column(name = "task_description")
    private String description;

    @Column(name = "task_status")
    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Column(name = "task_priority")
    @Enumerated(EnumType.STRING)
    private TaskPriority priority;

    @Column(name = "task_deadline")
    private LocalDate deadline;

    @Column(name = "project_id")
    private Long project_id;

    @Column(name = "assigned_user_id")
    private Long user_id;
}