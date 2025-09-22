package com.ai.imageagent.utils;

import java.util.regex.Pattern;

public interface CodePatterUtil {

    /**
     * ```html：匹配代码块的开头，指定语言是 html。
     * \\s*\\n：匹配后面可能有若干空格，然后换行。
     * ([\\s\\S]*?)：捕获组，匹配 任意字符（包括换行），非贪婪模式，直到遇到结尾。
     * \\s = 空白符
     * \\S = 非空白符
     * [\\s\\S] 合在一起表示“所有字符”
     * ```：匹配代码块的结尾。
     * Pattern.CASE_INSENSITIVE：忽略大小写，例如 HTML 也能识别。
     */

    Pattern HTML_CODE_PATTERN = Pattern.compile("```html\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    Pattern CSS_CODE_PATTERN = Pattern.compile("```css\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
    Pattern JS_CODE_PATTERN = Pattern.compile("```(?:js|javascript)\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);


}
