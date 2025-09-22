package com.ai.imageagent.service;

import com.ai.imageagent.domain.dto.ChatHistoryDto;
import com.ai.imageagent.domain.entity.ChatHistory;
import com.ai.imageagent.domain.entity.User;
import com.ai.imageagent.domain.vo.ChatMessageVo;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatHistoryService extends IService<ChatHistory> {

    Long saveUserMessage(Long appId, String message, User user, Long parentId);

    Long saveAiMessage(Long appId, String message, User user, Long parentId);

    Long saveErrorMessage(Long appId, String errorMsg, User user, Long parentId);

    Page<ChatHistory> pageAdmin(ChatHistoryDto dto);

    List<ChatMessageVo> listLatestByApp(Long appId, int limit);

    List<ChatMessageVo> listBeforeCursor(Long appId, Long cursorId, int limit);

    boolean removeByAppId(Long appId);

    boolean addChatMessage(Long appId, String message, String messageType, Long userId);

    boolean deleteByAppId(Long appId);

    QueryWrapper getQueryWrapper(ChatHistoryDto chatHistoryDto);

    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser);

    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);

    List<ChatHistory> listAllByApp(Long appId);
}


