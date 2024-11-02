package com.neoworld.cooking.utils.exceptions;

public class ConfigurationLoadFailedException extends Exception {
    public ConfigurationLoadFailedException(String msg) {
        super(msg);
    }

    public ConfigurationLoadFailedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
