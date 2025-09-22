package com.ai.imageagent.domain.dto;

import com.ai.imageagent.common.PageRequest;
import lombok.*;

/**
 * App 通用 DTO（用于增删改查和分页查询条件）。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppDto extends PageRequest {
    private Long id;
    private String appName;
    private String cover;
    private String initPrompt;
    private String codeGenType;
    private String deployKey;
    private Integer priority;
    private Long userId;
}


