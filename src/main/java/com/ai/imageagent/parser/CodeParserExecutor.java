package com.ai.imageagent.parser;

import com.ai.imageagent.common.ErrorCode;
import com.ai.imageagent.domain.aimodel.CodeGenTypeEnum;
import com.ai.imageagent.exception.BusinessException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代码解析执行器
 * 根据代码生成类型执行相应的解析逻辑
 *
 * @author chenqj
 */
@Component
public class CodeParserExecutor {

    @Resource
    private List<CodeParser<?>> codeParsers;

    private final Map<CodeGenTypeEnum, CodeParser<?>> typeToParser = new HashMap<>();

    @PostConstruct
    public void init() {
        for (CodeParser<?> parser : codeParsers) {
            CodeGenTypeEnum type = parser.supportedType();
            if (typeToParser.putIfAbsent(type, parser) != null) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "存在重复的解析器实现: " + type);
            }
        }
    }

    /**
     * 执行代码解析
     *
     * @param codeContent 代码内容
     * @param codeGenType 代码生成类型
     * @return 解析结果（HtmlCodeResult 或 MultiFileCodeResult）
     */
    public Object executeParser(String codeContent, CodeGenTypeEnum codeGenType) {
        CodeParser<?> parser = typeToParser.get(codeGenType);
        if (parser == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型: " + codeGenType);
        }
        return parser.parseCode(codeContent);
    }
}

