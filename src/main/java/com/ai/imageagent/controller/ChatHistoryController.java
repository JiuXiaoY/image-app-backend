package com.ai.imageagent.controller;

import cn.hutool.core.util.ObjectUtil;
import com.ai.imageagent.annotation.AuthCheck;
import com.ai.imageagent.common.BaseResponse;
import com.ai.imageagent.common.ErrorCode;
import com.ai.imageagent.common.ResultUtils;
import com.ai.imageagent.constant.UserConstant;
import com.ai.imageagent.domain.dto.ChatHistoryDto;
import com.ai.imageagent.domain.entity.App;
import com.ai.imageagent.domain.entity.ChatHistory;
import com.ai.imageagent.domain.entity.User;
import com.ai.imageagent.domain.vo.ChatMessageVo;
import com.ai.imageagent.exception.ThrowUtils;
import com.ai.imageagent.service.AppService;
import com.ai.imageagent.service.ChatHistoryService;
import com.ai.imageagent.service.UserService;
import com.mybatisflex.core.paginate.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 控制层。
 */
@RestController
@RequestMapping("/chat")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UserService userService;

    @Resource
    private AppService appService;

    // 用户：加载应用下最新 N 条消息（仅应用创建者可见）
    @GetMapping("/user/list/latest")
    public BaseResponse<List<ChatMessageVo>> listLatest(@RequestParam Long appId,
                                                        @RequestParam(required = false, defaultValue = "10") Integer limit,
                                                        HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(appId) || appId <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null || !app.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR);
        int size = ObjectUtil.defaultIfNull(limit, 10);
        return ResultUtils.success(chatHistoryService.listLatestByApp(appId, size));
    }

    // 用户：按游标向前加载更多（仅应用创建者可见）
    @GetMapping("/user/list/before")
    public BaseResponse<List<ChatMessageVo>> listBefore(@RequestParam Long appId,
                                                        @RequestParam Long cursorId,
                                                        @RequestParam(required = false, defaultValue = "10") Integer limit,
                                                        HttpServletRequest request) {
        ThrowUtils.throwIf(ObjectUtil.isNull(appId) || appId <= 0 || ObjectUtil.isNull(cursorId) || cursorId <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null || !app.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR);
        int size = ObjectUtil.defaultIfNull(limit, 10);
        return ResultUtils.success(chatHistoryService.listBeforeCursor(appId, cursorId, size));
    }

    // 管理员：分页查看所有应用的对话历史，按时间降序
    @PostMapping("/admin/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<?>> adminPage(@RequestBody ChatHistoryDto dto) {
        Page<?> page = chatHistoryService.pageAdmin(dto);
        return ResultUtils.success(page);
    }


    /**
     * 分页查询某个应用的对话历史（游标查询）
     *
     * @param appId          应用ID
     * @param pageSize       页面大小
     * @param lastCreateTime 最后一条记录的创建时间
     * @param request        请求
     * @return 对话历史分页
     */
    @GetMapping("/app/{appId}")
    public BaseResponse<Page<ChatHistory>> listAppChatHistory(@PathVariable Long appId,
                                                              @RequestParam(defaultValue = "10") int pageSize,
                                                              @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")LocalDateTime lastCreateTime,
                                                              HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Page<ChatHistory> result = chatHistoryService.listAppChatHistoryByPage(appId, pageSize, lastCreateTime, loginUser);
        return ResultUtils.success(result);
    }

}


