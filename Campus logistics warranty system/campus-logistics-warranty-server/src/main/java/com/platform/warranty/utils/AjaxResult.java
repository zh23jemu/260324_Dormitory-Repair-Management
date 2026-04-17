package com.platform.warranty.utils;

import lombok.Data;

@Data
public class  AjaxResult<T> {
     // 状态码（默认200表示成功）
    private int code;
    // 响应消息
    private String msg;
    // 泛型响应数据
    private T data;

    // 私有构造方法（强制使用工厂方法）
    private AjaxResult() {}

    // ----------------- 成功静态工厂方法 -----------------

    /**
     * 成功（默认状态码200，无数据）
     */
    public static <T> AjaxResult<T> success() {
        return success(null);
    }

    /**
     * 成功（默认状态码200，携带数据）
     */
    public static <T> AjaxResult<T> success(T data) {
        return new AjaxResult<T>()
                .code(200)
                .msg("操作成功")
                .data(data);
    }

    // ----------------- 失败静态工厂方法 -----------------

    /**
     * 失败（默认状态码500，自定义错误消息）
     */
    public static <T> AjaxResult<T> error(String msg) {
        return error(500, msg, null);
    }
    public static <T> AjaxResult<T> error(Integer code, String msg) {
        return error(code, msg,null);
    }
    public static <T> AjaxResult<T> error() {
        return error(500, "操作失败",null);
    }
    public static <T> AjaxResult<T> error(T data) {
        return error(500, "操作失败", data);
    }

    /**
     * 失败（自定义状态码和错误消息）
     */
    public static <T> AjaxResult<T> error(int code, String msg,T data) {
        return new AjaxResult<T>()
                .code(code)
                .msg(msg)
                .data(data);
    }

    // ----------------- 链式调用方法 -----------------

    public AjaxResult<T> code(int code) {
        this.code = code;
        return this;
    }

    public AjaxResult<T> msg(String msg) {
        this.msg = msg;
        return this;
    }

    public AjaxResult<T> data(T data) {
        this.data = data;
        return this;
    }
}
