package com.ai.imageagent.domain.aimodel;

import dev.langchain4j.model.output.structured.Description;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Description("生成 HTML 代码文件的结果")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HtmlCodeResult {

    @Description("HTML代码")
    private String htmlCode;

    @Description("生成代码的描述")
    private String description;
}

