package com.munchisunmiprinter;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

final class SunmiPrintQueueRuntime {
    static final String CATEGORY_DEVICE = "DEVICE";
    static final String CATEGORY_INTEGRATION = "INTEGRATION";
    static final String OPERATION_PRINT = "print";
    static final String OPERATION_PRINT_TEXT = "printText";
    static final String DEVICES_ERR_INVALID_ARGUMENT = "DEVICES_ERR_INVALID_ARGUMENT";
    static final String DEVICES_ERR_UNEXPECTED = "DEVICES_ERR_UNEXPECTED";
    static final String NOT_PREPARED = "NOT_PREPARED";

    interface TaskProcessor {
        void performPrintJob(int jobId, Map<String, Object> jobPayload);

        void performPrintText(int jobId, String text, Map<String, Object> textOptions);
    }

    interface TaskResultCallback {
        void onResolved();

        void onRejected(
            String operation,
            String code,
            String message,
            String category,
            Throwable throwable
        );
    }

    private final Object lock = new Object();
    private final Executor executor;
    private final TaskProcessor taskProcessor;
    private final ArrayDeque<QueuedPrintTask> printQueue = new ArrayDeque<>();
    private final AtomicInteger nextPrintJobId = new AtomicInteger(1);
    private boolean isPrintQueueRunning = false;
    private volatile boolean isPrepared = false;

    SunmiPrintQueueRuntime(Executor executor, TaskProcessor taskProcessor) {
        this.executor = executor;
        this.taskProcessor = taskProcessor;
    }

    void markPrepared() {
        isPrepared = true;
    }

    void resetPrepared() {
        isPrepared = false;
    }

    void enqueuePrintJob(Map<String, Object> jobPayload, TaskResultCallback callback) {
        enqueuePrintTask(
            QueuedPrintTask.forJob(
                nextPrintJobId.getAndIncrement(),
                jobPayload,
                callback
            )
        );
    }

    void enqueuePrintText(
        String text,
        Map<String, Object> textOptions,
        TaskResultCallback callback
    ) {
        enqueuePrintTask(
            QueuedPrintTask.forText(
                nextPrintJobId.getAndIncrement(),
                text,
                textOptions,
                callback
            )
        );
    }

    void invalidate() {
        synchronized (lock) {
            while (!printQueue.isEmpty()) {
                QueuedPrintTask queuedPrintTask = printQueue.poll();
                if (queuedPrintTask == null) {
                    continue;
                }

                queuedPrintTask.callback.onRejected(
                    queuedPrintTask.operation,
                    DEVICES_ERR_UNEXPECTED,
                    "SUNMI printer module was invalidated before the queued job could run.",
                    CATEGORY_DEVICE,
                    null
                );
            }
            isPrintQueueRunning = false;
            isPrepared = false;
        }
    }

    private void enqueuePrintTask(QueuedPrintTask queuedPrintTask) {
        synchronized (lock) {
            printQueue.add(queuedPrintTask);

            if (isPrintQueueRunning) {
                return;
            }

            isPrintQueueRunning = true;
        }

        executor.execute(this::drainPrintQueue);
    }

    private void drainPrintQueue() {
        while (true) {
            QueuedPrintTask queuedPrintTask;

            synchronized (lock) {
                queuedPrintTask = printQueue.poll();
                if (queuedPrintTask == null) {
                    isPrintQueueRunning = false;
                    return;
                }
            }

            processQueuedPrintTask(queuedPrintTask);
        }
    }

    private void processQueuedPrintTask(QueuedPrintTask queuedPrintTask) {
        if (!isPrepared) {
            queuedPrintTask.callback.onRejected(
                queuedPrintTask.operation,
                NOT_PREPARED,
                "Call prepare() successfully before printing.",
                CATEGORY_INTEGRATION,
                null
            );
            return;
        }

        try {
            if (OPERATION_PRINT.equals(queuedPrintTask.operation)) {
                taskProcessor.performPrintJob(
                    queuedPrintTask.jobId,
                    queuedPrintTask.jobPayload
                );
            } else if (OPERATION_PRINT_TEXT.equals(queuedPrintTask.operation)) {
                taskProcessor.performPrintText(
                    queuedPrintTask.jobId,
                    queuedPrintTask.text,
                    queuedPrintTask.textOptions
                );
            }

            queuedPrintTask.callback.onResolved();
        } catch (SunmiPrinterTaskException error) {
            queuedPrintTask.callback.onRejected(
                queuedPrintTask.operation,
                error.code,
                error.getMessage() == null ? "Unexpected SUNMI printer error" : error.getMessage(),
                error.category,
                error
            );
        } catch (IllegalArgumentException error) {
            queuedPrintTask.callback.onRejected(
                queuedPrintTask.operation,
                DEVICES_ERR_INVALID_ARGUMENT,
                error.getMessage() == null ? "Invalid print payload" : error.getMessage(),
                CATEGORY_DEVICE,
                error
            );
        } catch (RuntimeException error) {
            queuedPrintTask.callback.onRejected(
                queuedPrintTask.operation,
                DEVICES_ERR_UNEXPECTED,
                error.getMessage() == null ? "Unexpected SUNMI printer error" : error.getMessage(),
                CATEGORY_DEVICE,
                error
            );
        } catch (Throwable error) {
            queuedPrintTask.callback.onRejected(
                queuedPrintTask.operation,
                error instanceof LinkageError
                    ? "SUNMI_SDK_NOT_INTEGRATED"
                    : DEVICES_ERR_UNEXPECTED,
                error.getMessage() == null
                    ? (error instanceof LinkageError
                        ? "The SUNMI printer SDK dependency is not integrated."
                        : "Unexpected SUNMI printer error")
                    : error.getMessage(),
                error instanceof LinkageError ? CATEGORY_INTEGRATION : CATEGORY_DEVICE,
                error
            );
        }
    }

    private static final class QueuedPrintTask {
        final int jobId;
        final String operation;
        final Map<String, Object> jobPayload;
        final String text;
        final Map<String, Object> textOptions;
        final TaskResultCallback callback;

        private QueuedPrintTask(
            int jobId,
            String operation,
            Map<String, Object> jobPayload,
            String text,
            Map<String, Object> textOptions,
            TaskResultCallback callback
        ) {
            this.jobId = jobId;
            this.operation = operation;
            this.jobPayload = jobPayload;
            this.text = text;
            this.textOptions = textOptions;
            this.callback = callback;
        }

        static QueuedPrintTask forJob(
            int jobId,
            Map<String, Object> jobPayload,
            TaskResultCallback callback
        ) {
            return new QueuedPrintTask(
                jobId,
                OPERATION_PRINT,
                jobPayload,
                null,
                null,
                callback
            );
        }

        static QueuedPrintTask forText(
            int jobId,
            String text,
            Map<String, Object> textOptions,
            TaskResultCallback callback
        ) {
            return new QueuedPrintTask(
                jobId,
                OPERATION_PRINT_TEXT,
                null,
                text,
                textOptions,
                callback
            );
        }
    }
}
