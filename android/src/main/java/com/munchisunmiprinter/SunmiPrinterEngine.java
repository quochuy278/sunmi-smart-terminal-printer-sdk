package com.munchisunmiprinter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.RemoteException;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.Nullable;

import com.sunmi.peripheral.printer.InnerPrinterCallback;
import com.sunmi.peripheral.printer.InnerPrinterException;
import com.sunmi.peripheral.printer.InnerPrinterManager;
import com.sunmi.peripheral.printer.InnerResultCallback;
import com.sunmi.peripheral.printer.SunmiPrinterService;
import com.sunmi.peripheral.printer.WoyouConsts;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

final class SunmiPrinterEngine implements SunmiPrintQueueRuntime.TaskProcessor {
    private static final String LOG_TAG = "MunchiSunmiPrinter";
    private static final String CATEGORY_DEVICE = "DEVICE";
    private static final String CATEGORY_INTEGRATION = "INTEGRATION";
    private static final String CATEGORY_PRINTER = "PRINTER";
    private static final long BIND_TIMEOUT_MS = 5000L;
    private static final long CALLBACK_TIMEOUT_MS = 5000L;
    private static final int ALIGN_LEFT = 0;
    private static final int ALIGN_CENTER = 1;
    private static final int ALIGN_RIGHT = 2;
    private static final int STATUS_NORMAL = 1;
    private static final int STATUS_PREPARING = 2;
    private static final int STATUS_COMM_ERROR = 3;
    private static final int STATUS_OUT_OF_PAPER = 4;
    private static final int STATUS_OVERHEATED = 5;
    private static final int STATUS_COVER_OPEN = 6;
    private static final int STATUS_CUTTER_ERROR = 7;
    private static final int STATUS_CUTTER_RECOVERED = 8;
    private static final int STATUS_BLACK_MARK_NOT_FOUND = 9;
    private static final int STATUS_PRINTER_NOT_DETECTED = 505;
    private static final int STATUS_FIRMWARE_UPDATE_FAILED = 507;

    private final Context appContext;
    private final Object serviceLock = new Object();
    private volatile SunmiPrinterService printerService;
    private volatile CountDownLatch bindLatch;
    private volatile String bindErrorMessage;
    private volatile boolean bindingInFlight;
    private final InnerPrinterCallback printerCallback = new InnerPrinterCallback() {
        @Override
        protected void onConnected(SunmiPrinterService service) {
            synchronized (serviceLock) {
                printerService = service;
                bindErrorMessage = null;
                bindingInFlight = false;
                if (bindLatch != null) {
                    bindLatch.countDown();
                }
            }
        }

        @Override
        protected void onDisconnected() {
            synchronized (serviceLock) {
                printerService = null;
                bindErrorMessage = "SUNMI printer service was disconnected.";
                bindingInFlight = false;
                if (bindLatch != null) {
                    bindLatch.countDown();
                }
            }
        }
    };

    SunmiPrinterEngine(Context appContext) {
        this.appContext = appContext.getApplicationContext();
    }

    void prepare() {
        SunmiPrinterService service = ensurePrinterService();
        awaitCallback("prepare", callback -> service.printerInit(callback));
    }

    StatusSnapshot getPrinterStatus() {
        SunmiPrinterService service = ensurePrinterService();

        try {
            int statusCode = service.updatePrinterState();
            return new StatusSnapshot(
                isOnlineStatus(statusCode),
                statusCode == STATUS_OUT_OF_PAPER,
                statusCode == STATUS_COVER_OPEN,
                mapStatusCodeToBridgeCode(statusCode),
                mapStatusCodeToMessage(statusCode),
                statusCode == STATUS_NORMAL ? null : statusCode
            );
        } catch (RemoteException error) {
            throw mapRemoteException("getPrinterStatus", error);
        } catch (RuntimeException error) {
            throw mapUnexpectedException("getPrinterStatus", error);
        }
    }

    void cutPaper() {
        SunmiPrinterService service = ensurePrinterService();
        awaitCallback("cutPaper", callback -> service.cutPaper(callback));
    }

