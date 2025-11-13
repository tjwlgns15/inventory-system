package com.yhs.inventroysystem.infrastructure.pagenation;

import lombok.NoArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@NoArgsConstructor
public final class PageableUtils {

    private static final String DEFAULT_SORT_DIRECTION = "desc";
    private static final Sort.Direction ASC = Sort.Direction.ASC;
    private static final Sort.Direction DESC = Sort.Direction.DESC;

    /**
     * 정렬 기준과 방향으로 Sort 객체 반환
     */
    public static Sort createSort(String sortBy, String direction) {
        Sort.Direction sortDirection = isDescending(direction) ? DESC : ASC;
        return Sort.by(sortDirection, sortBy);

    }

    /**
     * 페이지 정보와 정렬 기준으로 Pageable 객체 반환
     */
    public static Pageable createPageable(int page, int size, String sortBy, String direction) {
        Sort sort = createSort(sortBy, direction);
        return PageRequest.of(page, size, sort);
    }


    /**
     * 내림차순 정렬 여부 확인
     */
    private static boolean isDescending(String direction) {
        return DEFAULT_SORT_DIRECTION.equalsIgnoreCase(direction);
    }

}
