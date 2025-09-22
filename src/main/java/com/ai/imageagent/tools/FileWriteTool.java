package com.ai.imageagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import com.ai.imageagent.constant.AppConstant;
import com.ai.imageagent.constant.FileToolConstant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * 文件写入工具
 * 支持 ai 调用方式
 */
@Slf4j
@Component
public class FileWriteTool extends BaseTool{

    @Override
    public String getToolName() {
        return FileToolConstant.FILE_WRITE_TOOL_NAME;
    }

    @Override
    public String getDisplayName() {
        return FileToolConstant.FILE_WRITE_TOOL_DISPLAY_NAME;
    }

    @Override
    public String generateToolExecutedResult(JSONObject arguments) {
        String relativeFilePath = arguments.getStr("relativeFilePath");
        String suffix = FileUtil.getSuffix(relativeFilePath);
        String content = arguments.getStr("content");
        return String.format("""
                        [工具调用] %s %s
                        ```%s
                        %s
                        ```
                        """, getDisplayName(), relativeFilePath, suffix, content);
    }

    @Tool(
            name = "file_write_tool"
    )
    public String writeFile(
            @P("文件的相对路径") String relativeFilePath,
            @P("文件内容") String content,
            @ToolMemoryId Long appId) {
        try {
            Path path = Paths.get(relativeFilePath);
            if (!path.isAbsolute()) {
                String projectDirName = "vue_project_" + appId;
                Path projectRoot = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, projectDirName);
                path = projectRoot.resolve(path);
            }
            // 创建父目录，如果不存在
            Path pathParent = path.getParent();
            if (ObjectUtil.isNotNull(pathParent)) {
                Files.createDirectories(pathParent);
            }
            // 写入文件内容
            Files.write(path, content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("writeFile success, path: {}", path);
            return "写入文件成功: " + relativeFilePath;
        } catch (Exception e) {
            log.error("writeFile error, {}", e.getMessage(), e);
            return "写入文件失败: " + relativeFilePath + ", 错误原因: " + e.getMessage();
        }
    }
}
