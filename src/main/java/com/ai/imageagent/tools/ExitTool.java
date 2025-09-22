package com.ai.imageagent.tools;

import cn.hutool.json.JSONObject;
import com.ai.imageagent.constant.FileToolConstant;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExitTool extends BaseTool {

    @Override
    public String getToolName() {
        return FileToolConstant.FILE_EXIT_TOOL_NAME;
    }

    @Override
    public String getDisplayName() {
        return FileToolConstant.FILE_EXIT_TOOL_DISPLAY_NAME;
    }

    /**
     * 退出工具调用
     * 当任务完成或无需继续使用工具时调用此方法
     *
     * @return 退出确认信息
     */
    @Tool(
            name = "file_exit_tool"
    )
    public String exit() {
        log.info("AI 请求退出工具调用");
        return "不要继续调用工具，可以输出最终结果了";
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        return "\n\n[执行结束]\n\n";
    }
}