    void lineWrap(int lines) {
        if (lines <= 0) {
            throw new IllegalArgumentException("lineWrap requires a positive line count");
        }

        SunmiPrinterService service = ensurePrinterService();
        awaitCallback("lineWrap", callback -> service.lineWrap(lines, callback));
    }

    void invalidate() {
        synchronized (serviceLock) {
            SunmiPrinterService currentService = printerService;
            printerService = null;
            bindErrorMessage = null;
            bindingInFlight = false;
            if (bindLatch != null) {
                bindLatch.countDown();
                bindLatch = null;
            }

            if (currentService == null) {
                return;
            }
        }

        try {
            InnerPrinterManager.getInstance().unBindService(appContext, printerCallback);
        } catch (InnerPrinterException | RuntimeException ignored) {
        }
    }

    @Override
    public void performPrintJob(int jobId, Map<String, Object> jobPayload) {
        SunmiPrintPayloadValidator.validateAndLogPrintJob(
            jobPayload,
            jobId,
            message -> Log.d(LOG_TAG, message)
        );

        performPrint("print", jobPayload);
    }

    @Override
    public void performPrintText(
        int jobId,
        String text,
        @Nullable Map<String, Object> textOptions
    ) {
        SunmiPrintPayloadValidator.validateAndLogPrintText(
            text,
            textOptions,
            jobId,
            message -> Log.d(LOG_TAG, message)
        );

        performPrint(
            "printText",
            SunmiPrintTextJobBuilder.build(text, textOptions)
        );
    }

