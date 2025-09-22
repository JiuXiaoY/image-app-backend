package com.ai.imageagent.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.ai.imageagent.common.ErrorCode;
import com.ai.imageagent.constant.UserConstant;
import com.ai.imageagent.domain.dto.ChatHistoryDto;
import com.ai.imageagent.domain.entity.App;
import com.ai.imageagent.domain.entity.ChatHistory;
import com.ai.imageagent.domain.entity.User;
import com.ai.imageagent.domain.enums.MessageTypeEnum;
import com.ai.imageagent.domain.vo.ChatMessageVo;
import com.ai.imageagent.exception.BusinessException;
import com.ai.imageagent.exception.ThrowUtils;
import com.ai.imageagent.mapper.ChatHistoryMapper;
import com.ai.imageagent.service.AppService;
import com.ai.imageagent.service.ChatHistoryService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Resource
    @Lazy
    private AppService appService;

    @Override
    public Long saveUserMessage(Long appId, String message, User user, Long parentId) {
        checkParams(appId, message, user);
        ChatHistory ch = ChatHistory.builder()
                .appId(appId)
                .message(message)
                .messageType(MessageTypeEnum.USER.getValue())
                .userId(user.getId())
                .parentId(parentId)
                .build();
        boolean saved = this.save(ch);
        if (!saved) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "保存用户消息失败");
        }
        return ch.getId();
    }

    @Override
    public Long saveAiMessage(Long appId, String message, User user, Long parentId) {
        checkParams(appId, message, user);
        ChatHistory ch = ChatHistory.builder()
                .appId(appId)
                .message(message)
                .messageType(MessageTypeEnum.AI.getValue())
                .userId(user.getId())
                .parentId(parentId)
                .build();
        boolean saved = this.save(ch);
        if (!saved) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "保存AI消息失败");
        }
        return ch.getId();
    }

    @Override
    public Long saveErrorMessage(Long appId, String errorMsg, User user, Long parentId) {
        if (ObjectUtil.isNull(appId) || appId <= 0 || ObjectUtil.isNull(user)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String msg = StrUtil.blankToDefault(errorMsg, "");
        ChatHistory ch = ChatHistory.builder()
                .appId(appId)
                .message(msg)
                .messageType(MessageTypeEnum.AI.getValue())
                .userId(user.getId())
                .parentId(parentId)
                .build();
        boolean saved = this.save(ch);
        if (!saved) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "保存错误消息失败");
        }
        return ch.getId();
    }

    @Override
    public Page<ChatHistory> pageAdmin(ChatHistoryDto dto) {
        int pageNumber = dto.getPageNum();
        int pageSize = dto.getPageSize();
        QueryWrapper qw = QueryWrapper.create().from(ChatHistory.class);
        if (ObjectUtil.isNotNull(dto.getAppId())) {
            qw.where("appId = ?", dto.getAppId());
        }
        if (ObjectUtil.isNotNull(dto.getUserId())) {
            qw.and("userId = ?", dto.getUserId());
        }
        if (StrUtil.isNotBlank(dto.getMessageType())) {
            qw.and("messageType = ?", dto.getMessageType());
        }
        qw.orderBy("createTime desc");
        return this.page(new Page<>(pageNumber, pageSize), qw);
    }

    @Override
    public List<ChatMessageVo> listLatestByApp(Long appId, int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        QueryWrapper qw = QueryWrapper.create()
                .from(ChatHistory.class)
                .where("appId = ?", appId)
                .orderBy("createTime desc")
                .limit(size);
        List<ChatHistory> list = this.list(qw);
        // 返回时按时间正序显示
        List<ChatHistory> reversed = list.stream()
                .sorted(Comparator.comparing(ChatHistory::getCreateTime))
                .toList();
        return reversed.stream().map(ChatMessageVo::from).collect(Collectors.toList());
    }

    @Override
    public List<ChatMessageVo> listBeforeCursor(Long appId, Long cursorId, int limit) {
        int size = Math.min(Math.max(limit, 1), 50);
        QueryWrapper cursorQw = QueryWrapper.create().from(ChatHistory.class).where("id = ?", cursorId);
        ChatHistory cursor = this.getOne(cursorQw);
        if (cursor == null) {
            return listLatestByApp(appId, size);
        }
        QueryWrapper qw = QueryWrapper.create()
                .from(ChatHistory.class)
                .where("appId = ?", appId)
                .and("createTime < ?", cursor.getCreateTime())
                .orderBy("createTime desc")
                .limit(size);
        List<ChatHistory> list = this.list(qw);
        List<ChatHistory> reversed = list.stream()
                .sorted(Comparator.comparing(ChatHistory::getCreateTime))
                .toList();
        return reversed.stream().map(ChatMessageVo::from).collect(Collectors.toList());
    }

    @Override
    public boolean removeByAppId(Long appId) {
        QueryWrapper qw = QueryWrapper.create().from(ChatHistory.class).where("appId = ?", appId);
        return this.remove(qw);
    }

    private static void checkParams(Long appId, String message, User user) {
        if (ObjectUtil.isNull(appId) || appId <= 0 || StrUtil.isBlank(message) || ObjectUtil.isNull(user)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
    }

    @Override
    public List<ChatHistory> listAllByApp(Long appId) {
        QueryWrapper qw = QueryWrapper.create()
                .from(ChatHistory.class)
                .where("appId = ?", appId)
                .orderBy("createTime asc");
        return this.list(qw);
    }

    @Override
    public boolean addChatMessage(Long appId, String message, String messageType, Long userId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息内容不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(messageType), ErrorCode.PARAMS_ERROR, "消息类型不能为空");
        ThrowUtils.throwIf(userId == null || userId <= 0, ErrorCode.PARAMS_ERROR, "用户ID不能为空");
        // 验证消息类型是否有效
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getEnumByValue(messageType);
        ThrowUtils.throwIf(messageTypeEnum == null, ErrorCode.PARAMS_ERROR, "不支持的消息类型: " + messageType);
        ChatHistory chatHistory = ChatHistory.builder()
                .appId(appId)
                .message(message)
                .messageType(messageType)
                .userId(userId)
                .build();
        return this.save(chatHistory);
    }

    @Override
    public boolean deleteByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("appId", appId);
        return this.remove(queryWrapper);
    }


    /**
     * 获取查询包装类
     *
     * @param chatHistoryDto
     * @return
     */
    @Override
    public QueryWrapper getQueryWrapper(ChatHistoryDto chatHistoryDto) {
        QueryWrapper queryWrapper = QueryWrapper.create();
        if (chatHistoryDto == null) {
            return queryWrapper;
        }
        Long id = chatHistoryDto.getId();
        String message = chatHistoryDto.getMessage();
        String messageType = chatHistoryDto.getMessageType();
        Long appId = chatHistoryDto.getAppId();
        Long userId = chatHistoryDto.getUserId();
        LocalDateTime lastCreateTime = chatHistoryDto.getLastCreateTime();
        String sortField = chatHistoryDto.getSortField();
        String sortOrder = chatHistoryDto.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq("id", id)
                .like("message", message)
                .eq("messageType", messageType)
                .eq("appId", appId)
                .eq("userId", userId);
        // 游标查询逻辑 - 只使用 createTime 作为游标
        if (lastCreateTime != null) {
            queryWrapper.lt("createTime", lastCreateTime);
        }
        // 排序
        if (StrUtil.isNotBlank(sortField)) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            // 默认按创建时间降序排列
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }

    @Override
    public Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                                      LocalDateTime lastCreateTime,
                                                      User loginUser) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        ThrowUtils.throwIf(pageSize <= 0 || pageSize > 50, ErrorCode.PARAMS_ERROR, "页面大小必须在1-50之间");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        // 验证权限：只有应用创建者和管理员可以查看
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        boolean isAdmin = UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole());
        boolean isCreator = app.getUserId().equals(loginUser.getId());
        ThrowUtils.throwIf(!isAdmin && !isCreator, ErrorCode.NO_AUTH_ERROR, "无权查看该应用的对话历史");
        // 构建查询条件
        ChatHistoryDto queryRequest = new ChatHistoryDto();
        queryRequest.setAppId(appId);
        queryRequest.setLastCreateTime(lastCreateTime);
        QueryWrapper queryWrapper = this.getQueryWrapper(queryRequest);
        // 查询数据
        return this.page(Page.of(1, pageSize), queryWrapper);
    }

    @Override
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount) {
        try {
            // 直接构造查询条件，起始点为 1 而不是 0，用于排除最新的用户消息
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq(ChatHistory::getAppId, appId)
                    .orderBy(ChatHistory::getCreateTime, false)
                    .limit(1, maxCount);
            List<ChatHistory> historyList = this.list(queryWrapper);
            if (CollUtil.isEmpty(historyList)) {
                return 0;
            }
            // 反转列表，确保按时间正序（老的在前，新的在后） Java 21 才支持
            // historyList = historyList.reversed();
            Collections.reverse(historyList);
            // 按时间顺序添加到记忆中
            int loadedCount = 0;
            // 先清理历史缓存，防止重复加载
            chatMemory.clear();
            for (ChatHistory history : historyList) {
                if (MessageTypeEnum.USER.getValue().equals(history.getMessageType())) {
                    chatMemory.add(UserMessage.from(history.getMessage()));
                    loadedCount++;
                } else if (MessageTypeEnum.AI.getValue().equals(history.getMessageType())) {
                    chatMemory.add(AiMessage.from(history.getMessage()));
                    loadedCount++;
                }
            }
            log.info("成功为 appId: {} 加载了 {} 条历史对话", appId, loadedCount);
            return loadedCount;
        } catch (Exception e) {
            log.error("加载历史对话失败，appId: {}, error: {}", appId, e.getMessage(), e);
            // 加载失败不影响系统运行，只是没有历史上下文
            return 0;
        }
    }


}


