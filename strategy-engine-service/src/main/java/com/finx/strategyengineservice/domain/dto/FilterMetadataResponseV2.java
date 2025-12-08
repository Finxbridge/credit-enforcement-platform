package com.finx.strategyengineservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response matching your sample structure for dynamic filter metadata
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilterMetadataResponseV2 {

    private String code;
    private String status;
    private String msg;
    private FilterData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FilterData {
        private List<Attribute> attributes;      // Text filters with options
        private List<FieldInfo> datefields;      // Date filters
        private List<FieldInfo> numberfields;    // Numeric filters
        private List<OperatorInfo> numericOperators;
        private List<OperatorInfo> dateOperators;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Attribute {
        private String key;
        private String type;
        private List<Option> options;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Option {
        private String _id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldInfo {
        private String key;
        private String label;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperatorInfo {
        private String code;
        private String symbol;
        private String displayName;
        private String description;
    }
}
