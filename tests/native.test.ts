import { beforeEach, describe, expect, it, vi } from "vitest";

const { mockModule } = vi.hoisted(() => ({
  mockModule: {
    prepare: vi.fn(async () => true),
    getDeviceInfo: vi.fn(async () => ({
      manufacturer: "SUNMI",
      brand: "SUNMI",
      model: "P2 SE",
      sdkInt: 30
    })),
    getPrinterStatus: vi.fn(async () => ({
      online: true,
      paperEmpty: false,
      coverOpen: false,
      errorCode: null,
      errorMessage: null,
      nativeStatusCode: null
    })),
    lineWrap: vi.fn(async (_lines: number) => true),
    print: vi.fn(async () => true),
    printText: vi.fn(async (_text: string, _options?: unknown) => true),
    cutPaper: vi.fn(async () => true)
  }
}));

vi.mock("react-native", () => ({
  NativeModules: {
    MunchiSunmiPrinter: mockModule
  },
  Platform: {
    OS: "android"
  }
}));

import {
  lineWrap,
  prepare,
  printText,
  setAlignment,
  setFontSize
} from "../src/native";

describe("SUNMI native compatibility helpers", () => {
  beforeEach(async () => {
    vi.clearAllMocks();
    await prepare();
  });

  it("merges persistent alignment and font size into printText()", async () => {
    await setAlignment("center");
    await setFontSize(48);
    await printText("Hello");

    expect(mockModule.printText).toHaveBeenCalledWith("Hello", {
      align: "center",
      fontSize: 48
    });
  });

  it("lets explicit printText options override persistent defaults", async () => {
    await setAlignment("center");
    await setFontSize(24);
    await printText("Hello", {
      align: "left"
    });

    expect(mockModule.printText).toHaveBeenCalledWith("Hello", {
      align: "left",
      fontSize: 24
    });
  });

  it("forwards lineWrap() to the native module", async () => {
    await lineWrap(3);

    expect(mockModule.lineWrap).toHaveBeenCalledWith(3);
  });
});
