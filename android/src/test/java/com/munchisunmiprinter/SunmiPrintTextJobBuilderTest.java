package com.munchisunmiprinter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SunmiPrintTextJobBuilderTest {
    @Test
    public void buildCreatesTextOnlyJobWithoutOptions() {
        Map<String, Object> job = SunmiPrintTextJobBuilder.build("Hello Sunmi", null);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> commands = (List<Map<String, Object>>) job.get("commands");

        assertEquals(1, commands.size());
        assertEquals("text", commands.get(0).get("type"));
        assertEquals("Hello Sunmi", commands.get(0).get("text"));
        assertEquals("UTF-8", commands.get(0).get("charset"));
    }

    @Test
    public void buildMapsAlignFontSizeAndBoldToCommands() {
        Map<String, Object> options = new HashMap<>();
        options.put("align", "center");
        options.put("fontSize", 24);
        options.put("bold", true);

        Map<String, Object> job = SunmiPrintTextJobBuilder.build("Hello Sunmi", options);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> commands = (List<Map<String, Object>>) job.get("commands");

        assertEquals(4, commands.size());
        assertEquals("align", commands.get(0).get("type"));
        assertEquals("center", commands.get(0).get("align"));
        assertEquals("font", commands.get(1).get("type"));
        assertEquals("FONT_24_24", commands.get(1).get("ascii"));
        assertEquals("FONT_24_24", commands.get(1).get("ext"));
        assertEquals("gray", commands.get(2).get("type"));
        assertEquals(4, commands.get(2).get("level"));
        assertEquals("text", commands.get(3).get("type"));
    }

    @Test
    public void buildRejectsInvalidFontSize() {
        Map<String, Object> options = new HashMap<>();
        options.put("fontSize", 0);

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> SunmiPrintTextJobBuilder.build("Hello Sunmi", options)
        );

        assertTrue(error.getMessage().contains("fontSize"));
    }
}
