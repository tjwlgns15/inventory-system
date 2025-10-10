package com.yhs.inventroysystem.domain.task;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Priority {
    LOW("낮음", 1, "#95a5a6"),
    MEDIUM("보통", 2, "#f39c12"),
    HIGH("높음", 3, "#e67e22"),
    URGENT("긴급", 4, "#e74c3c");

    private final String displayName;
    private final int level;
    private final String colorCode;
}