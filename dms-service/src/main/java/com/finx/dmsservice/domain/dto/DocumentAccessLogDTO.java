package com.finx.dmsservice.domain.dto;

import com.finx.dmsservice.domain.enums.AccessType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentAccessLogDTO {
    private Long id;
    private Long documentId;
    private String documentName;
    private Long userId;
    private AccessType accessType;
    private String accessIp;
    private String accessUserAgent;
    private String accessReason;
    private List<Long> sharedWith;
    private LocalDateTime accessedAt;
}
