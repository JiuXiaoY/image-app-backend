package com.ai.imageagent.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.ai.imageagent.ai.AiCodeGenTypeRoutingService;
import com.ai.imageagent.annotation.AuthCheck;
import com.ai.imageagent.annotation.RateLimit;
import com.ai.imageagent.common.BaseResponse;
import com.ai.imageagent.common.ErrorCode;
import com.ai.imageagent.common.ResultUtils;
import com.ai.imageagent.constant.AppConstant;
import com.ai.imageagent.constant.UserConstant;
import com.ai.imageagent.domain.aimodel.CodeGenTypeEnum;
import com.ai.imageagent.domain.deploy.AppDeployRequest;
import com.ai.imageagent.domain.dto.AppDto;
import com.ai.imageagent.domain.entity.App;
import com.ai.imageagent.domain.entity.User;
import com.ai.imageagent.domain.enums.RateLimitType;
import com.ai.imageagent.domain.vo.AppVo;
import com.ai.imageagent.exception.BusinessException;
import com.ai.imageagent.exception.ThrowUtils;
import com.ai.imageagent.service.AppService;
import com.ai.imageagent.service.ChatHistoryService;
import com.ai.imageagent.service.ProjectDownloadService;
import com.ai.imageagent.service.UserService;
import com.mybatisflex.core.paginate.Page;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Map;

/**
 * App 控制层。
 */
@RestController
@RequestMapping("/app")
public class AppController {

    @Resource
    private AppService appService;

    @Resource
    private UserService userService;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private ProjectDownloadService projectDownloadService;

    @Resource
    private AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService;

