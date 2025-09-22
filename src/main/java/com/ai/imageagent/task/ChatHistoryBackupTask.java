package com.ai.imageagent.task;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.ai.imageagent.constant.AppConstant;
import com.ai.imageagent.domain.entity.App;
import com.ai.imageagent.domain.entity.ChatHistory;
import com.ai.imageagent.domain.enums.MessageTypeEnum;
import com.ai.imageagent.service.AppService;
import com.ai.imageagent.service.ChatHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
public class ChatHistoryBackupTask {

    @Resource
    private AppService appService;

    @Resource
    private ChatHistoryService chatHistoryService;

    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * 每 7 天跑一次
     */
    @Scheduled(cron = "0 0 3 */7 * ?")
    public void backupAllAppsChatHistory() {
        try {
            FileUtil.mkdir(AppConstant.CHAT_HISTORY_BACKUP_DIR);
            List<App> apps = appService.list();
            for (App app : apps) {
                try {
                    backupSingleApp(app);
                } catch (Exception e) {
                    log.error("备份应用对话失败 appId={} error={}", app.getId(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("定时备份任务异常: {}", e.getMessage(), e);
        }
    }

    private void backupSingleApp(App app) {
        Long appId = app.getId();
        List<ChatHistory> historyList = chatHistoryService.listAllByApp(appId);
        if (ObjectUtil.isEmpty(historyList)) {
            log.info("appId={} 无历史对话，跳过备份", appId);
            return;
        }
        String ts = LocalDateTime.now().format(FILE_TS);
        String fileName = StrFormatter.format("app_{}_{}.md", appId, ts);
        File out = new File(AppConstant.CHAT_HISTORY_BACKUP_DIR, fileName);
        String md = buildMarkdown(app, historyList);
        FileUtil.writeString(md, out, StandardCharsets.UTF_8);
        log.info("已备份应用聊天记录 appId={} -> {}", appId, out.getAbsolutePath());
    }

    private static String buildMarkdown(App app, List<ChatHistory> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("# App Chat History\n\n");
        sb.append("- AppId: ").append(app.getId()).append("\n");
        if (StrUtil.isNotBlank(app.getAppName())) {
            sb.append("- AppName: ").append(app.getAppName()).append("\n");
        }
        sb.append("- ExportedAt: ").append(LocalDateTime.now()).append("\n\n");
        for (ChatHistory ch : items) {
            String role = ch.getMessageType();
            if (MessageTypeEnum.USER.getValue().equalsIgnoreCase(role)) {
                role = "User";
            } else if (MessageTypeEnum.AI.getValue().equalsIgnoreCase(role)) {
                role = "AI";
            }
            sb.append("## ").append(role).append(" @ ").append(ch.getCreateTime()).append("\n\n");
            sb.append("```").append("\n");
            sb.append(StrUtil.nullToEmpty(ch.getMessage())).append("\n");
            sb.append("```").append("\n\n");
        }
        return sb.toString();
    }
}


