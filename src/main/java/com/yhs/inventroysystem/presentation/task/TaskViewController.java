package com.yhs.inventroysystem.presentation.task;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/tasks")
public class TaskViewController {

    @GetMapping
    public String calenderPage() {
        return "task/tasks";
    }

}
