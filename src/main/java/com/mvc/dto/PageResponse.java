package com.mvc.dto;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PageResponse<T> {

    @JsonView(Views.UserSummary.class)
    private final List<T> content;

    @JsonView(Views.UserSummary.class)
    private final int currentPage;

    @JsonView(Views.UserSummary.class)
    private final int pageSize;

    @JsonView(Views.UserSummary.class)
    private final long totalElements;

    @JsonView(Views.UserSummary.class)
    private final int totalPages;

    @JsonView(Views.UserSummary.class)
    private final boolean first;

    @JsonView(Views.UserSummary.class)
    private final boolean last;

    public PageResponse(Page<T> page) {
        this.content = page.getContent();
        this.currentPage = page.getNumber();
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.first = page.isFirst();
        this.last = page.isLast();
    }
}
