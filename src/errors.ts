export enum SunmiPrinterErrorCategory {
  Device = "DEVICE",
  Integration = "INTEGRATION",
  Permission = "PERMISSION",
  Platform = "PLATFORM",
  Printer = "PRINTER",
  Unknown = "UNKNOWN"
}

export enum SunmiPrinterErrorCode {
  DevicesErrConnect = "DEVICES_ERR_CONNECT",
  DevicesErrInvalidArgument = "DEVICES_ERR_INVALID_ARGUMENT",
  DevicesErrNoPermission = "DEVICES_ERR_NO_PERMISSION",
  DevicesErrNoSupport = "DEVICES_ERR_NO_SUPPORT",
  DevicesErrUnexpected = "DEVICES_ERR_UNEXPECTED",
  ErrorDisabled = "ERROR_DISABLED",
  ErrorNotInWhitelist = "ERROR_NOT_IN_WHITELIST",
  NativeModuleNotLinked = "NATIVE_MODULE_NOT_LINKED",
  NotPrepared = "NOT_PREPARED",
  PrinterErrBusy = "PRINTER_ERR_BUSY",
  PrinterErrCoverOpen = "PRINTER_ERR_COVER_OPEN",
  PrinterErrCutPaper = "PRINTER_ERR_CUT_PAPER",
  PrinterErrCutterJam = "PRINTER_ERR_CUTTER_JAM",
  PrinterErrDataPacketError = "PRINTER_ERR_DATA_PACKET_ERROR",
  PrinterErrFontFormatErr = "PRINTER_ERR_FONT_FORMAT_ERR",
  PrinterErrFontNotExist = "PRINTER_ERR_FONT_NOT_EXIST",
  PrinterErrLackOfFont = "PRINTER_ERR_LACK_OF_FONT",
  PrinterErrOutOfPaper = "PRINTER_ERR_OUT_OF_PAPER",
  PrinterErrPrintUnfinished = "PRINTER_ERR_PRINT_UNFINISHED",
  PrinterErrPrinterOverHeating = "PRINTER_ERR_PRINTER_OVER_HEATING",
  PrinterErrPrinterProblems = "PRINTER_ERR_PRINTER_PROBLEMS",
  PrinterErrTooLong = "PRINTER_ERR_TOO_LONG",
  PrinterErrUnsupportedEncoding = "PRINTER_ERR_UNSUPPORTED_ENCODING",
  PrinterErrVoltageTooLow = "PRINTER_ERR_VOLTAGE_TOO_LOW",
  SunmiSdkNotIntegrated = "SUNMI_SDK_NOT_INTEGRATED",
  Unknown = "UNKNOWN",
  UnknownNativePrinterResult = "UNKNOWN_NATIVE_PRINTER_RESULT",
  UnsupportedPlatform = "UNSUPPORTED_PLATFORM"
}

export type SunmiPrinterOperation =
  | "cutPaper"
  | "getDeviceInfo"
  | "getPrinterStatus"
  | "initialize"
  | "lineWrap"
  | "prepare"
  | "print"
  | "printText"
  | "setAlignment"
  | "setFontSize"
  | "resolveModule";

export type SunmiPrinterErrorDetails = {
  category: SunmiPrinterErrorCategory;
  code: SunmiPrinterErrorCode;
  message: string;
  nativeCode: string | null;
  nativeMessage: string | null;
  nativeStatusCode: number | null;
  operation: SunmiPrinterOperation | null;
  recoverable: boolean;
  retryable: boolean;
};

type SunmiPrinterErrorDefinition = Omit<
  SunmiPrinterErrorDetails,
  "message" | "nativeCode" | "nativeMessage" | "nativeStatusCode" | "operation"
> & {
  defaultMessage: string;
};

type SunmiPrinterErrorInput = Partial<SunmiPrinterErrorDetails> & {
  code: SunmiPrinterErrorCode;
  cause?: unknown;
  message?: string;
};

const ERROR_DEFINITIONS: Record<
  SunmiPrinterErrorCode,
  SunmiPrinterErrorDefinition
