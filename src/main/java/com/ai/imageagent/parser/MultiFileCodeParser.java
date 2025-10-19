package com.ai.imageagent.parser;

import com.ai.imageagent.domain.aimodel.MultiFileCodeResult;
import com.ai.imageagent.domain.aimodel.CodeGenTypeEnum;
import com.ai.imageagent.utils.CodePatterUtil;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 多文件代码解析器（HTML + CSS + JS）
 *
 * @author chenqj
 */
@Component
public class MultiFileCodeParser implements CodeParser<MultiFileCodeResult> {
    @Override
    public MultiFileCodeResult parseCode(String codeContent) {
        MultiFileCodeResult result = new MultiFileCodeResult();
        // 提取各类代码
        String htmlCode = extractCodeByPattern(codeContent, CodePatterUtil.HTML_CODE_PATTERN);
        String cssCode = extractCodeByPattern(codeContent, CodePatterUtil.CSS_CODE_PATTERN);
        String jsCode = extractCodeByPattern(codeContent, CodePatterUtil.JS_CODE_PATTERN);
        // 设置HTML代码
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            result.setHtmlCode(htmlCode.trim());
        }
        // 设置CSS代码
        if (cssCode != null && !cssCode.trim().isEmpty()) {
            result.setCssCode(cssCode.trim());
        }
        // 设置JS代码
        if (jsCode != null && !jsCode.trim().isEmpty()) {
            result.setJsCode(jsCode.trim());
        }
        return result;
    }

    /**
     * 根据正则模式提取代码
     *
     * @param content 原始内容
     * @param pattern 正则模式
     * @return 提取的代码
     */
    private String extractCodeByPattern(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    @Override
    public CodeGenTypeEnum supportedType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }
}

