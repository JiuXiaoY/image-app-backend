package com.ai.imageagent.domain.dto;

import com.ai.imageagent.common.PageRequest;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ChatHistory 通用 DTO。
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistoryDto extends PageRequest implements Serializable {
    private Long id;
    private Long appId;
    private Long userId;
    private String message;
    private String messageType; // user/ai/error
    private Long cursor;        // 游标（上一批中最早一条的 id 或 createTime）
    private Integer limit;      // 每次拉取条数，默认10，最大50
    private LocalDateTime lastCreateTime;

    @Serial
    private static final long serialVersionUID = 1L;
}


