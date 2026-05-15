export {
  resolveSunmiDeviceInfo,
  resolveSunmiDevicePlatform
} from "./deviceInfo";
export {
  mapSunmiPrintJobToNativePrintJob
} from "./print";
export {
  SunmiPrinterSmokeTestScreen
} from "./smokeTest";
export {
  buildSunmiSmokeTestJob
} from "./smokeTestJob";
export {
  cutPaper,
  getDeviceInfo,
  getPrinterStatus,
  initialize,
  lineWrap,
  prepare,
  print,
  printText,
  setAlignment,
  setFontSize
} from "./native";
export {
  SunmiPrinterError,
  SunmiPrinterErrorCategory,
  SunmiPrinterErrorCode,
  createSunmiPrinterError,
  createSunmiPrinterErrorDetails,
  isSunmiPrinterError,
  isSunmiPrinterErrorCode,
  isPrinterUnavailableSunmiPrinterError,
  isRetryableSunmiPrinterError,
  resolveSunmiPrinterError,
  resolveSunmiPrinterErrorCodeFromStatus
} from "./errors";
export type {
  SunmiPrintJob,
  PrintTextOptions,
  SunmiDeviceInfo,
  SunmiPrintTextAlign,
  SunmiPrinterStatus
} from "./types";
export { SunmiDevicePlatform } from "./types";
export {
  SunmiAsciiFontType,
  SunmiExtFontType,
  SunmiPrintAlign,
  SunmiPrintCutMode
} from "./types";