> = {
  [SunmiPrinterErrorCode.DevicesErrConnect]: {
    category: SunmiPrinterErrorCategory.Device,
    code: SunmiPrinterErrorCode.DevicesErrConnect,
    defaultMessage: "SUNMI printer connection failed.",
    recoverable: true,
    retryable: true
  },
  [SunmiPrinterErrorCode.DevicesErrInvalidArgument]: {
    category: SunmiPrinterErrorCategory.Device,
    code: SunmiPrinterErrorCode.DevicesErrInvalidArgument,
    defaultMessage: "Invalid argument passed to the SUNMI printer SDK.",
    recoverable: false,
    retryable: false
  },
  [SunmiPrinterErrorCode.DevicesErrNoPermission]: {
    category: SunmiPrinterErrorCategory.Permission,
    code: SunmiPrinterErrorCode.DevicesErrNoPermission,
    defaultMessage: "Missing permission for the SUNMI printer SDK.",
    recoverable: true,
    retryable: false
  },
  [SunmiPrinterErrorCode.DevicesErrNoSupport]: {
    category: SunmiPrinterErrorCategory.Device,
    code: SunmiPrinterErrorCode.DevicesErrNoSupport,
    defaultMessage: "This SUNMI printer operation is not supported on the device.",
    recoverable: false,
    retryable: false
  },
  [SunmiPrinterErrorCode.DevicesErrUnexpected]: {
    category: SunmiPrinterErrorCategory.Device,
    code: SunmiPrinterErrorCode.DevicesErrUnexpected,
    defaultMessage: "Unexpected SUNMI printer device error.",
    recoverable: true,
    retryable: false
  },
  [SunmiPrinterErrorCode.ErrorDisabled]: {
    category: SunmiPrinterErrorCategory.Device,
    code: SunmiPrinterErrorCode.ErrorDisabled,
    defaultMessage: "The SUNMI printer module is disabled.",
    recoverable: true,
    retryable: false
  },
  [SunmiPrinterErrorCode.ErrorNotInWhitelist]: {
    category: SunmiPrinterErrorCategory.Permission,
    code: SunmiPrinterErrorCode.ErrorNotInWhitelist,
    defaultMessage: "The app is not in the SUNMI printer whitelist.",
    recoverable: false,
    retryable: false
  },
  [SunmiPrinterErrorCode.NativeModuleNotLinked]: {
    category: SunmiPrinterErrorCategory.Integration,
    code: SunmiPrinterErrorCode.NativeModuleNotLinked,
    defaultMessage: "The native SUNMI printer module is not linked.",
    recoverable: false,
    retryable: false
  },
  [SunmiPrinterErrorCode.NotPrepared]: {
    category: SunmiPrinterErrorCategory.Integration,
    code: SunmiPrinterErrorCode.NotPrepared,
    defaultMessage: "Call prepare() successfully before printing.",
    recoverable: true,
    retryable: false
  },
  [SunmiPrinterErrorCode.PrinterErrBusy]: {
    category: SunmiPrinterErrorCategory.Printer,
    code: SunmiPrinterErrorCode.PrinterErrBusy,
    defaultMessage: "The SUNMI printer is busy.",
    recoverable: true,
    retryable: true
  },
  [SunmiPrinterErrorCode.PrinterErrCoverOpen]: {
    category: SunmiPrinterErrorCategory.Printer,
    code: SunmiPrinterErrorCode.PrinterErrCoverOpen,
    defaultMessage: "The SUNMI printer cover is open.",
    recoverable: true,
    retryable: false
  },
  [SunmiPrinterErrorCode.PrinterErrCutPaper]: {
    category: SunmiPrinterErrorCategory.Printer,
    code: SunmiPrinterErrorCode.PrinterErrCutPaper,
    defaultMessage: "The SUNMI printer reported a cut paper error.",
    recoverable: true,
    retryable: false
  },
  [SunmiPrinterErrorCode.PrinterErrCutterJam]: {
    category: SunmiPrinterErrorCategory.Printer,
    code: SunmiPrinterErrorCode.PrinterErrCutterJam,
    defaultMessage: "The SUNMI printer cutter is jammed.",
    recoverable: true,
    retryable: false
  },
  [SunmiPrinterErrorCode.PrinterErrDataPacketError]: {
    category: SunmiPrinterErrorCategory.Printer,
    code: SunmiPrinterErrorCode.PrinterErrDataPacketError,
    defaultMessage: "The SUNMI printer received malformed print data.",
    recoverable: false,
    retryable: false
  },
  [SunmiPrinterErrorCode.PrinterErrFontFormatErr]: {
    category: SunmiPrinterErrorCategory.Printer,
    code: SunmiPrinterErrorCode.PrinterErrFontFormatErr,
    defaultMessage: "The SUNMI printer font file format is invalid.",
    recoverable: false,
    retryable: false
  },
  [SunmiPrinterErrorCode.PrinterErrFontNotExist]: {
    category: SunmiPrinterErrorCategory.Printer,
    code: SunmiPrinterErrorCode.PrinterErrFontNotExist,
    defaultMessage: "The requested SUNMI printer font does not exist.",
    recoverable: false,
    retryable: false
  },
  [SunmiPrinterErrorCode.PrinterErrLackOfFont]: {
    category: SunmiPrinterErrorCategory.Printer,
    code: SunmiPrinterErrorCode.PrinterErrLackOfFont,
    defaultMessage: "The SUNMI printer font library is not installed.",
    recoverable: false,
    retryable: false
  },
  [SunmiPrinterErrorCode.PrinterErrOutOfPaper]: {
    category: SunmiPrinterErrorCategory.Printer,
    code: SunmiPrinterErrorCode.PrinterErrOutOfPaper,
    defaultMessage: "The SUNMI printer is out of paper.",
    recoverable: true,
    retryable: false
  },
  [SunmiPrinterErrorCode.PrinterErrPrintUnfinished]: {
    category: SunmiPrinterErrorCategory.Printer,
    code: SunmiPrinterErrorCode.PrinterErrPrintUnfinished,
    defaultMessage: "The SUNMI printer did not finish the print job.",
    recoverable: true,
    retryable: true
  },
  [SunmiPrinterErrorCode.PrinterErrPrinterOverHeating]: {
    category: SunmiPrinterErrorCategory.Printer,
    code: SunmiPrinterErrorCode.PrinterErrPrinterOverHeating,
    defaultMessage: "The SUNMI printer is overheated.",
    recoverable: true,
    retryable: true
  },
  [SunmiPrinterErrorCode.PrinterErrPrinterProblems]: {
    category: SunmiPrinterErrorCategory.Printer,
    code: SunmiPrinterErrorCode.PrinterErrPrinterProblems,
    defaultMessage: "The SUNMI printer reported a hardware fault.",
    recoverable: true,
    retryable: false
  },
  [SunmiPrinterErrorCode.PrinterErrTooLong]: {
    category: SunmiPrinterErrorCategory.Printer,
    code: SunmiPrinterErrorCode.PrinterErrTooLong,
    defaultMessage: "The SUNMI printer data package is too long.",
    recoverable: false,
    retryable: false
  },
  [SunmiPrinterErrorCode.PrinterErrUnsupportedEncoding]: {
    category: SunmiPrinterErrorCategory.Printer,
    code: SunmiPrinterErrorCode.PrinterErrUnsupportedEncoding,
    defaultMessage: "The SUNMI printer encoding is not supported.",
    recoverable: false,
    retryable: false
  },
  [SunmiPrinterErrorCode.PrinterErrVoltageTooLow]: {
    category: SunmiPrinterErrorCategory.Printer,
    code: SunmiPrinterErrorCode.PrinterErrVoltageTooLow,
    defaultMessage: "The SUNMI printer voltage is too low.",
    recoverable: true,
    retryable: true
  },
  [SunmiPrinterErrorCode.SunmiSdkNotIntegrated]: {
    category: SunmiPrinterErrorCategory.Integration,
    code: SunmiPrinterErrorCode.SunmiSdkNotIntegrated,
    defaultMessage: "The SUNMI printer SDK dependency is not integrated.",
    recoverable: false,
    retryable: false
  },
  [SunmiPrinterErrorCode.Unknown]: {
    category: SunmiPrinterErrorCategory.Unknown,
    code: SunmiPrinterErrorCode.Unknown,
    defaultMessage: "Unknown SUNMI printer error.",
    recoverable: false,
    retryable: false
  },
  [SunmiPrinterErrorCode.UnknownNativePrinterResult]: {
    category: SunmiPrinterErrorCategory.Printer,
    code: SunmiPrinterErrorCode.UnknownNativePrinterResult,
    defaultMessage: "The SUNMI printer returned an undocumented status code.",
    recoverable: false,
    retryable: false
  },
  [SunmiPrinterErrorCode.UnsupportedPlatform]: {
    category: SunmiPrinterErrorCategory.Platform,
    code: SunmiPrinterErrorCode.UnsupportedPlatform,
    defaultMessage: "The SUNMI printer bridge is not supported on this platform.",
    recoverable: false,
    retryable: false
  }
};

