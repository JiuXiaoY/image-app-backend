package com.ai.imageagent.domain.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

@Getter
public enum VueProjectBuildToolEnum {

    PNPM_MODE("pnpm", "pnpm"),
    NPM_MODE("npm", "npm"),
    YARN_MODE("yarn","yarn"),
    ;

    private final String text;

    private final String value;

    VueProjectBuildToolEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static VueProjectBuildToolEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (VueProjectBuildToolEnum anEnum : VueProjectBuildToolEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
