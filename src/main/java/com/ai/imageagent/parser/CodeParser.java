package com.ai.imageagent.parser;

import com.ai.imageagent.domain.aimodel.CodeGenTypeEnum;

/**
 * 代码解析器策略接口
 *
 * @author chenqj
 */
public interface CodeParser<T> {

    /**
     * 解析代码内容
     *
     * @param codeContent 原始代码内容
     * @return 解析后的结果对象
     */
    T parseCode(String codeContent);

    /**
     * 支持的代码生成类型
     *
     * @return 对应的 CodeGenTypeEnum
     */
    CodeGenTypeEnum supportedType();
}

