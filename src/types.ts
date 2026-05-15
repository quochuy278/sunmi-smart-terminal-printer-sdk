import type { SunmiPrinterErrorCode } from "./errors";

export enum SunmiDevicePlatform {
  Android = "android",
  Ios = "ios",
  Other = "other"
}

export enum SunmiPrintAlign {
  Left = "left",
  Center = "center",
  Right = "right"
}

export enum SunmiPrintCutMode {
  None = "none",
  Full = "full",
  Partial = "partial"
}

export enum SunmiAsciiFontType {
  Font8x16 = "FONT_8_16",
  Font8x32 = "FONT_8_32",
  Font12x24 = "FONT_12_24",
  Font12x48 = "FONT_12_48",
  Font16x16 = "FONT_16_16",
  Font16x24 = "FONT_16_24",
  Font16x32 = "FONT_16_32",
  Font16x48 = "FONT_16_48",
  Font24x24 = "FONT_24_24",
  Font24x48 = "FONT_24_48",
  Font32x24 = "FONT_32_24",
  Font32x48 = "FONT_32_48"
}

export enum SunmiExtFontType {
  Font16x16 = "FONT_16_16",
  Font16x32 = "FONT_16_32",
  Font24x24 = "FONT_24_24",
  Font24x48 = "FONT_24_48",
  Font32x16 = "FONT_32_16",
  Font32x32 = "FONT_32_32",
  Font48x24 = "FONT_48_24",
  Font48x48 = "FONT_48_48"
}

export type SunmiPrinterStatus = {
  online: boolean;
  paperEmpty: boolean;
  coverOpen: boolean;
  errorCode?: SunmiPrinterErrorCode | null;
  errorMessage?: string | null;
  nativeStatusCode?: number | null;
};

export type SunmiDeviceInfo = {
  platform: SunmiDevicePlatform;
  manufacturer: string;
  brand: string;
  model: string;
  modelNormalized: string;
  sdkInt: number;
};

export type PrintTextOptions = {
  fontSize?: number;
  bold?: boolean;
  align?: "left" | "center" | "right";
};

export type SunmiPrintTextAlign = NonNullable<PrintTextOptions["align"]>;

export type SunmiPrintTextCommand = {
  type: "text";
  text: string;
  charset?: string;
};

export type SunmiPrintBitmapCommand = {
  type: "bitmap";
  base64: string;
  monoThreshold?: number;
};

export type SunmiPrintFeedCommand = {
  type: "feed";
  pixels: number;
};

export type SunmiPrintAlignCommand = {
  type: "align";
  align: SunmiPrintAlign;
};

export type SunmiPrintGrayCommand = {
  type: "gray";
  level: number;
};

export type SunmiPrintSpacingCommand = {
  type: "spacing";
  wordSpace?: number;
  lineSpace?: number;
};

export type SunmiPrintIndentCommand = {
  type: "indent";
  pixels: number;
};

export type SunmiPrintInvertCommand = {
  type: "invert";
  enabled: boolean;
};

export type SunmiPrintFontCommand = {
  type: "font";
  ascii: SunmiAsciiFontType;
  ext: SunmiExtFontType;
};

export type SunmiPrintFontScaleCommand = {
  type: "fontScale";
  asciiDoubleWidth?: boolean;
  localDoubleWidth?: boolean;
  asciiDoubleHeight?: boolean;
  localDoubleHeight?: boolean;
};

export type SunmiPrintCutCommand = {
  type: "cut";
  mode?: SunmiPrintCutMode;
};

export type SunmiPrintCommand =
  | SunmiPrintAlignCommand
  | SunmiPrintBitmapCommand
  | SunmiPrintCutCommand
  | SunmiPrintFeedCommand
  | SunmiPrintFontCommand
  | SunmiPrintFontScaleCommand
  | SunmiPrintGrayCommand
  | SunmiPrintIndentCommand
  | SunmiPrintInvertCommand
  | SunmiPrintSpacingCommand
  | SunmiPrintTextCommand;

export type SunmiPrintJob = {
  commands: SunmiPrintCommand[];
};
