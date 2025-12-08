package com.finx.casesourcingservice.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;

/**
 * A Page implementation that can be properly serialized/deserialized by Jackson.
 * Use this class when caching Page results in Redis.
 *
 * @param <T> the type of elements in this page
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestPage<T> extends PageImpl<T> {

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public RestPage(
            @JsonProperty("content") List<T> content,
            @JsonProperty("number") int number,
            @JsonProperty("size") int size,
            @JsonProperty("totalElements") long totalElements,
            @JsonProperty("pageable") JsonPageable pageable,
            @JsonProperty("last") boolean last,
            @JsonProperty("totalPages") int totalPages,
            @JsonProperty("sort") JsonSort sort,
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JsonPageable {
        @JsonProperty("pageNumber")
        private int pageNumber;
        @JsonProperty("pageSize")
        private int pageSize;
        @JsonProperty("offset")
        private long offset;
        @JsonProperty("paged")
        private boolean paged;
        @JsonProperty("unpaged")
        private boolean unpaged;
        @JsonProperty("sort")
        private JsonSort sort;

        public int getPageNumber() { return pageNumber; }
        public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }
        public int getPageSize() { return pageSize; }
        public void setPageSize(int pageSize) { this.pageSize = pageSize; }
        public long getOffset() { return offset; }
        public void setOffset(long offset) { this.offset = offset; }
        public boolean isPaged() { return paged; }
        public void setPaged(boolean paged) { this.paged = paged; }
        public boolean isUnpaged() { return unpaged; }
        public void setUnpaged(boolean unpaged) { this.unpaged = unpaged; }
        public JsonSort getSort() { return sort; }
        public void setSort(JsonSort sort) { this.sort = sort; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JsonSort {
        @JsonProperty("sorted")
        private boolean sorted;
        @JsonProperty("unsorted")
        private boolean unsorted;
        @JsonProperty("empty")
        private boolean empty;

        public boolean isSorted() { return sorted; }
        public void setSorted(boolean sorted) { this.sorted = sorted; }
        public boolean isUnsorted() { return unsorted; }
        public void setUnsorted(boolean unsorted) { this.unsorted = unsorted; }
        public boolean isEmpty() { return empty; }
        public void setEmpty(boolean empty) { this.empty = empty; }
    }
}
