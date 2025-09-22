package com.ai.imageagent.core;

import com.ai.imageagent.domain.aimodel.HtmlCodeResult;
import com.ai.imageagent.domain.aimodel.MultiFileCodeResult;
import com.ai.imageagent.utils.CodePatterUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代码解析器提
 * 供静态方法解析不同代码
 */
@Deprecated
public class CodeParser {

    /**
     * 解析单文件
     * @param codeContent 代码内容
     * @return HTML代码
     */
    public static HtmlCodeResult parseHtmlCode(String codeContent) {
        String htmlCode = extractHtmlCode(codeContent);
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            return new HtmlCodeResult(htmlCode.trim(), "here need description");
        }
        return new HtmlCodeResult(codeContent.trim(), "here need description");
    }

    /**
     * 解析多文件
     * @param codeContent 代码内容
     * @return HTML代码
     */
    public static MultiFileCodeResult parseMultiFileCode(String codeContent) {
        MultiFileCodeResult multiFileCodeResult = new MultiFileCodeResult();
        String jsCode = extractCodeByPattern(codeContent, CodePatterUtil.JS_CODE_PATTERN);
        String htmlCode = extractCodeByPattern(codeContent, CodePatterUtil.HTML_CODE_PATTERN);
        String cssCode = extractCodeByPattern(codeContent, CodePatterUtil.CSS_CODE_PATTERN);
        if (jsCode != null && !jsCode.trim().isEmpty()) {
            multiFileCodeResult.setJsCode(jsCode.trim());
        }
        if (htmlCode != null && !htmlCode.trim().isEmpty()) {
            multiFileCodeResult.setHtmlCode(htmlCode.trim());
        }
        if (cssCode != null && !cssCode.trim().isEmpty()) {
            multiFileCodeResult.setCssCode(cssCode.trim());
        }
        return multiFileCodeResult;
    }

    /**
     * 通过正则表达式提取HTML代码
     * @param content 待处理的内容
     * @return HTML代码
     */
    public static String extractHtmlCode(String content) {
        Matcher matcher = CodePatterUtil.HTML_CODE_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 通过正则表达式提取代码
     *
     * @param content 待处理的内容
     * @param pattern 正则表达式
     * @return 提取的代码
     */
    public static String extractCodeByPattern(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