const NATIVE_CODE_SET = new Set<string>(Object.values(SunmiPrinterErrorCode));

const SUNMI_STATUS_CODE_MAP = new Map<number, SunmiPrinterErrorCode>([
  [2, SunmiPrinterErrorCode.PrinterErrBusy],
  [3, SunmiPrinterErrorCode.DevicesErrConnect],
  [4, SunmiPrinterErrorCode.PrinterErrOutOfPaper],
  [5, SunmiPrinterErrorCode.PrinterErrPrinterOverHeating],
  [6, SunmiPrinterErrorCode.PrinterErrCoverOpen],
  [7, SunmiPrinterErrorCode.PrinterErrCutterJam],
  [9, SunmiPrinterErrorCode.PrinterErrPrinterProblems],
  [505, SunmiPrinterErrorCode.DevicesErrNoSupport],
  [507, SunmiPrinterErrorCode.DevicesErrUnexpected]
]);

const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === "object" && value !== null;

const asString = (value: unknown): string | null =>
  typeof value === "string" && value.length > 0 ? value : null;

const asBoolean = (value: unknown): boolean | null =>
  typeof value === "boolean" ? value : null;

const asNumber = (value: unknown): number | null =>
  typeof value === "number" && Number.isFinite(value) ? value : null;

const asOperation = (value: unknown): SunmiPrinterOperation | null => {
  const operation = asString(value);
  if (!operation) {
    return null;
  }

  switch (operation) {
    case "cutPaper":
    case "getDeviceInfo":
    case "getPrinterStatus":
    case "initialize":
    case "lineWrap":
    case "prepare":
    case "print":
    case "printText":
    case "setAlignment":
    case "setFontSize":
    case "resolveModule":
      return operation;
    default:
      return null;
  }
};