    // 用户：创建应用（须填写 initPrompt）
    @PostMapping("/user/create")
    public BaseResponse<Long> createApp(@RequestBody AppDto dto, HttpServletRequest request) {
        ThrowUtils.throwIf(dto == null || StrUtil.isBlank(dto.getInitPrompt()), ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        App app = new App();
        BeanUtil.copyProperties(dto, app);
        // 未指定的话，智能路由
        if (app.getCodeGenType() == null) {
            CodeGenTypeEnum codeGenTypeEnum = aiCodeGenTypeRoutingService.routeCodeGenType(dto.getInitPrompt());
            app.setCodeGenType(codeGenTypeEnum.getValue());
        }
        if (app.getAppName() == null) {
            app.setAppName("测试用例");
        }
        app.setUserId(loginUser.getId());
        boolean saved = appService.save(app);
        ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(app.getId());
    }

    // 用户：根据 id 修改自己的应用（仅应用名称）
    @PostMapping("/user/updateName")
    public BaseResponse<Boolean> updateMyAppName(@RequestBody AppDto dto, HttpServletRequest request) {
        ThrowUtils.throwIf(dto == null || dto.getId() == null || StrUtil.isBlank(dto.getAppName()), ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        App exist = appService.getById(dto.getId());
        ThrowUtils.throwIf(exist == null || !exist.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR);
        App update = new App();
        update.setId(dto.getId());
        update.setAppName(dto.getAppName());
        return ResultUtils.success(appService.updateById(update));
    }

    // 用户：根据 id 删除自己的应用
    @PostMapping("/user/delete")
    public BaseResponse<Boolean> deleteMyApp(@RequestBody AppDto dto, HttpServletRequest request) {
        ThrowUtils.throwIf(dto == null || dto.getId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        App exist = appService.getById(dto.getId());
        ThrowUtils.throwIf(exist == null || !exist.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR);
        chatHistoryService.removeByAppId(exist.getId());
        return ResultUtils.success(appService.removeById(dto.getId()));
    }

    // 用户：根据 id 查看应用详情
    @GetMapping("/user/get")
    public BaseResponse<AppVo> getMyApp(Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        App exist = appService.getById(id);
        ThrowUtils.throwIf(exist == null || !exist.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR);
        return ResultUtils.success(AppVo.from(exist));
    }

    // 用户：分页查询自己的应用列表（支持名称，每页<=20）
    @PostMapping("/user/page")
    public BaseResponse<Page<App>> pageMyApps(@RequestBody AppDto dto, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        dto.setUserId(loginUser.getId());
        return ResultUtils.success(appService.pageByDto(dto, false));
    }

    // 用户：分页查询精选应用列表（支持名称，每页<=20）
    @PostMapping("/user/featured/page")
    @Cacheable(
            value = "good_app_page",
            key = "T(com.ai.imageagent.utils.CacheKeyUtils).generateKey(#dto)",
            condition = "#dto.pageNum <= 10"
    )
    @AuthCheck(mustRole = UserConstant.DEFAULT_ROLE)
    public BaseResponse<Page<App>> pageFeatured(@RequestBody AppDto dto) {
        // 精选：按优先级倒序、名称过滤，由 service 的 userQuery 满足
        return ResultUtils.success(appService.pageByDto(dto, false));
    }

    // 管理员：根据 id 删除任意应用
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> adminDelete(@RequestBody AppDto dto) {
        ThrowUtils.throwIf(dto == null || dto.getId() == null, ErrorCode.PARAMS_ERROR);
        chatHistoryService.removeByAppId(dto.getId());
        return ResultUtils.success(appService.removeById(dto.getId()));
    }

    // 管理员：根据 id 更新任意应用（名称、封面、优先级）
    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> adminUpdate(@RequestBody AppDto dto) {
        ThrowUtils.throwIf(dto == null || dto.getId() == null, ErrorCode.PARAMS_ERROR);
        App update = new App();
        update.setId(dto.getId());
        update.setAppName(dto.getAppName());
        update.setCover(dto.getCover());
        update.setPriority(dto.getPriority());
        return ResultUtils.success(appService.updateById(update));
    }

    // 管理员：分页查询应用列表（支持除时间外的任意字段）
    @PostMapping("/admin/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<App>> adminPage(@RequestBody AppDto dto) {
        return ResultUtils.success(appService.pageByDto(dto, true));
    }

    // 管理员：根据 id 查看应用详情
    @GetMapping("/admin/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<AppVo> adminGet(Long id) {
        ThrowUtils.throwIf(id == null, ErrorCode.PARAMS_ERROR, "应用ID无效");
        App app = appService.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        return ResultUtils.success(AppVo.from(app));
    }

    /**
     * 应用聊天生成代码（流式 SSE）
     *
     * @param appId   应用 ID
     * @param message 用户消息
     * @param request 请求对象
     * @return 生成结果流
     */
    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @RateLimit(limitType = RateLimitType.USER, rate = 5, rateInterval = 60, message = "Ai对话请求过于频繁，请稍后再试")
    public Flux<ServerSentEvent<String>> chatToGenCode(@RequestParam Long appId,
                                                       @RequestParam String message,
                                                       @RequestParam(defaultValue = "false")boolean agent,
                                                       HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务生成代码（流式）
        Flux<String> stringFlux = appService.chatToGenCode(appId, message, loginUser, agent);
        return stringFlux.map(chunk -> {
            Map<String, String> wrapper = Map.of("d", chunk);
            String jsonData = JSONUtil.toJsonStr(wrapper);
            return ServerSentEvent.<String>builder().data(jsonData).build();
        }).concatWith(Mono.just(ServerSentEvent.<String>builder().event("done").data("").build()));
    }


    /**
     * 应用部署
     *
     * @param appDeployRequest 部署请求
     * @param request          请求
     * @return 部署 URL
     */
    @PostMapping("/deploy")
    public BaseResponse<String> deployApp(@RequestBody AppDeployRequest appDeployRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appDeployRequest == null, ErrorCode.PARAMS_ERROR);
        Long appId = appDeployRequest.getAppId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务部署应用
        String deployUrl = appService.deployApp(appId, loginUser);
        return ResultUtils.success(deployUrl);
    }

    /**
     * 下载应用代码
     *
     * @param appId    应用ID
     * @param request  请求
     * @param response 响应
     */
    @GetMapping("/download/{appId}")
    public void downloadAppCode(@PathVariable Long appId,
                                HttpServletRequest request,
                                HttpServletResponse response) {
        // 1. 基础校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        // 2. 查询应用信息
        App app = appService.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 权限校验：只有应用创建者可以下载代码
        User loginUser = userService.getLoginUser(request);
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限下载该应用代码");
        }
        // 4. 构建应用代码目录路径（生成目录，非部署目录）
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 5. 检查代码目录是否存在
        File sourceDir = new File(sourceDirPath);
        ThrowUtils.throwIf(!sourceDir.exists() || !sourceDir.isDirectory(),
                ErrorCode.NOT_FOUND_ERROR, "应用代码不存在，请先生成代码");
        // 6. 生成下载文件名（不建议添加中文内容）
        String downloadFileName = String.valueOf(appId);
        // 7. 调用通用下载服务
        projectDownloadService.downloadProjectAsZip(sourceDirPath, downloadFileName, response);
    }


}


