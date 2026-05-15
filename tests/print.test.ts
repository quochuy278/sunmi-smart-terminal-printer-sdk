import { describe, expect, it } from "vitest";
import {
  mapSunmiPrintJobToNativePrintJob
} from "../src/print";
import {
  SunmiAsciiFontType,
  SunmiExtFontType,
  SunmiPrintAlign,
  SunmiPrintCutMode,
  type SunmiPrintJob
} from "../src/types";

describe("SUNMI print job mapping", () => {
  it("maps print commands to a native-ready payload", () => {
    const job: SunmiPrintJob = {
      commands: [
        { type: "align", align: SunmiPrintAlign.Center },
        { type: "text", text: "Hello Sunmi", charset: "UTF-8" },
        { type: "gray", level: 120 },
        { type: "spacing", wordSpace: 2, lineSpace: 8 },
        { type: "indent", pixels: 16 },
        { type: "invert", enabled: true },
        {
          type: "font",
          ascii: SunmiAsciiFontType.Font12x24,
          ext: SunmiExtFontType.Font24x24
        },
        {
          type: "fontScale",
          asciiDoubleWidth: true,
          localDoubleHeight: true
        },
        { type: "feed", pixels: 24 },
        { type: "bitmap", base64: "Zm9v", monoThreshold: 180 },
        { type: "cut", mode: SunmiPrintCutMode.Partial }
      ]
    };

    expect(mapSunmiPrintJobToNativePrintJob(job)).toEqual({
      commands: [
        { type: "align", align: "center" },
        { type: "text", text: "Hello Sunmi", charset: "UTF-8" },
        { type: "gray", level: 120 },
        { type: "spacing", wordSpace: 2, lineSpace: 8 },
        { type: "indent", pixels: 16 },
        { type: "invert", enabled: true },
        {
          type: "font",
          ascii: "FONT_12_24",
          ext: "FONT_24_24"
        },
        {
          type: "fontScale",
          asciiDoubleWidth: true,
          localDoubleWidth: false,
          asciiDoubleHeight: false,
          localDoubleHeight: true
        },
        { type: "feed", pixels: 24 },
        { type: "bitmap", base64: "Zm9v", monoThreshold: 180 },
        { type: "cut", mode: "partial" }
      ]
    });
  });

  it("fills defaults for nullable and optional print fields", () => {
    const job: SunmiPrintJob = {
      commands: [
        { type: "text", text: "Hello Sunmi" },
        { type: "spacing" },
        { type: "fontScale" },
        { type: "cut" }
      ]
    };

    expect(mapSunmiPrintJobToNativePrintJob(job)).toEqual({
      commands: [
        { type: "text", text: "Hello Sunmi", charset: null },
        { type: "spacing", wordSpace: null, lineSpace: null },
        {
          type: "fontScale",
          asciiDoubleWidth: false,
          localDoubleWidth: false,
          asciiDoubleHeight: false,
          localDoubleHeight: false
        },
        { type: "cut", mode: "none" }
      ]
    });
  });
});
