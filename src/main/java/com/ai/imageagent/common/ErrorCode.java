package com.ai.imageagent.common;

/**
 * 自定义错误码
 */
public enum ErrorCode {

    SUCCESS(0, "ok"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    PARAMS_ERROR(50001, "参数错误"),
    NOT_LOGIN_ERROR(50002, "未登录"),
    OPERATION_ERROR(50003, "操作异常"),
    NO_AUTH_ERROR(40100, "无权限"),
    NOT_FOUND_ERROR(50004, "未找到用户"),
    TOO_MANY_REQUEST(42900, "请求过于频繁"),

    ;

    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
