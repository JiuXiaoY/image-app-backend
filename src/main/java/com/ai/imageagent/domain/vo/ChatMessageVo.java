package com.ai.imageagent.domain.vo;

import com.ai.imageagent.domain.entity.ChatHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageVo {
    private Long id;
    private String message;
    private String messageType;
    private Long appId;
    private Long userId;
    private Long parentId;
    private LocalDateTime createTime;

    public static ChatMessageVo from(ChatHistory ch) {
        return ChatMessageVo.builder()
                .id(ch.getId())
                .message(ch.getMessage())
                .messageType(ch.getMessageType())
                .appId(ch.getAppId())
                .userId(ch.getUserId())
                .parentId(ch.getParentId())
                .createTime(ch.getCreateTime())
                .build();
    }
}


