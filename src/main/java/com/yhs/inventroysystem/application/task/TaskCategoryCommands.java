package com.yhs.inventroysystem.application.task;


public class TaskCategoryCommands {
    public record TaskCategoryCreateCommand(
            String name,
            String description,
            String colorCode,
            Integer displayOrder
    ) {
        public TaskCategoryCreateCommand {
            if (displayOrder == null) {
                displayOrder = 0;
            }
        }
    }

    public record TaskCategoryUpdateCommand(
            String name,
            String description,
            String colorCode,
            Integer displayOrder
    ) {}

    public record TaskCategoryDisplayOrderUpdateCommand(
            Long taskCategoryId,
            Integer displayOrder
    ) {}
}
