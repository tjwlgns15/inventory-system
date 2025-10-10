package com.yhs.inventroysystem.application.task;


import com.yhs.inventroysystem.domain.task.Priority;
import com.yhs.inventroysystem.domain.task.TaskStatus;

import java.time.LocalDate;

public class TaskCommands {
    public record TaskCreateCommand(
            String title,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            TaskStatus status,
            Priority priority
    ) {
    }

    public record TaskUpdateCommand(
            String title,
            String description,
            LocalDate startDate,
            LocalDate endDate,
            TaskStatus status,
            Priority priority
    ) {
    }
}
