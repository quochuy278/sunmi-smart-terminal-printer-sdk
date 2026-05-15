import { NativeModules, Platform } from "react-native";
import {
  resolveSunmiDeviceInfo,
  resolveSunmiDevicePlatform
} from "./deviceInfo";
import {
  SunmiPrinterErrorCode,
  createSunmiPrinterError,
  resolveSunmiPrinterError
} from "./errors";
import { mapSunmiPrintJobToNativePrintJob, type SunmiNativePrintJob } from "./print";
import type {
  SunmiPrintJob,
  PrintTextOptions,
  SunmiDeviceInfo,
  SunmiPrintTextAlign,
  SunmiPrinterStatus
} from "./types";

type SunmiModule = {
  prepare(): Promise<boolean>;
  getDeviceInfo(): Promise<SunmiDeviceInfo>;
  getPrinterStatus(): Promise<SunmiPrinterStatus>;
  lineWrap(lines: number): Promise<boolean>;
  print(job: SunmiNativePrintJob): Promise<boolean>;
  printText(text: string, options?: PrintTextOptions): Promise<boolean>;
  cutPaper(): Promise<boolean>;
};

const MODULE_NAME = "MunchiSunmiPrinter";
let isPrepared = false;
let currentPrintTextOptions: PrintTextOptions = {};

const unsupportedPlatformModule: SunmiModule = {
  prepare: async () => {
    throw createSunmiPrinterError({
      code: SunmiPrinterErrorCode.UnsupportedPlatform,
      operation: "prepare"
    });
  },
  getDeviceInfo: async () =>
    resolveSunmiDeviceInfo(
      {
        manufacturer: Platform.OS,
        brand: Platform.OS,
        model: Platform.OS,
        sdkInt: 0
      },
      resolveSunmiDevicePlatform(Platform.OS)
    ),
  getPrinterStatus: async () => ({
    online: false,
    paperEmpty: false,
    coverOpen: false,
    errorCode: SunmiPrinterErrorCode.UnsupportedPlatform,
    errorMessage: "The SUNMI printer bridge is not supported on this platform.",
    nativeStatusCode: null
  }),
  lineWrap: async (_lines: number) => false,
  print: async (_job: SunmiNativePrintJob) => false,
  printText: async (_text: string, _options?: PrintTextOptions) => false,
  cutPaper: async () => false
};

const resolveModule = (): SunmiModule => {
  if (Platform.OS !== "android") {
    return unsupportedPlatformModule;
  }

  const nativeModule = NativeModules[MODULE_NAME] as SunmiModule | undefined;
  if (!nativeModule) {
    throw createSunmiPrinterError({
      code: SunmiPrinterErrorCode.NativeModuleNotLinked,
      message: `${MODULE_NAME} is not linked. Rebuild the Android app after integrating the SUNMI module.`,
      nativeCode: SunmiPrinterErrorCode.NativeModuleNotLinked,
      nativeMessage: `${MODULE_NAME} is not linked. Rebuild the Android app after integrating the SUNMI module.`,
      operation: "resolveModule"
    });
  }
  return nativeModule;
};

const execute = async <T>(
  operation: "cutPaper" | "getDeviceInfo" | "getPrinterStatus" | "initialize" | "lineWrap" | "prepare" | "print" | "printText",
  action: () => Promise<T>
): Promise<T> => {
  try {
    return await action();
  } catch (error) {
    throw resolveSunmiPrinterError(error, { operation });
  }
};

const assertPrepared = (
  operation: "cutPaper" | "lineWrap" | "print" | "printText"
): void => {
  if (!isPrepared) {
    throw createSunmiPrinterError({
      code: SunmiPrinterErrorCode.NotPrepared,
      operation
    });
  }
};

const resetPrintTextOptions = (): void => {
  currentPrintTextOptions = {};
};

const mergePrintTextOptions = (
  options?: PrintTextOptions
): PrintTextOptions | undefined => {
  const merged: PrintTextOptions = {
    ...currentPrintTextOptions,
    ...options
  };

  return Object.keys(merged).length > 0 ? merged : undefined;
};

const resolveAlignment = (align: string): SunmiPrintTextAlign => {
  if (align === "left" || align === "center" || align === "right") {
    return align;
  }

  throw createSunmiPrinterError({
    code: SunmiPrinterErrorCode.DevicesErrInvalidArgument,
    message: `Unsupported alignment: ${align}`,
    operation: "setAlignment"
  });
};

const assertFontSize = (fontSize: number): void => {
  if (Number.isFinite(fontSize) && fontSize > 0) {
    return;
  }

  throw createSunmiPrinterError({
    code: SunmiPrinterErrorCode.DevicesErrInvalidArgument,
    message: "setFontSize() requires a font size greater than zero.",
    operation: "setFontSize"
  });
};

const assertLineWrapLines = (lines: number): void => {
  if (Number.isInteger(lines) && lines > 0) {
    return;
  }

  throw createSunmiPrinterError({
    code: SunmiPrinterErrorCode.DevicesErrInvalidArgument,
    message: "lineWrap() requires a positive integer line count.",
    operation: "lineWrap"
  });
};

export const prepare = async (): Promise<boolean> =>
  execute("prepare", async () => {
    resetPrintTextOptions();
    const result = await resolveModule().prepare();
    isPrepared = result;
    return result;
  });

export const initialize = async (): Promise<boolean> =>
  execute("initialize", async () => {
    resetPrintTextOptions();
    const result = await prepare();
    isPrepared = result;
    return result;
  });

export const getDeviceInfo = (): Promise<SunmiDeviceInfo> =>
  execute("getDeviceInfo", async () =>
    resolveSunmiDeviceInfo(
      await resolveModule().getDeviceInfo(),
      resolveSunmiDevicePlatform(Platform.OS)
    )
  );

export const getPrinterStatus = (): Promise<SunmiPrinterStatus> =>
  execute("getPrinterStatus", () => resolveModule().getPrinterStatus());

export const setAlignment = async (
  align: string
): Promise<boolean> => {
  const resolvedAlignment = resolveAlignment(align);
  currentPrintTextOptions = {
    ...currentPrintTextOptions,
    align: resolvedAlignment
  };
  return true;
};

export const setFontSize = async (fontSize: number): Promise<boolean> => {
  assertFontSize(fontSize);
  currentPrintTextOptions = {
    ...currentPrintTextOptions,
    fontSize
  };
  return true;
};

export const lineWrap = (lines: number): Promise<boolean> =>
  execute("lineWrap", () => {
    assertPrepared("lineWrap");
    assertLineWrapLines(lines);
    return resolveModule().lineWrap(lines);
  });

export const print = (job: SunmiPrintJob): Promise<boolean> =>
  execute("print", () => {
    assertPrepared("print");
    return resolveModule().print(mapSunmiPrintJobToNativePrintJob(job));
  });

export const printText = (
  text: string,
  options?: PrintTextOptions
): Promise<boolean> =>
  execute("printText", () => {
    assertPrepared("printText");
    return resolveModule().printText(text, mergePrintTextOptions(options));
  });

export const cutPaper = (): Promise<boolean> =>
  execute("cutPaper", () => {
    assertPrepared("cutPaper");
    return resolveModule().cutPaper();
  });
