package com.munchisunmiprinter;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

final class SunmiPrintTextJobBuilder {
    private static final int BOLD_GRAY_LEVEL = 4;
    private static final String DEFAULT_CHARSET = "UTF-8";

    private SunmiPrintTextJobBuilder() {}

    static Map<String, Object> build(String text, @Nullable Map<String, Object> options) {
        if (text == null) {
            throw new IllegalArgumentException("printText requires a text value");
        }

        ArrayList<Map<String, Object>> commands = new ArrayList<>();
        if (options != null) {
            String align = optionalString(options, "align");
            if (align != null) {
                commands.add(createAlignCommand(align));
            }

            Integer fontSize = optionalInt(options, "fontSize");
            if (fontSize != null) {
                commands.add(createFontCommand(fontSize));
            }

            if (optionalBoolean(options, "bold", false)) {
                commands.add(createGrayCommand(BOLD_GRAY_LEVEL));
            }
        }

        commands.add(createTextCommand(text));

        HashMap<String, Object> jobPayload = new HashMap<>();
        jobPayload.put("commands", commands);
        return jobPayload;
    }

    private static Map<String, Object> createAlignCommand(String align) {
        HashMap<String, Object> command = new HashMap<>();
        command.put("type", "align");
        command.put("align", align);
        return command;
    }

    private static Map<String, Object> createGrayCommand(int level) {
        HashMap<String, Object> command = new HashMap<>();
        command.put("type", "gray");
        command.put("level", level);
        return command;
    }

    private static Map<String, Object> createTextCommand(String text) {
        HashMap<String, Object> command = new HashMap<>();
        command.put("type", "text");
        command.put("text", text);
        command.put("charset", DEFAULT_CHARSET);
        return command;
    }

    private static Map<String, Object> createFontCommand(int fontSize) {
        if (fontSize <= 0) {
            throw new IllegalArgumentException("printText fontSize must be greater than zero");
        }

        HashMap<String, Object> command = new HashMap<>();
        command.put("type", "font");

        if (fontSize <= 10) {
            command.put("ascii", "FONT_8_16");
            command.put("ext", "FONT_16_16");
            return command;
        }
        if (fontSize <= 14) {
            command.put("ascii", "FONT_12_24");
            command.put("ext", "FONT_16_16");
            return command;
        }
        if (fontSize <= 18) {
            command.put("ascii", "FONT_16_24");
            command.put("ext", "FONT_16_16");
            return command;
        }
        if (fontSize <= 24) {
            command.put("ascii", "FONT_24_24");
            command.put("ext", "FONT_24_24");
            return command;
        }
        if (fontSize <= 32) {
            command.put("ascii", "FONT_16_32");
            command.put("ext", "FONT_16_32");
            return command;
        }

        command.put("ascii", "FONT_24_48");
        command.put("ext", "FONT_24_48");
        return command;
    }

    @Nullable
    private static Integer optionalInt(Map<String, Object> command, String key) {
        if (!command.containsKey(key) || command.get(key) == null) {
            return null;
        }

        Object value = command.get(key);
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException("printText has invalid number field: " + key);
        }

        return ((Number) value).intValue();
    }

    private static boolean optionalBoolean(
        Map<String, Object> command,
        String key,
        boolean fallback
    ) {
        if (!command.containsKey(key) || command.get(key) == null) {
            return fallback;
        }

        Object value = command.get(key);
        if (!(value instanceof Boolean)) {
            throw new IllegalArgumentException("printText has invalid boolean field: " + key);
        }

        return (Boolean) value;
    }

    @Nullable
    private static String optionalString(Map<String, Object> command, String key) {
        if (!command.containsKey(key) || command.get(key) == null) {
            return null;
        }

        Object value = command.get(key);
        if (!(value instanceof String)) {
            throw new IllegalArgumentException("printText has invalid string field: " + key);
        }

        return (String) value;
    }
}