const normalizeNativeCode = (value: unknown): string | null => {
  const code = asString(value);
  return code ? code.trim().toUpperCase() : null;
};

export const isSunmiPrinterErrorCode = (
  value: unknown
): value is SunmiPrinterErrorCode => {
  const normalized = normalizeNativeCode(value);
  return normalized !== null && NATIVE_CODE_SET.has(normalized);
};

export const resolveSunmiPrinterErrorCodeFromStatus = (
  nativeStatusCode: number | null | undefined
): SunmiPrinterErrorCode | null => {
  if (nativeStatusCode == null || nativeStatusCode === 0 || nativeStatusCode === 1 || nativeStatusCode === 8) {
    return null;
  }

  return (
    SUNMI_STATUS_CODE_MAP.get(nativeStatusCode) ??
    SunmiPrinterErrorCode.UnknownNativePrinterResult
  );
};

export const createSunmiPrinterErrorDetails = ({
  code,
  cause: _cause,
  message,
  nativeCode,
  nativeMessage,
  nativeStatusCode,
  operation,
  recoverable,
  retryable,
  category
}: SunmiPrinterErrorInput): SunmiPrinterErrorDetails => {
  const definition = ERROR_DEFINITIONS[code] ?? ERROR_DEFINITIONS[SunmiPrinterErrorCode.Unknown];

  return {
    category: category ?? definition.category,
    code,
    message: message ?? definition.defaultMessage,
    nativeCode: nativeCode ?? null,
    nativeMessage: nativeMessage ?? null,
    nativeStatusCode: nativeStatusCode ?? null,
    operation: operation ?? null,
    recoverable: recoverable ?? definition.recoverable,
    retryable: retryable ?? definition.retryable
  };
};

