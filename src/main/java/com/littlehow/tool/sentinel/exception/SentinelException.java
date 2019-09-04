package com.littlehow.tool.sentinel.exception;

import lombok.Getter;

@Getter
public class SentinelException extends RuntimeException {
    private String code;
    public SentinelException(String message, String code) {
        super(message, null, true, false);
        this.code = code;
    }
}