    private void performPrint(String operation, Map<String, Object> jobPayload) {
        SunmiPrinterService service = ensurePrinterService();
        awaitCallback(operation, callback -> service.printerInit(callback));

        Object commandsValue = jobPayload.get("commands");
        if (!(commandsValue instanceof Iterable<?>)) {
            throw new IllegalArgumentException("Print job must include a commands array");
        }

        boolean cutRequested = false;
        for (Object commandValue : (Iterable<?>) commandsValue) {
            if (!(commandValue instanceof Map<?, ?>)) {
                throw new IllegalArgumentException("Print command must be an object");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> command = (Map<String, Object>) commandValue;
            if (applyCommand(service, operation, command)) {
                cutRequested = true;
            }
        }

        if (cutRequested) {
            awaitCallback(operation, callback -> service.cutPaper(callback));
        }
    }

    private boolean applyCommand(
        SunmiPrinterService service,
        String operation,
        Map<String, Object> command
    ) {
        String type = requiredString(command, "type");

        switch (type) {
            case "align":
                awaitCallback(
                    operation,
                    callback -> service.setAlignment(
                        mapAlignMode(requiredString(command, "align")),
                        callback
                    )
                );
                return false;
            case "bitmap":
                awaitCallback(
                    operation,
                    callback -> service.printBitmap(
                        decodeBitmap(requiredString(command, "base64")),
                        callback
                    )
                );
                return false;
            case "cut":
                return !"none".equalsIgnoreCase(optionalString(command, "mode"));
            case "feed":
                awaitCallback(
                    operation,
                    callback -> service.lineWrap(
                        Math.max(1, (int) Math.ceil(requiredInt(command, "pixels") / 24.0d)),
                        callback
                    )
                );
                return false;
            case "font":
                awaitCallback(
                    operation,
                    callback -> service.setFontSize(
                        mapFontSize(optionalString(command, "ascii"), optionalString(command, "ext")),
                        callback
                    )
                );
                return false;
            case "fontScale":
                applyFontScale(service, command, operation);
                return false;
            case "gray":
                applyPrinterStyle(
                    service,
                    WoyouConsts.ENABLE_BOLD,
                    requiredInt(command, "level") >= 4 ? WoyouConsts.ENABLE : WoyouConsts.DISABLE,
                    operation
                );
                return false;
            case "indent":
                applyPrinterStyle(
                    service,
                    WoyouConsts.SET_LEFT_SPACING,
                    requiredInt(command, "pixels"),
                    operation
                );
                return false;
            case "invert":
                applyPrinterStyle(
                    service,
                    WoyouConsts.ENABLE_INVERT,
                    requiredBoolean(command, "enabled") ? WoyouConsts.ENABLE : WoyouConsts.DISABLE,
                    operation
                );
                return false;
            case "spacing":
                applySpacing(service, command, operation);
                return false;
            case "text":
                awaitCallback(
                    operation,
                    callback -> service.printText(
                        requiredString(command, "text"),
                        callback
                    )
                );
                return false;
            default:
                throw new IllegalArgumentException("Unsupported print command type: " + type);
        }
    }

    private void applyFontScale(
        SunmiPrinterService service,
        Map<String, Object> command,
        String operation
    ) {
        applyPrinterStyle(
            service,
            WoyouConsts.ENABLE_DOUBLE_WIDTH,
            requiredBoolean(command, "asciiDoubleWidth") || requiredBoolean(command, "localDoubleWidth")
                ? WoyouConsts.ENABLE
                : WoyouConsts.DISABLE,
            operation
        );
        applyPrinterStyle(
            service,
            WoyouConsts.ENABLE_DOUBLE_HEIGHT,
            requiredBoolean(command, "asciiDoubleHeight") || requiredBoolean(command, "localDoubleHeight")
                ? WoyouConsts.ENABLE
                : WoyouConsts.DISABLE,
            operation
        );
    }

    private void applySpacing(
        SunmiPrinterService service,
        Map<String, Object> command,
        String operation
    ) {
        Integer wordSpace = optionalInt(command, "wordSpace");
        if (wordSpace != null) {
            applyPrinterStyle(service, WoyouConsts.SET_TEXT_RIGHT_SPACING, wordSpace, operation);
        }

        Integer lineSpace = optionalInt(command, "lineSpace");
        if (lineSpace != null) {
            applyPrinterStyle(service, WoyouConsts.SET_LINE_SPACING, lineSpace, operation);
        }
    }

    private void applyPrinterStyle(
        SunmiPrinterService service,
        int style,
        int value,
        String operation
    ) {
        try {
            service.setPrinterStyle(style, value);
        } catch (RemoteException error) {
            throw mapRemoteException(operation, error);
        } catch (RuntimeException error) {
            throw mapUnexpectedException(operation, error);
        }
    }

    private SunmiPrinterService ensurePrinterService() {
        SunmiPrinterService currentService = printerService;
        if (currentService != null) {
            return currentService;
        }

        CountDownLatch currentLatch;
        synchronized (serviceLock) {
            if (printerService != null) {
                return printerService;
            }

            if (!bindingInFlight) {
                bindLatch = new CountDownLatch(1);
                bindErrorMessage = null;
                bindingInFlight = true;

                try {
                    boolean result = InnerPrinterManager.getInstance().bindService(appContext, printerCallback);
                    if (!result) {
                        bindingInFlight = false;
                        throw new SunmiPrinterTaskException(
                            "DEVICES_ERR_CONNECT",
                            "Failed to bind the SUNMI printer service.",
                            CATEGORY_DEVICE,
                            null,
                            "DEVICES_ERR_CONNECT",
                            "Failed to bind the SUNMI printer service."
                        );
                    }
                } catch (InnerPrinterException error) {
                    bindingInFlight = false;
                    throw new SunmiPrinterTaskException(
                        "DEVICES_ERR_CONNECT",
                        error.getMessage() == null
                            ? "Failed to bind the SUNMI printer service."
                            : error.getMessage(),
                        CATEGORY_DEVICE,
                        null,
                        "DEVICES_ERR_CONNECT",
                        error.getMessage(),
                        error
                    );
                } catch (LinkageError error) {
                    bindingInFlight = false;
                    throw mapLinkageError("prepare", error);
                }
            }

            currentLatch = bindLatch;
        }

        if (currentLatch == null) {
            throw new SunmiPrinterTaskException(
                "DEVICES_ERR_CONNECT",
                "SUNMI printer connection state is invalid.",
                CATEGORY_DEVICE,
                null,
                "DEVICES_ERR_CONNECT",
                "SUNMI printer connection state is invalid."
            );
        }

        try {
            if (!currentLatch.await(BIND_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                synchronized (serviceLock) {
                    bindingInFlight = false;
                    bindLatch = null;
                }
                throw new SunmiPrinterTaskException(
                    "DEVICES_ERR_CONNECT",
                    "Timed out while connecting to the SUNMI printer service.",
                    CATEGORY_DEVICE,
                    null,
                    "DEVICES_ERR_CONNECT",
                    "Timed out while connecting to the SUNMI printer service."
                );
            }
        } catch (InterruptedException error) {
            Thread.currentThread().interrupt();
            throw new SunmiPrinterTaskException(
                "DEVICES_ERR_CONNECT",
                "Interrupted while connecting to the SUNMI printer service.",
                CATEGORY_DEVICE,
                null,
                "DEVICES_ERR_CONNECT",
                "Interrupted while connecting to the SUNMI printer service.",
                error
            );
        }

        synchronized (serviceLock) {
            bindingInFlight = false;
            bindLatch = null;

            if (printerService == null) {
                throw new SunmiPrinterTaskException(
                    "DEVICES_ERR_CONNECT",
                    bindErrorMessage == null
                        ? "The SUNMI printer service is not available on this device."
                        : bindErrorMessage,
                    CATEGORY_DEVICE,
                    null,
                    "DEVICES_ERR_CONNECT",
                    bindErrorMessage
                );
            }

            try {
                if (!InnerPrinterManager.getInstance().hasPrinter(printerService)) {
                    throw new SunmiPrinterTaskException(
                        "DEVICES_ERR_NO_SUPPORT",
                        "The SUNMI internal printer is not available on this device.",
                        CATEGORY_DEVICE,
                        STATUS_PRINTER_NOT_DETECTED,
                        "DEVICES_ERR_NO_SUPPORT",
                        "The SUNMI internal printer is not available on this device."
                    );
                }
            } catch (InnerPrinterException error) {
                throw new SunmiPrinterTaskException(
                    "DEVICES_ERR_NO_SUPPORT",
                    error.getMessage() == null
                        ? "The SUNMI internal printer is not available on this device."
                        : error.getMessage(),
                    CATEGORY_DEVICE,
                    STATUS_PRINTER_NOT_DETECTED,
                    "DEVICES_ERR_NO_SUPPORT",
                    error.getMessage(),
                    error
                );
            }

            return printerService;
        }
    }

    private void awaitCallback(
        String operation,
        PrinterCallbackAction action
    ) {
        CallbackResult callbackResult = new CallbackResult(operation);

        try {
            action.run(callbackResult.callback);
        } catch (RemoteException error) {
            throw mapRemoteException(operation, error);
        } catch (LinkageError error) {
            throw mapLinkageError(operation, error);
        } catch (RuntimeException error) {
            throw mapUnexpectedException(operation, error);
        }

        callbackResult.await();
    }

    private int mapAlignMode(String align) {
        switch (align) {
            case "center":
                return ALIGN_CENTER;
            case "right":
                return ALIGN_RIGHT;
            case "left":
            default:
                return ALIGN_LEFT;
        }
    }

    private float mapFontSize(@Nullable String ascii, @Nullable String ext) {
        String value = ascii != null ? ascii : ext;
        if (value == null) {
            return 24.0f;
        }

        switch (value) {
            case "FONT_8_16":
                return 16.0f;
            case "FONT_12_24":
                return 24.0f;
            case "FONT_16_24":
            case "FONT_16_16":
                return 26.0f;
            case "FONT_16_32":
                return 32.0f;
            case "FONT_24_24":
                return 36.0f;
            case "FONT_24_48":
            case "FONT_32_48":
            case "FONT_48_48":
                return 48.0f;
            default:
                return 24.0f;
        }
    }

    private Bitmap decodeBitmap(String base64) {
        byte[] bytes;
        try {
            bytes = Base64.decode(base64, Base64.DEFAULT);
        } catch (IllegalArgumentException error) {
            throw new IllegalArgumentException("Bitmap command base64 payload is invalid");
        }

        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        if (bitmap == null) {
            throw new IllegalArgumentException("Bitmap command could not be decoded");
        }

        return bitmap;
    }

    private boolean isOnlineStatus(int statusCode) {
        return statusCode == STATUS_NORMAL
            || statusCode == STATUS_PREPARING
            || statusCode == STATUS_CUTTER_RECOVERED
            || statusCode == STATUS_BLACK_MARK_NOT_FOUND;
    }

    private String mapStatusCodeToBridgeCode(int statusCode) {
        switch (statusCode) {
            case STATUS_OUT_OF_PAPER:
                return "PRINTER_ERR_OUT_OF_PAPER";
            case STATUS_OVERHEATED:
                return "PRINTER_ERR_PRINTER_OVER_HEATING";
            case STATUS_COVER_OPEN:
                return "PRINTER_ERR_COVER_OPEN";
            case STATUS_CUTTER_ERROR:
                return "PRINTER_ERR_CUTTER_JAM";
            case STATUS_PREPARING:
                return "PRINTER_ERR_BUSY";
            case STATUS_COMM_ERROR:
                return "DEVICES_ERR_CONNECT";
            case STATUS_PRINTER_NOT_DETECTED:
                return "DEVICES_ERR_NO_SUPPORT";
            case STATUS_FIRMWARE_UPDATE_FAILED:
                return "DEVICES_ERR_UNEXPECTED";
            case STATUS_BLACK_MARK_NOT_FOUND:
                return "PRINTER_ERR_PRINTER_PROBLEMS";
            default:
                return null;
        }
    }

    private String mapStatusCodeToMessage(int statusCode) {
        switch (statusCode) {
            case STATUS_PREPARING:
                return "The SUNMI printer is preparing.";
            case STATUS_COMM_ERROR:
                return "SUNMI printer communication is abnormal.";
            case STATUS_OUT_OF_PAPER:
                return "The SUNMI printer is out of paper.";
            case STATUS_OVERHEATED:
                return "The SUNMI printer is overheated.";
            case STATUS_COVER_OPEN:
                return "The SUNMI printer cover is open.";
            case STATUS_CUTTER_ERROR:
                return "The SUNMI printer cutter is abnormal.";
            case STATUS_BLACK_MARK_NOT_FOUND:
                return "The SUNMI printer black mark was not detected.";
            case STATUS_PRINTER_NOT_DETECTED:
                return "The SUNMI internal printer was not detected.";
            case STATUS_FIRMWARE_UPDATE_FAILED:
                return "The SUNMI printer firmware update failed.";
            default:
                return null;
        }
    }

    private SunmiPrinterTaskException mapRemoteException(String operation, RemoteException error) {
        return new SunmiPrinterTaskException(
            "DEVICES_ERR_CONNECT",
            error.getMessage() == null
                ? "SUNMI printer communication failed."
                : error.getMessage(),
            CATEGORY_DEVICE,
            null,
            "DEVICES_ERR_CONNECT",
            error.getMessage(),
            error
        );
    }

    private SunmiPrinterTaskException mapLinkageError(String operation, LinkageError error) {
        String message = error.getMessage() == null || error.getMessage().isEmpty()
            ? "The SUNMI printer SDK dependency is not integrated."
            : error.getMessage();

        return new SunmiPrinterTaskException(
            "SUNMI_SDK_NOT_INTEGRATED",
            message,
            CATEGORY_INTEGRATION,
            null,
            "SUNMI_SDK_NOT_INTEGRATED",
            message,
            error
        );
    }

    private SunmiPrinterTaskException mapUnexpectedException(
        String operation,
        RuntimeException error
    ) {
        return new SunmiPrinterTaskException(
            "DEVICES_ERR_UNEXPECTED",
            error.getMessage() == null ? "Unexpected SUNMI printer error." : error.getMessage(),
            CATEGORY_DEVICE,
            null,
            "DEVICES_ERR_UNEXPECTED",
            error.getMessage(),
            error
        );
    }

    private String requiredString(Map<String, Object> command, String key) {
        Object value = command.get(key);
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Print command has invalid string field: " + key);
        }
        return (String) value;
    }

    private int requiredInt(Map<String, Object> command, String key) {
        Object value = command.get(key);
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException("Print command has invalid number field: " + key);
        }
        return ((Number) value).intValue();
    }