export class SunmiPrinterError extends Error {
  readonly category: SunmiPrinterErrorCategory;
  readonly code: SunmiPrinterErrorCode;
  readonly nativeCode: string | null;
  readonly nativeMessage: string | null;
  readonly nativeStatusCode: number | null;
  readonly operation: SunmiPrinterOperation | null;
  readonly recoverable: boolean;
  readonly retryable: boolean;

  constructor(details: SunmiPrinterErrorDetails) {
    super(details.message);
    this.name = "SunmiPrinterError";
    this.category = details.category;
    this.code = details.code;
    this.nativeCode = details.nativeCode;
    this.nativeMessage = details.nativeMessage;
    this.nativeStatusCode = details.nativeStatusCode;
    this.operation = details.operation;
    this.recoverable = details.recoverable;
    this.retryable = details.retryable;
  }
}

export const createSunmiPrinterError = (
  input: SunmiPrinterErrorInput
): SunmiPrinterError => {
  const error = new SunmiPrinterError(createSunmiPrinterErrorDetails(input));
  if (input.cause !== undefined) {
    Object.defineProperty(error, "cause", {
      configurable: true,
      enumerable: false,
      value: input.cause,
      writable: true
    });
  }
  return error;
};

export const isSunmiPrinterError = (
  value: unknown
): value is SunmiPrinterError => value instanceof SunmiPrinterError;

export const resolveSunmiPrinterError = (
  value: unknown,
  fallback?: { operation?: SunmiPrinterOperation }
): SunmiPrinterError => {
  if (isSunmiPrinterError(value)) {
    return value;
  }

  if (!isRecord(value)) {
    return createSunmiPrinterError({
      code: SunmiPrinterErrorCode.Unknown,
      operation: fallback?.operation ?? null
    });
  }

  const userInfo = isRecord(value.userInfo) ? value.userInfo : null;
  const nativeStatusCode = asNumber(value.nativeStatusCode) ?? asNumber(userInfo?.nativeStatusCode);
  const normalizedCode =
    normalizeNativeCode(value.code) ??
    normalizeNativeCode(userInfo?.code) ??
    normalizeNativeCode(userInfo?.nativeCode);

  const resolvedCode = isSunmiPrinterErrorCode(normalizedCode)
    ? normalizedCode
    : resolveSunmiPrinterErrorCodeFromStatus(nativeStatusCode) ?? SunmiPrinterErrorCode.Unknown;

  return createSunmiPrinterError({
    code: resolvedCode,
    message: asString(value.message) ?? asString(userInfo?.message) ?? undefined,
    nativeCode: normalizeNativeCode(userInfo?.nativeCode) ?? normalizedCode,
    nativeMessage: asString(userInfo?.nativeMessage) ?? asString(value.message),
    nativeStatusCode,
    operation:
      asOperation(userInfo?.operation) ??
      asOperation(value.operation) ??
      fallback?.operation ??
      null,
    retryable: asBoolean(userInfo?.retryable) ?? undefined,
    recoverable: asBoolean(userInfo?.recoverable) ?? undefined,
    category:
      (asString(userInfo?.category) as SunmiPrinterErrorCategory | null) ??
      undefined
  });
};

export const isRetryableSunmiPrinterError = (value: unknown): boolean =>
  isSunmiPrinterError(value) && value.retryable;

export const isPrinterUnavailableSunmiPrinterError = (
  value: unknown
): boolean => {
  if (!isSunmiPrinterError(value)) {
    return false;
  }

  switch (value.code) {
    case SunmiPrinterErrorCode.PrinterErrBusy:
    case SunmiPrinterErrorCode.PrinterErrCoverOpen:
    case SunmiPrinterErrorCode.PrinterErrCutterJam:
    case SunmiPrinterErrorCode.PrinterErrOutOfPaper:
    case SunmiPrinterErrorCode.PrinterErrPrinterOverHeating:
    case SunmiPrinterErrorCode.DevicesErrConnect:
    case SunmiPrinterErrorCode.DevicesErrNoSupport:
      return true;
    default:
      return false;
  }
};
