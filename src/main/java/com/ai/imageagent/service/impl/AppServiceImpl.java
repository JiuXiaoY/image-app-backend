package com.ai.imageagent.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.ai.imageagent.ai.AiCodeGeneratorFacade;
import com.ai.imageagent.common.ErrorCode;
import com.ai.imageagent.constant.AppConstant;
import com.ai.imageagent.domain.aimodel.CodeGenTypeEnum;
import com.ai.imageagent.domain.dto.AppDto;
import com.ai.imageagent.domain.entity.App;
import com.ai.imageagent.domain.entity.User;
import com.ai.imageagent.domain.enums.MessageTypeEnum;
import com.ai.imageagent.domain.enums.VueProjectBuildToolEnum;
import com.ai.imageagent.exception.BusinessException;
import com.ai.imageagent.exception.ThrowUtils;
import com.ai.imageagent.langgraph4j.CodeGenWorkflow;
import com.ai.imageagent.mamager.CosManager;
import com.ai.imageagent.mapper.AppMapper;
import com.ai.imageagent.service.AppService;
import com.ai.imageagent.service.ChatHistoryService;
import com.ai.imageagent.service.ScreenshotService;
import com.ai.imageagent.streamhandler.StreamHandlerExecutor;
import com.ai.imageagent.tools.VueProjectBuilderTool;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;
import java.io.File;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;
    @Autowired
    private VueProjectBuilderTool vueProjectBuilderTool;

    @Resource
    private ScreenshotService screenshotService;

    @Resource
    private ThreadPoolExecutor saveCoverExecutor;
    @Autowired
    private CosManager cosManager;

    @Value("${code.deploy-host:http://localhost}")
    private String deployHost;

    @Override
    public QueryWrapper buildUserQuery(AppDto dto) {
        QueryWrapper qw = QueryWrapper.create();
        qw.from(App.class);
        if (StrUtil.isNotBlank(dto.getAppName())) {
            qw.like(App::getAppName, dto.getAppName());
        }
        if (ObjectUtil.isNotNull(dto.getUserId())) {
            qw.and(App::getUserId).eq(dto.getUserId());
        }
        qw.and(App::getPriority).eq(99);
        // 按优先级、更新时间倒序
        qw.orderBy(App::getPriority, false);
        qw.orderBy(App::getUpdateTime, false);
        return qw;
    }

    @Override
    public QueryWrapper buildAdminQuery(AppDto dto) {
        QueryWrapper qw = QueryWrapper.create();
        qw.from(App.class);
        if (StrUtil.isNotBlank(dto.getAppName())) {
            qw.like(App::getAppName, dto.getAppName());
        }
        if (StrUtil.isNotBlank(dto.getCover())) {
            qw.and(App::getCover).like(dto.getCover());
        }
        if (StrUtil.isNotBlank(dto.getCodeGenType())) {
            qw.and(App::getCodeGenType).eq(dto.getCodeGenType());
        }
        if (StrUtil.isNotBlank(dto.getDeployKey())) {
            qw.and(App::getDeployKey).eq(dto.getDeployKey());
        }
        if (ObjectUtil.isNotNull(dto.getPriority())) {
            qw.and(App::getPriority).eq(dto.getPriority());
        }
        if (ObjectUtil.isNotNull(dto.getUserId())) {
            qw.and(App::getUserId).eq(dto.getUserId());
        }
        // 按优先级、更新时间倒序
        qw.orderBy(App::getPriority, false);
        qw.orderBy(App::getUpdateTime, false);
        return qw;
    }

    @Override
    public Page<App> pageByDto(AppDto dto, boolean isAdmin) {
        int pageNumber = dto.getPageNum();
        int pageSize = dto.getPageSize();
        if (!isAdmin) {
            pageSize = Math.min(pageSize, 20);
        }
        QueryWrapper qw = isAdmin ? buildAdminQuery(dto) : buildUserQuery(dto);
        return this.page(new Page<>(pageNumber, pageSize), qw);
    }

    @Override
    public Flux<String> chatToGenCode(Long appId, String userMessage, User loginUser, boolean agent) {
        if (ObjectUtil.isNull(appId) || appId < 0L) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "appId 非法（为空或者小于0）");
        }
        if (StrUtil.isBlank(userMessage)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        }
        App app = this.getById(appId);
        if (ObjectUtil.isNull(app)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "app不存在");
        }
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有权限");
        }
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        // 添加用户消息到历史记录
        chatHistoryService.addChatMessage(appId, userMessage, MessageTypeEnum.USER.getValue(), loginUser.getId());
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenType);
        }
        // 6. 根据 agent 参数选择生成方式
        Flux<String> codeStream;
        if (agent) {
            // Agent 模式：使用工作流生成代码
            codeStream = new CodeGenWorkflow().executeWorkflowWithFlux(userMessage, appId);
        } else {
            // 传统模式：调用 AI 生成代码（流式）
            codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(userMessage, codeGenTypeEnum, appId);
        }

        return streamHandlerExecutor.doExecute(codeStream, chatHistoryService, appId, loginUser, codeGenTypeEnum);
    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        // 参数检验
        if (ObjectUtil.isNull(appId) || appId < 0L) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "appId 非法（为空或者小于0）");
        }
        if (ObjectUtil.isNull(loginUser)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        App app = this.getById(appId);
        if (ObjectUtil.isNull(app)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "app不存在");
        }
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有权限");
        }
        String deployKey = app.getDeployKey();
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        // 构建路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "源代码目录不存在");
        }

        // Vue 项目特殊处理
        CodeGenTypeEnum vueType = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (ObjectUtil.equals(vueType, CodeGenTypeEnum.VUE_PROJECT)) {
            boolean buildSuccess = vueProjectBuilderTool.buildProject(sourceDirPath, VueProjectBuildToolEnum.PNPM_MODE);
            ThrowUtils.throwIf(!buildSuccess, new BusinessException(ErrorCode.SYSTEM_ERROR, "构建 Vue 项目失败"));

            // 检查 dist 目录是否存在
            File distDir = new File(sourceDir, "dist");
            ThrowUtils.throwIf(!distDir.exists(), new BusinessException(ErrorCode.SYSTEM_ERROR, "构建 Vue 项目失败，缺失 dist 目录"));
            sourceDir = distDir;
            log.info("Vue 项目构建成功，dist 目录: {}", distDir.getAbsolutePath());
        }

        // 复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "复制文件到部署目录失败" + e.getMessage());
        }
        // 回写 deployKey 以及部署时间
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateRes = this.updateById(updateApp);
        if (!updateRes) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "回写 deployKey 以及部署时间失败");
        }
        log.info("部署成功，deployKey: {}, appId: {}", deployKey, appId);
        // 返回路径
//        String appDeployUrl = String.format("%s%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
        // 10. 构建应用访问 URL
        String appDeployUrl = String.format("%s/%s/", deployHost, deployKey);
        // 截图保存封面
        generateAppScreenshotAsync(appId, appDeployUrl);
        return appDeployUrl;
    }


    /**
     * 异步生成应用截图并更新封面
     *
     * @param appId  应用ID
     * @param appUrl 应用访问URL
     */
    @Override
    public void generateAppScreenshotAsync(Long appId, String appUrl) {
        saveCoverExecutor.submit(() -> {
            try {
                // 调用截图服务生成截图并上传
                String screenshotUrl = screenshotService.generateAndUploadScreenshot(appUrl);
                // 更新应用封面字段
                App updateApp = new App();
                updateApp.setId(appId);
                updateApp.setCover(screenshotUrl);
                boolean updated = this.updateById(updateApp);
                ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新应用封面字段失败");
            } catch (Exception e) {
                log.error("生成应用截图并更新封面异常:{}", e.getMessage());
            }
        });
    }
}


