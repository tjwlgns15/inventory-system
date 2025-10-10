package com.yhs.inventroysystem.domain.task;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaskStatus {
    TODO("할 일"),
    IN_PROGRESS("진행 중"),
    COMPLETED("완료");

    private final String displayName;
}