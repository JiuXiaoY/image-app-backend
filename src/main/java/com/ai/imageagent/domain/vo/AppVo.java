package com.ai.imageagent.domain.vo;

import cn.hutool.core.bean.BeanUtil;
import com.ai.imageagent.domain.entity.App;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppVo {
    private Long id;
    private String appName;
    private String cover;
    private String codeGenType;
    private String deployKey;
    private Integer priority;
    private Long userId;
    private LocalDateTime createTime;
    private String initPrompt;
    private UserVo user;
    private LocalDateTime updateTime;
    private LocalDateTime deployedTime;


    public static AppVo from(App app) {
        AppVo vo = new AppVo();
        BeanUtil.copyProperties(app, vo);
        return vo;
    }
}


