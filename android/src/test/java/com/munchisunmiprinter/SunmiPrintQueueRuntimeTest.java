package com.munchisunmiprinter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public class SunmiPrintQueueRuntimeTest {
    @Test
    public void runsQueuedJobsInFifoOrder() {
        List<String> callOrder = new ArrayList<>();
        SunmiPrintQueueRuntime runtime = new SunmiPrintQueueRuntime(
            directExecutor(),
            new SunmiPrintQueueRuntime.TaskProcessor() {
                @Override
                public void performPrintJob(int jobId, Map<String, Object> jobPayload) {
                    callOrder.add("job-" + jobId);
                }

                @Override
                public void performPrintText(int jobId, String text, Map<String, Object> textOptions) {
                    callOrder.add("text-" + jobId + ":" + text);
                }
            }
        );
        runtime.markPrepared();

        RecordingCallback first = new RecordingCallback();
        RecordingCallback second = new RecordingCallback();
        RecordingCallback third = new RecordingCallback();

        runtime.enqueuePrintText("A", null, first);
        runtime.enqueuePrintJob(validPrintJob(), second);
        runtime.enqueuePrintText("B", null, third);

        assertEquals(
            Arrays.asList("text-1:A", "job-2", "text-3:B"),
            callOrder
        );
        assertTrue(first.resolved);
        assertTrue(second.resolved);
        assertTrue(third.resolved);
    }

    @Test
    public void rejectsQueuedJobsWhenNotPrepared() {
        SunmiPrintQueueRuntime runtime = new SunmiPrintQueueRuntime(
            directExecutor(),
            new SunmiPrintQueueRuntime.TaskProcessor() {
                @Override
                public void performPrintJob(int jobId, Map<String, Object> jobPayload) {
                    throw new AssertionError("Should not execute print job before prepare");
                }

                @Override
                public void performPrintText(int jobId, String text, Map<String, Object> textOptions) {
                    throw new AssertionError("Should not execute print text before prepare");
                }
            }
        );

        RecordingCallback callback = new RecordingCallback();
        runtime.enqueuePrintText("A", null, callback);

        assertEquals(SunmiPrintQueueRuntime.NOT_PREPARED, callback.code);
        assertEquals(SunmiPrintQueueRuntime.CATEGORY_INTEGRATION, callback.category);
        assertEquals(SunmiPrintQueueRuntime.OPERATION_PRINT_TEXT, callback.operation);
        assertEquals("Call prepare() successfully before printing.", callback.message);
        assertTrue(!callback.resolved);
    }

    @Test
    public void continuesQueueAfterTaskFailure() {
        List<String> callOrder = new ArrayList<>();
        SunmiPrintQueueRuntime runtime = new SunmiPrintQueueRuntime(
            directExecutor(),
            new SunmiPrintQueueRuntime.TaskProcessor() {
                @Override
                public void performPrintJob(int jobId, Map<String, Object> jobPayload) {
                    callOrder.add("job-" + jobId);
                    throw new IllegalArgumentException("bad payload");
                }

                @Override
                public void performPrintText(int jobId, String text, Map<String, Object> textOptions) {
                    callOrder.add("text-" + jobId);
                }
            }
        );
        runtime.markPrepared();

        RecordingCallback first = new RecordingCallback();
        RecordingCallback second = new RecordingCallback();

        runtime.enqueuePrintJob(validPrintJob(), first);
        runtime.enqueuePrintText("B", null, second);

        assertEquals(Arrays.asList("job-1", "text-2"), callOrder);
        assertEquals(SunmiPrintQueueRuntime.DEVICES_ERR_INVALID_ARGUMENT, first.code);
        assertTrue(second.resolved);
    }

    @Test
    public void invalidationRejectsPendingJobs() {
        DeferredExecutor executor = new DeferredExecutor();
        SunmiPrintQueueRuntime runtime = new SunmiPrintQueueRuntime(
            executor,
            new SunmiPrintQueueRuntime.TaskProcessor() {
                @Override
                public void performPrintJob(int jobId, Map<String, Object> jobPayload) {
                    throw new AssertionError("Should not execute invalidated job");
                }

                @Override
                public void performPrintText(int jobId, String text, Map<String, Object> textOptions) {
                    throw new AssertionError("Should not execute invalidated job");
                }
            }
        );
        runtime.markPrepared();

        RecordingCallback first = new RecordingCallback();
        RecordingCallback second = new RecordingCallback();

        runtime.enqueuePrintText("A", null, first);
        runtime.enqueuePrintJob(validPrintJob(), second);
        runtime.invalidate();
        executor.runAll();

        assertEquals(SunmiPrintQueueRuntime.DEVICES_ERR_UNEXPECTED, first.code);
        assertEquals(SunmiPrintQueueRuntime.DEVICES_ERR_UNEXPECTED, second.code);
        assertEquals(
            "SUNMI printer module was invalidated before the queued job could run.",
            first.message
        );
        assertNull(first.throwable);
    }

    private static Map<String, Object> validPrintJob() {
        Map<String, Object> textCommand = new HashMap<>();
        textCommand.put("type", "text");
        textCommand.put("text", "hello");

        List<Map<String, Object>> commands = new ArrayList<>();
        commands.add(textCommand);

        Map<String, Object> job = new HashMap<>();
        job.put("commands", commands);
        return job;
    }

    private static Executor directExecutor() {
        return Runnable::run;
    }

    private static final class RecordingCallback implements SunmiPrintQueueRuntime.TaskResultCallback {
        boolean resolved;
        String operation;
        String code;
        String message;
        String category;
        Throwable throwable;

        @Override
        public void onResolved() {
            resolved = true;
        }

        @Override
        public void onRejected(
            String operation,
            String code,
            String message,
            String category,
            Throwable throwable
        ) {
            this.operation = operation;
            this.code = code;
            this.message = message;
            this.category = category;
            this.throwable = throwable;
        }
    }

    private static final class DeferredExecutor implements Executor {
        private final List<Runnable> tasks = new ArrayList<>();

        @Override
        public void execute(Runnable command) {
            tasks.add(command);
        }

        void runAll() {
            List<Runnable> snapshot = new ArrayList<>(tasks);
            tasks.clear();
            for (Runnable task : snapshot) {
                task.run();
            }
        }
    }
}
