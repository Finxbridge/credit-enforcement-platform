package com.finx.casesourcingservice.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

/**
 * A Page implementation that can be properly serialized/deserialized by Jackson.
 * Use this class when caching Page results in Redis.
 *
 * This class ignores problematic Spring Data fields (pageable, sort) during serialization
 * and reconstructs them from simple properties (number, size) during deserialization.
 *
 * @param <T> the type of elements in this page
 */
@JsonIgnoreProperties(ignoreUnknown = true, value = {"pageable", "sort"})
public class RestPage<T> extends PageImpl<T> {

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public RestPage(
            @JsonProperty("content") List<T> content,
            @JsonProperty("number") int number,
            @JsonProperty("size") int size,
            @JsonProperty("totalElements") long totalElements,
            @JsonProperty("last") boolean last,
            @JsonProperty("totalPages") int totalPages,
            @JsonProperty("first") boolean first,
            @JsonProperty("numberOfElements") int numberOfElements,
            @JsonProperty("empty") boolean empty) {
        super(content != null ? content : new ArrayList<>(),
              PageRequest.of(number, size > 0 ? size : 1),
              totalElements);
    }

    public RestPage(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public RestPage(List<T> content) {
        super(content);
    }

    public RestPage() {
        super(new ArrayList<>());
    }

    /**
     * Override to prevent serialization of the pageable field.
     * We reconstruct it from number and size during deserialization.
     */
    @Override
    @JsonIgnore
    public Pageable getPageable() {
        return super.getPageable();
    }

    /**
     * Override to prevent serialization of the sort field.
     * Sort is reconstructed as unsorted during deserialization.
     */
    @Override
    @JsonIgnore
    public Sort getSort() {
        return super.getSort();
    }
}
