package com.munchisunmiprinter;

final class SunmiPrinterTaskException extends RuntimeException {
    final String code;
    final String category;
    final Integer nativeStatusCode;
    final String nativeCode;
    final String nativeMessage;

    SunmiPrinterTaskException(
        String code,
        String message,
        String category,
        Integer nativeStatusCode,
        String nativeCode,
        String nativeMessage,
        Throwable cause
    ) {
        super(message, cause);
        this.code = code;
        this.category = category;
        this.nativeStatusCode = nativeStatusCode;
        this.nativeCode = nativeCode;
        this.nativeMessage = nativeMessage;
    }

    SunmiPrinterTaskException(
        String code,
        String message,
        String category,
        Integer nativeStatusCode,
        String nativeCode,
        String nativeMessage
    ) {
        this(code, message, category, nativeStatusCode, nativeCode, nativeMessage, null);
    }
}
