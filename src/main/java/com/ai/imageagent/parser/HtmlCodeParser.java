package com.ai.imageagent.parser;

import com.ai.imageagent.domain.aimodel.HtmlCodeResult;
import com.ai.imageagent.domain.aimodel.CodeGenTypeEnum;
import com.ai.imageagent.utils.CodePatterUtil;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;

/**
 * HTML 单文件代码解析器
 *
 * @author chenqj
 */
@Component
public class HtmlCodeParser implements CodeParser<HtmlCodeResult> {
    @Override
    public HtmlCodeResult parseCode(String codeContent) {
        String htmlCode = extractHtmlCode(codeContent);
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            return new HtmlCodeResult(htmlCode.trim(), "here need description");
        }
        return new HtmlCodeResult(codeContent.trim(), "here need description");
    }

    /**
     * 提取HTML代码内容
     *
     * @param content 原始内容
     * @return HTML代码
     */
    private String extractHtmlCode(String content) {
        Matcher matcher = CodePatterUtil.HTML_CODE_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    @Override
    public CodeGenTypeEnum supportedType() {
        return CodeGenTypeEnum.HTML;
    }
}

