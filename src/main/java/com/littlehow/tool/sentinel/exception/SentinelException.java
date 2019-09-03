package com.littlehow.tool.sentinel.exception;

import lombok.Getter;

@Getter
public class SentinelException extends RuntimeException {
    private String code;
    public SentinelException(String message, String code) {
        super(message);
        this.code = code;
    }
}
