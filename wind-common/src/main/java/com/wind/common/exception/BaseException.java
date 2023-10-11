package com.wind.common.exception;


import lombok.Getter;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

/**
 * 通用基础业务异常，不同的业务场景可以继承该类做扩展
 * 支出对消息做 i18n 处理 {@link #MESSAGE_I18N}
 *
 * @author wuxp
 */
@Getter
public class BaseException extends RuntimeException {

    private static final long serialVersionUID = 319556802147084526L;

    /**
     * 异常消息 i18n 支持
     */
    private static final AtomicReference<UnaryOperator<String>> MESSAGE_I18N = new AtomicReference<>(t -> t);

    private final ExceptionCode code;

    public BaseException(String message) {
        this(DefaultExceptionCode.COMMON_ERROR, message);
    }

    public BaseException(ExceptionCode code, String message) {
        this(code, message, null);
    }

    public BaseException(ExceptionCode code, String message, Throwable cause) {
        super(MESSAGE_I18N.get().apply(message), cause);
        this.code = code;
    }

    public String getTextCode() {
        return code.getCode();
    }

    public static BaseException badRequest(String message) {
        return new BaseException(DefaultExceptionCode.BAD_REQUEST, message);
    }

    public static BaseException unAuthorized(String message) {
        return new BaseException(DefaultExceptionCode.UNAUTHORIZED, message);
    }

    public static BaseException forbidden(String message) {
        return new BaseException(DefaultExceptionCode.FORBIDDEN, message);
    }

    public static BaseException notFound(String message) {
        return new BaseException(DefaultExceptionCode.NOT_FOUND, message);
    }

    public static BaseException common(String message) {
        return new BaseException(message);
    }


    public static void setMessageI18n(UnaryOperator<String> converter) {
        MESSAGE_I18N.set(converter);
    }

}
