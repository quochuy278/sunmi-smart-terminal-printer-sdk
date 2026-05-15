package com.munchisunmiprinter;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MunchiSunmiPrinterModule extends ReactContextBaseJavaModule {
    private static final String CATEGORY_INTEGRATION = "INTEGRATION";
    private static final String OPERATION_CUT_PAPER = "cutPaper";
    private static final String SUNMI_SDK_NOT_INTEGRATED = "SUNMI_SDK_NOT_INTEGRATED";
    private final ExecutorService printerExecutor = Executors.newSingleThreadExecutor();
    private final SunmiPrinterEngine printerEngine;
    private final SunmiPrintQueueRuntime printQueueRuntime = new SunmiPrintQueueRuntime(
        printerExecutor,
        new SunmiPrintQueueRuntime.TaskProcessor() {
            @Override
            public void performPrintJob(int jobId, Map<String, Object> jobPayload) {
                printerEngine.performPrintJob(jobId, jobPayload);
            }

            @Override
            public void performPrintText(
                int jobId,
                String text,
                Map<String, Object> textOptions
            ) {
                printerEngine.performPrintText(jobId, text, textOptions);
            }
        }
    );

    public MunchiSunmiPrinterModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.printerEngine = new SunmiPrinterEngine(reactContext);
    }

    @NonNull
    @Override
    public String getName() {
        return "MunchiSunmiPrinter";
    }

    @ReactMethod
    public void initialize(Promise promise) {
        printerExecutor.execute(() -> {
            try {
                printerEngine.prepare();
                printQueueRuntime.markPrepared();
                promise.resolve(true);
            } catch (SunmiPrinterTaskException error) {
                printQueueRuntime.resetPrepared();
                rejectPrinterError(
                    promise,
                    "initialize",
                    error.code,
                    error.getMessage() == null ? "Unexpected SUNMI printer error" : error.getMessage(),
                    error.category,
                    error
                );
            } catch (LinkageError error) {
                printQueueRuntime.resetPrepared();
                rejectPrinterError(
                    promise,
                    "initialize",
                    SUNMI_SDK_NOT_INTEGRATED,
                    error.getMessage() == null
                        ? "The SUNMI printer SDK dependency is not integrated."
                        : error.getMessage(),
                    CATEGORY_INTEGRATION,
                    error
                );
            } catch (Throwable error) {
                printQueueRuntime.resetPrepared();
                rejectUnexpectedError(promise, "initialize", error);
            }
        });
    }

    @ReactMethod
    public void prepare(Promise promise) {
        printerExecutor.execute(() -> {
            try {
                printerEngine.prepare();
                printQueueRuntime.markPrepared();
                promise.resolve(true);
            } catch (SunmiPrinterTaskException error) {
                printQueueRuntime.resetPrepared();
                rejectPrinterError(
                    promise,
                    "prepare",
                    error.code,
                    error.getMessage() == null ? "Unexpected SUNMI printer error" : error.getMessage(),
                    error.category,
                    error
                );
            } catch (LinkageError error) {
                printQueueRuntime.resetPrepared();
                rejectPrinterError(
                    promise,
                    "prepare",
                    SUNMI_SDK_NOT_INTEGRATED,
                    error.getMessage() == null
                        ? "The SUNMI printer SDK dependency is not integrated."
                        : error.getMessage(),
                    CATEGORY_INTEGRATION,
                    error
                );
            } catch (Throwable error) {
                printQueueRuntime.resetPrepared();
                rejectUnexpectedError(promise, "prepare", error);
            }
        });
    }

    @ReactMethod
    public void getDeviceInfo(Promise promise) {
        WritableMap map = Arguments.createMap();
        map.putString("manufacturer", Build.MANUFACTURER == null ? "" : Build.MANUFACTURER);
        map.putString("brand", Build.BRAND == null ? "" : Build.BRAND);
        map.putString("model", Build.MODEL == null ? "" : Build.MODEL);
        map.putInt("sdkInt", Build.VERSION.SDK_INT);
        promise.resolve(map);
    }

    @ReactMethod
    public void getPrinterStatus(Promise promise) {
        printerExecutor.execute(() -> {
            try {
                SunmiPrinterEngine.StatusSnapshot statusSnapshot = printerEngine.getPrinterStatus();
                WritableMap map = Arguments.createMap();
                map.putBoolean("online", statusSnapshot.online);
                map.putBoolean("paperEmpty", statusSnapshot.paperEmpty);
                map.putBoolean("coverOpen", statusSnapshot.coverOpen);
                if (statusSnapshot.errorCode == null) {
                    map.putNull("errorCode");
                    map.putNull("errorMessage");
                } else {
                    map.putString("errorCode", statusSnapshot.errorCode);
                    map.putString("errorMessage", statusSnapshot.errorMessage);
                }
                if (statusSnapshot.nativeStatusCode == null) {
                    map.putNull("nativeStatusCode");
                } else {
                    map.putInt("nativeStatusCode", statusSnapshot.nativeStatusCode);
                }
                promise.resolve(map);
            } catch (SunmiPrinterTaskException error) {
                rejectPrinterError(
                    promise,
                    "getPrinterStatus",
                    error.code,
                    error.getMessage() == null ? "Unexpected SUNMI printer error" : error.getMessage(),
                    error.category,
                    error
                );
            } catch (LinkageError error) {
                rejectPrinterError(
                    promise,
                    "getPrinterStatus",
                    SUNMI_SDK_NOT_INTEGRATED,
                    error.getMessage() == null
                        ? "The SUNMI printer SDK dependency is not integrated."
                        : error.getMessage(),
                    CATEGORY_INTEGRATION,
                    error
                );
            } catch (Throwable error) {
                rejectUnexpectedError(promise, "getPrinterStatus", error);
            }
        });
    }

    @ReactMethod
    public void printText(String text, @Nullable ReadableMap options, Promise promise) {
        printQueueRuntime.enqueuePrintText(
            text,
            options == null ? null : options.toHashMap(),
            createTaskResultCallback(promise)
        );
    }

    @ReactMethod
    public void print(ReadableMap job, Promise promise) {
        printQueueRuntime.enqueuePrintJob(
            job.toHashMap(),
            createTaskResultCallback(promise)
        );
    }

    @ReactMethod
    public void cutPaper(Promise promise) {
        printerExecutor.execute(() -> {
            try {
                printerEngine.cutPaper();
                promise.resolve(true);
            } catch (SunmiPrinterTaskException error) {
                rejectPrinterError(
                    promise,
                    OPERATION_CUT_PAPER,
                    error.code,
                    error.getMessage() == null ? "Unexpected SUNMI printer error" : error.getMessage(),
                    error.category,
                    error
                );
            } catch (LinkageError error) {
                rejectPrinterError(
                    promise,
                    OPERATION_CUT_PAPER,
                    SUNMI_SDK_NOT_INTEGRATED,
                    error.getMessage() == null
                        ? "The SUNMI printer SDK dependency is not integrated."
                        : error.getMessage(),
                    CATEGORY_INTEGRATION,
                    error
                );
            } catch (Throwable error) {
                rejectUnexpectedError(promise, OPERATION_CUT_PAPER, error);
            }
        });
    }

    @ReactMethod
    public void lineWrap(double lines, Promise promise) {
        printerExecutor.execute(() -> {
            try {
                printerEngine.lineWrap((int) lines);
                promise.resolve(true);
            } catch (SunmiPrinterTaskException error) {
                rejectPrinterError(
                    promise,
                    "lineWrap",
                    error.code,
                    error.getMessage() == null ? "Unexpected SUNMI printer error" : error.getMessage(),
                    error.category,
                    error
                );
            } catch (IllegalArgumentException error) {
                rejectPrinterError(
                    promise,
                    "lineWrap",
                    "DEVICES_ERR_INVALID_ARGUMENT",
                    error.getMessage() == null ? "Invalid SUNMI lineWrap arguments" : error.getMessage(),
                    "DEVICE",
                    error
                );
            } catch (LinkageError error) {
                rejectPrinterError(
                    promise,
                    "lineWrap",
                    SUNMI_SDK_NOT_INTEGRATED,
                    error.getMessage() == null
                        ? "The SUNMI printer SDK dependency is not integrated."
                        : error.getMessage(),
                    CATEGORY_INTEGRATION,
                    error
                );
            } catch (Throwable error) {
                rejectUnexpectedError(promise, "lineWrap", error);
            }
        });
    }

    @Override
    public void invalidate() {
        printQueueRuntime.invalidate();
        printerEngine.invalidate();
        printerExecutor.shutdownNow();
        super.invalidate();
    }

    private SunmiPrintQueueRuntime.TaskResultCallback createTaskResultCallback(
        Promise promise
    ) {
        return new SunmiPrintQueueRuntime.TaskResultCallback() {
            @Override
            public void onResolved() {
                promise.resolve(true);
            }

            @Override
            public void onRejected(
                String operation,
                String code,
                String message,
                String category,
                Throwable throwable
            ) {
                rejectPrinterError(
                    promise,
                    operation,
                    code,
                    message,
                    category,
                    throwable
                );
            }
        };
    }

    private void rejectPrinterError(
        Promise promise,
        String operation,
        String code,
        String message,
        String category,
        Throwable throwable
    ) {
        WritableMap userInfo = Arguments.createMap();
        userInfo.putString("operation", operation);
        userInfo.putString("category", category);
        userInfo.putString("nativeCode", code);
        userInfo.putString("nativeMessage", message);
        if (throwable instanceof SunmiPrinterTaskException) {
            SunmiPrinterTaskException taskException = (SunmiPrinterTaskException) throwable;
            if (taskException.nativeStatusCode == null) {
                userInfo.putNull("nativeStatusCode");
            } else {
                userInfo.putInt("nativeStatusCode", taskException.nativeStatusCode);
            }
            userInfo.putBoolean("recoverable", isRecoverable(taskException.code));
            userInfo.putBoolean("retryable", isRetryable(taskException.code));
        } else {
            userInfo.putNull("nativeStatusCode");
            userInfo.putBoolean("recoverable", false);
            userInfo.putBoolean("retryable", false);
        }

        promise.reject(code, message, throwable, userInfo);
    }

    private boolean isRecoverable(String code) {
        return !"SUNMI_SDK_NOT_INTEGRATED".equals(code)
            && !"NATIVE_MODULE_NOT_LINKED".equals(code)
            && !"UNSUPPORTED_PLATFORM".equals(code);
    }

    private boolean isRetryable(String code) {
        return "DEVICES_ERR_CONNECT".equals(code)
            || "PRINTER_ERR_BUSY".equals(code)
            || "PRINTER_ERR_PRINT_UNFINISHED".equals(code)
            || "PRINTER_ERR_PRINTER_OVER_HEATING".equals(code)
            || "PRINTER_ERR_VOLTAGE_TOO_LOW".equals(code);
    }

    private void rejectUnexpectedError(
        Promise promise,
        String operation,
        Throwable throwable
    ) {
        rejectPrinterError(
            promise,
            operation,
            "DEVICES_ERR_UNEXPECTED",
            throwable.getMessage() == null
                ? "Unexpected SUNMI printer error"
                : throwable.getMessage(),
            "DEVICE",
            throwable
        );
    }
}
