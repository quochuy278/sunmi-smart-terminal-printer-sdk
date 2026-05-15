package com.munchisunmiprinter;

import java.util.List;
import java.util.Map;

final class SunmiPrintPayloadValidator {
    interface DebugLogger {
        void log(String message);
    }

    private SunmiPrintPayloadValidator() {}

    static void validateAndLogPrintJob(
        Map<String, Object> jobPayload,
        int jobId,
        DebugLogger debugLogger
    ) {
        if (jobPayload == null) {
            throw new IllegalArgumentException("Print job must be an object");
        }

        Object commandsValue = jobPayload.get("commands");
        if (!(commandsValue instanceof List<?>)) {
            throw new IllegalArgumentException("Print job must include a commands array");
        }

        List<?> commands = (List<?>) commandsValue;
        for (int index = 0; index < commands.size(); index++) {
            Object commandValue = commands.get(index);
            if (!(commandValue instanceof Map<?, ?>)) {
                throw new IllegalArgumentException("Print command at index " + index + " must be an object");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> command = (Map<String, Object>) commandValue;
            validateAndLogPrintCommand(command, jobId, index, debugLogger);
        }
    }

    static void validateAndLogPrintText(
        String text,
        Map<String, Object> options,
        int jobId,
        DebugLogger debugLogger
    ) {
        if (text == null) {
            throw new IllegalArgumentException("printText requires a text value");
        }

        String align = null;
        if (options != null && options.containsKey("align")) {
            align = optionalString(options, "align");
        }

        debugLogger.log(
            "printText() job#" + jobId
                + " textLength=" + text.length()
                + " align=" + align
                + " options=" + options
        );
    }

    private static void validateAndLogPrintCommand(
        Map<String, Object> command,
        int jobId,
        int index,
        DebugLogger debugLogger
    ) {
        String type = requireString(command, "type", index);
        if (type.isEmpty()) {
            throw new IllegalArgumentException("Print command at index " + index + " is missing type");
        }

        switch (type) {
            case "align":
                debugLogger.log(
                    "print() job#" + jobId + " command[" + index + "] type=align align="
                        + requireString(command, "align", index)
                );
                return;
            case "bitmap":
                debugLogger.log(
                    "print() job#" + jobId + " command[" + index + "] type=bitmap base64Length="
                        + requireString(command, "base64", index).length()
                );
                return;
            case "cut":
                debugLogger.log(
                    "print() job#" + jobId + " command[" + index + "] type=cut mode="
                        + optionalString(command, "mode")
                );
                return;
            case "feed":
                debugLogger.log(
                    "print() job#" + jobId + " command[" + index + "] type=feed pixels="
                        + requireInt(command, "pixels", index)
                );
                return;
            case "font":
                debugLogger.log(
                    "print() job#" + jobId + " command[" + index + "] type=font ascii="
                        + requireString(command, "ascii", index)
                        + " ext=" + requireString(command, "ext", index)
                );
                return;
            case "fontScale":
                debugLogger.log(
                    "print() job#" + jobId + " command[" + index + "] type=fontScale"
                );
                return;
            case "gray":
                debugLogger.log(
                    "print() job#" + jobId + " command[" + index + "] type=gray level="
                        + requireInt(command, "level", index)
                );
                return;
            case "indent":
                debugLogger.log(
                    "print() job#" + jobId + " command[" + index + "] type=indent pixels="
                        + requireInt(command, "pixels", index)
                );
                return;
            case "invert":
                debugLogger.log(
                    "print() job#" + jobId + " command[" + index + "] type=invert enabled="
                        + requireBoolean(command, "enabled", index)
                );
                return;
            case "spacing":
                debugLogger.log(
                    "print() job#" + jobId + " command[" + index + "] type=spacing wordSpace="
                        + optionalInt(command, "wordSpace")
                        + " lineSpace=" + optionalInt(command, "lineSpace")
                );
                return;
            case "text":
                debugLogger.log(
                    "print() job#" + jobId + " command[" + index + "] type=text textLength="
                        + requireString(command, "text", index).length()
                );
                return;
            default:
                throw new IllegalArgumentException("Unsupported print command type: " + type);
        }
    }

    private static String requireString(Map<String, Object> command, String key, int index) {
        if (!command.containsKey(key)) {
            throw new IllegalArgumentException("Print command at index " + index + " is missing string field: " + key);
        }

        Object value = command.get(key);
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Print command at index " + index + " has invalid string field: " + key);
        }
        return (String) value;
    }

    private static int requireInt(Map<String, Object> command, String key, int index) {
        if (!command.containsKey(key)) {
            throw new IllegalArgumentException("Print command at index " + index + " is missing number field: " + key);
        }

        Object value = command.get(key);
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException("Print command at index " + index + " has invalid number field: " + key);
        }

        return ((Number) value).intValue();
    }

    private static boolean requireBoolean(Map<String, Object> command, String key, int index) {
        if (!command.containsKey(key)) {
            throw new IllegalArgumentException("Print command at index " + index + " is missing boolean field: " + key);
        }

        Object value = command.get(key);
        if (!(value instanceof Boolean)) {
            throw new IllegalArgumentException("Print command at index " + index + " has invalid boolean field: " + key);
        }

        return (Boolean) value;
    }

    private static Integer optionalInt(Map<String, Object> command, String key) {
        if (!command.containsKey(key) || command.get(key) == null) {
            return null;
        }

        Object value = command.get(key);
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException("Print command has invalid number field: " + key);
        }

        return ((Number) value).intValue();
    }

    private static String optionalString(Map<String, Object> command, String key) {
        if (!command.containsKey(key) || command.get(key) == null) {
            return null;
        }

        Object value = command.get(key);
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("Print command has invalid string field: " + key);
        }

        return (String) value;
    }
}