    private boolean requiredBoolean(Map<String, Object> command, String key) {
        Object value = command.get(key);
        if (!(value instanceof Boolean)) {
            throw new IllegalArgumentException("Print command has invalid boolean field: " + key);
        }
        return (Boolean) value;
    }

    @Nullable
    private Integer optionalInt(Map<String, Object> command, String key) {
        Object value = command.get(key);
        if (value == null) {
            return null;
        }
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException("Print command has invalid number field: " + key);
        }
        return ((Number) value).intValue();
    }

    @Nullable
    private String optionalString(Map<String, Object> command, String key) {
        Object value = command.get(key);
        if (value == null) {
            return null;
        }
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Print command has invalid string field: " + key);
        }
        return (String) value;
    }

    static final class StatusSnapshot {
        final boolean online;
        final boolean paperEmpty;
        final boolean coverOpen;
        final String errorCode;
        final String errorMessage;
        final Integer nativeStatusCode;

        StatusSnapshot(
            boolean online,
            boolean paperEmpty,
            boolean coverOpen,
            @Nullable String errorCode,
            @Nullable String errorMessage,
            @Nullable Integer nativeStatusCode
        ) {
            this.online = online;
            this.paperEmpty = paperEmpty;
            this.coverOpen = coverOpen;
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
            this.nativeStatusCode = nativeStatusCode;
        }
    }

    private interface PrinterCallbackAction {
        void run(InnerResultCallback callback) throws RemoteException;
    }

    private final class CallbackResult {
        private final CountDownLatch latch = new CountDownLatch(1);
        private final AtomicBoolean completed = new AtomicBoolean(false);
        private final String operation;
        private volatile SunmiPrinterTaskException error;
        private final InnerResultCallback callback = new InnerResultCallback() {
            @Override
            public void onRunResult(boolean isSuccess) {
                if (!isSuccess) {
                    error = new SunmiPrinterTaskException(
                        "DEVICES_ERR_UNEXPECTED",
                        "SUNMI printer operation failed.",
                        CATEGORY_DEVICE,
                        null,
                        "DEVICES_ERR_UNEXPECTED",
                        "SUNMI printer operation failed."
                    );
                }
                complete();
            }

            @Override
            public void onReturnString(String result) {
                complete();
            }

            @Override
            public void onRaiseException(int code, String msg) {
                error = new SunmiPrinterTaskException(
                    mapStatusCodeToBridgeCode(code) == null
                        ? "DEVICES_ERR_UNEXPECTED"
                        : mapStatusCodeToBridgeCode(code),
                    msg == null || msg.isEmpty()
                        ? "SUNMI printer operation raised an exception."
                        : msg,
                    CATEGORY_PRINTER,
                    code,
                    Integer.toString(code),
                    msg
                );
                complete();
            }

            @Override
            public void onPrintResult(int code, String msg) {
                if (code != 0 && code != STATUS_NORMAL) {
                    error = new SunmiPrinterTaskException(
                        mapStatusCodeToBridgeCode(code) == null
                            ? "UNKNOWN_NATIVE_PRINTER_RESULT"
                            : mapStatusCodeToBridgeCode(code),
                        msg == null || msg.isEmpty()
                            ? "SUNMI printer returned a print result error."
                            : msg,
                        CATEGORY_PRINTER,
                        code,
                        Integer.toString(code),
                        msg
                    );
                }
                complete();
            }
        };

        CallbackResult(String operation) {
            this.operation = operation;
        }

        void await() {
            try {
                if (!latch.await(CALLBACK_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                    throw new SunmiPrinterTaskException(
                        "DEVICES_ERR_CONNECT",
                        "Timed out while waiting for the SUNMI printer callback.",
                        CATEGORY_DEVICE,
                        null,
                        "DEVICES_ERR_CONNECT",
                        "Timed out while waiting for the SUNMI printer callback."
                    );
                }
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                throw new SunmiPrinterTaskException(
                    "DEVICES_ERR_CONNECT",
                    "Interrupted while waiting for the SUNMI printer callback.",
                    CATEGORY_DEVICE,
                    null,
                    "DEVICES_ERR_CONNECT",
                    "Interrupted while waiting for the SUNMI printer callback.",
                    interruptedException
                );
            }

            if (error != null) {
                throw error;
            }
        }

        private void complete() {
            if (completed.compareAndSet(false, true)) {
                latch.countDown();
            }
        }
    }
}
