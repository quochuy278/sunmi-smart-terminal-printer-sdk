import { describe, expect, it } from "vitest";
import {
  SunmiPrinterError,
  SunmiPrinterErrorCategory,
  SunmiPrinterErrorCode,
  createSunmiPrinterError,
  createSunmiPrinterErrorDetails,
  isPrinterUnavailableSunmiPrinterError,
  isRetryableSunmiPrinterError,
  isSunmiPrinterError,
  isSunmiPrinterErrorCode,
  resolveSunmiPrinterError,
  resolveSunmiPrinterErrorCodeFromStatus
} from "../src/errors";

describe("SUNMI printer error codes", () => {
  it("maps documented status codes", () => {
    expect(resolveSunmiPrinterErrorCodeFromStatus(1)).toBeNull();
    expect(resolveSunmiPrinterErrorCodeFromStatus(2)).toBe(
      SunmiPrinterErrorCode.PrinterErrBusy
    );
    expect(resolveSunmiPrinterErrorCodeFromStatus(4)).toBe(
      SunmiPrinterErrorCode.PrinterErrOutOfPaper
    );
    expect(resolveSunmiPrinterErrorCodeFromStatus(5)).toBe(
      SunmiPrinterErrorCode.PrinterErrPrinterOverHeating
    );
    expect(resolveSunmiPrinterErrorCodeFromStatus(6)).toBe(
      SunmiPrinterErrorCode.PrinterErrCoverOpen
    );
    expect(resolveSunmiPrinterErrorCodeFromStatus(7)).toBe(
      SunmiPrinterErrorCode.PrinterErrCutterJam
    );
    expect(resolveSunmiPrinterErrorCodeFromStatus(505)).toBe(
      SunmiPrinterErrorCode.DevicesErrNoSupport
    );
  });

  it("returns unknown marker for undocumented codes", () => {
    expect(resolveSunmiPrinterErrorCodeFromStatus(null)).toBeNull();
    expect(resolveSunmiPrinterErrorCodeFromStatus(999)).toBe(
      SunmiPrinterErrorCode.UnknownNativePrinterResult
    );
  });

  it("detects package error codes", () => {
    expect(isSunmiPrinterErrorCode("PRINTER_ERR_BUSY")).toBe(true);
    expect(isSunmiPrinterErrorCode("printer_err_busy")).toBe(true);
    expect(isSunmiPrinterErrorCode("SUNMI_SDK_NOT_INTEGRATED")).toBe(true);
    expect(isSunmiPrinterErrorCode("NOT_PREPARED")).toBe(true);
    expect(isSunmiPrinterErrorCode("NOT_A_REAL_CODE")).toBe(false);
  });
});

describe("SUNMI printer error model", () => {
  it("creates typed errors with defaults", () => {
    const error = createSunmiPrinterError({
      code: SunmiPrinterErrorCode.PrinterErrBusy
    });

    expect(error).toBeInstanceOf(SunmiPrinterError);
    expect(error.message).toBe("The SUNMI printer is busy.");
    expect(error.category).toBe(SunmiPrinterErrorCategory.Printer);
    expect(error.retryable).toBe(true);
    expect(error.recoverable).toBe(true);
    expect(isSunmiPrinterError(error)).toBe(true);
  });

  it("creates details with overrides", () => {
    const details = createSunmiPrinterErrorDetails({
      code: SunmiPrinterErrorCode.DevicesErrNoPermission,
      message: "Custom permission message",
      nativeCode: "DEVICES_ERR_NO_PERMISSION",
      nativeMessage: "raw native message",
      nativeStatusCode: 321,
      operation: "printText",
      retryable: true,
      recoverable: true
    });

    expect(details.message).toBe("Custom permission message");
    expect(details.nativeCode).toBe("DEVICES_ERR_NO_PERMISSION");
    expect(details.nativeMessage).toBe("raw native message");
    expect(details.nativeStatusCode).toBe(321);
    expect(details.operation).toBe("printText");
    expect(details.retryable).toBe(true);
    expect(details.recoverable).toBe(true);
  });

  it("normalizes react native native-module errors with userInfo", () => {
    const error = resolveSunmiPrinterError({
      code: "SUNMI_SDK_NOT_INTEGRATED",
      message: "Sunmi dependency is not integrated yet",
      userInfo: {
        category: "INTEGRATION",
        nativeCode: "SUNMI_SDK_NOT_INTEGRATED",
        nativeMessage: "Sunmi dependency is not integrated yet",
        nativeStatusCode: null,
        operation: "printText",
        retryable: false,
        recoverable: false
      }
    });

    expect(error.code).toBe(SunmiPrinterErrorCode.SunmiSdkNotIntegrated);
    expect(error.category).toBe(SunmiPrinterErrorCategory.Integration);
    expect(error.operation).toBe("printText");
    expect(error.nativeCode).toBe("SUNMI_SDK_NOT_INTEGRATED");
  });

  it("normalizes native status-only failures", () => {
    const error = resolveSunmiPrinterError(
      {
        message: "Printer failed",
        userInfo: {
          nativeStatusCode: 6,
          operation: "printText",
          retryable: false,
          recoverable: true
        }
      },
      {
        operation: "printText"
      }
    );

    expect(error.code).toBe(SunmiPrinterErrorCode.PrinterErrCoverOpen);
    expect(error.operation).toBe("printText");
    expect(error.nativeStatusCode).toBe(6);
    expect(error.recoverable).toBe(true);
  });

  it("falls back to unknown for unsupported shapes", () => {
    const error = resolveSunmiPrinterError("broken");

    expect(error.code).toBe(SunmiPrinterErrorCode.Unknown);
    expect(error.category).toBe(SunmiPrinterErrorCategory.Unknown);
    expect(error.message).toBe("Unknown SUNMI printer error.");
  });
});

describe("SUNMI printer error utility functions", () => {
  it("flags retryable errors", () => {
    expect(
      isRetryableSunmiPrinterError(
        createSunmiPrinterError({
          code: SunmiPrinterErrorCode.PrinterErrPrinterOverHeating
        })
      )
    ).toBe(true);

    expect(
      isRetryableSunmiPrinterError(
        createSunmiPrinterError({
          code: SunmiPrinterErrorCode.PrinterErrTooLong
        })
      )
    ).toBe(false);
  });

  it("flags printer unavailable conditions", () => {
    expect(
      isPrinterUnavailableSunmiPrinterError(
        createSunmiPrinterError({
          code: SunmiPrinterErrorCode.PrinterErrOutOfPaper
        })
      )
    ).toBe(true);

    expect(
      isPrinterUnavailableSunmiPrinterError(
        createSunmiPrinterError({
          code: SunmiPrinterErrorCode.PrinterErrDataPacketError
        })
      )
    ).toBe(false);
  });

  it("keeps not prepared as a typed integration error", () => {
    const error = resolveSunmiPrinterError({
      code: "NOT_PREPARED",
      message: "Call prepare() successfully before printing."
    });

    expect(error.code).toBe(SunmiPrinterErrorCode.NotPrepared);
    expect(error.category).toBe(SunmiPrinterErrorCategory.Integration);
  });
});
