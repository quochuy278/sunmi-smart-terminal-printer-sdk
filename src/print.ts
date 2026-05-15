import {
  SunmiPrintCutMode,
  type SunmiPrintAlignCommand,
  type SunmiPrintBitmapCommand,
  type SunmiPrintCommand,
  type SunmiPrintCutCommand,
  type SunmiPrintFeedCommand,
  type SunmiPrintFontCommand,
  type SunmiPrintFontScaleCommand,
  type SunmiPrintGrayCommand,
  type SunmiPrintIndentCommand,
  type SunmiPrintInvertCommand,
  type SunmiPrintJob,
  type SunmiPrintSpacingCommand,
  type SunmiPrintTextCommand
} from "./types";

type SunmiNativePrintTextCommand = {
  type: "text";
  text: string;
  charset: string | null;
};

type SunmiNativePrintBitmapCommand = {
  type: "bitmap";
  base64: string;
  monoThreshold: number | null;
};

type SunmiNativePrintFeedCommand = {
  type: "feed";
  pixels: number;
};

type SunmiNativePrintAlignCommand = {
  type: "align";
  align: string;
};

type SunmiNativePrintGrayCommand = {
  type: "gray";
  level: number;
};

type SunmiNativePrintSpacingCommand = {
  type: "spacing";
  wordSpace: number | null;
  lineSpace: number | null;
};

type SunmiNativePrintIndentCommand = {
  type: "indent";
  pixels: number;
};

type SunmiNativePrintInvertCommand = {
  type: "invert";
  enabled: boolean;
};

type SunmiNativePrintFontCommand = {
  type: "font";
  ascii: string;
  ext: string;
};

type SunmiNativePrintFontScaleCommand = {
  type: "fontScale";
  asciiDoubleWidth: boolean;
  localDoubleWidth: boolean;
  asciiDoubleHeight: boolean;
  localDoubleHeight: boolean;
};

type SunmiNativePrintCutCommand = {
  type: "cut";
  mode: string;
};

export type SunmiNativePrintCommand =
  | SunmiNativePrintAlignCommand
  | SunmiNativePrintBitmapCommand
  | SunmiNativePrintCutCommand
  | SunmiNativePrintFeedCommand
  | SunmiNativePrintFontCommand
  | SunmiNativePrintFontScaleCommand
  | SunmiNativePrintGrayCommand
  | SunmiNativePrintIndentCommand
  | SunmiNativePrintInvertCommand
  | SunmiNativePrintSpacingCommand
  | SunmiNativePrintTextCommand;

export type SunmiNativePrintJob = {
  commands: SunmiNativePrintCommand[];
};

const mapTextCommand = (
  command: SunmiPrintTextCommand
): SunmiNativePrintTextCommand => ({
  type: "text",
  text: command.text,
  charset: command.charset ?? null
});

const mapBitmapCommand = (
  command: SunmiPrintBitmapCommand
): SunmiNativePrintBitmapCommand => ({
  type: "bitmap",
  base64: command.base64,
  monoThreshold: command.monoThreshold ?? null
});

const mapFeedCommand = (
  command: SunmiPrintFeedCommand
): SunmiNativePrintFeedCommand => ({
  type: "feed",
  pixels: command.pixels
});

const mapAlignCommand = (
  command: SunmiPrintAlignCommand
): SunmiNativePrintAlignCommand => ({
  type: "align",
  align: command.align
});

const mapGrayCommand = (
  command: SunmiPrintGrayCommand
): SunmiNativePrintGrayCommand => ({
  type: "gray",
  level: command.level
});

const mapSpacingCommand = (
  command: SunmiPrintSpacingCommand
): SunmiNativePrintSpacingCommand => ({
  type: "spacing",
  wordSpace: command.wordSpace ?? null,
  lineSpace: command.lineSpace ?? null
});

const mapIndentCommand = (
  command: SunmiPrintIndentCommand
): SunmiNativePrintIndentCommand => ({
  type: "indent",
  pixels: command.pixels
});

const mapInvertCommand = (
  command: SunmiPrintInvertCommand
): SunmiNativePrintInvertCommand => ({
  type: "invert",
  enabled: command.enabled
});

const mapFontCommand = (
  command: SunmiPrintFontCommand
): SunmiNativePrintFontCommand => ({
  type: "font",
  ascii: command.ascii,
  ext: command.ext
});

const mapFontScaleCommand = (
  command: SunmiPrintFontScaleCommand
): SunmiNativePrintFontScaleCommand => ({
  type: "fontScale",
  asciiDoubleWidth: command.asciiDoubleWidth ?? false,
  localDoubleWidth: command.localDoubleWidth ?? false,
  asciiDoubleHeight: command.asciiDoubleHeight ?? false,
  localDoubleHeight: command.localDoubleHeight ?? false
});

const mapCutCommand = (
  command: SunmiPrintCutCommand
): SunmiNativePrintCutCommand => ({
  type: "cut",
  mode: command.mode ?? SunmiPrintCutMode.None
});

const mapCommand = (command: SunmiPrintCommand): SunmiNativePrintCommand => {
  switch (command.type) {
    case "align":
      return mapAlignCommand(command);
    case "bitmap":
      return mapBitmapCommand(command);
    case "cut":
      return mapCutCommand(command);
    case "feed":
      return mapFeedCommand(command);
    case "font":
      return mapFontCommand(command);
    case "fontScale":
      return mapFontScaleCommand(command);
    case "gray":
      return mapGrayCommand(command);
    case "indent":
      return mapIndentCommand(command);
    case "invert":
      return mapInvertCommand(command);
    case "spacing":
      return mapSpacingCommand(command);
    case "text":
      return mapTextCommand(command);
  }
};

export const mapSunmiPrintJobToNativePrintJob = (
  job: SunmiPrintJob
): SunmiNativePrintJob => ({
  commands: job.commands.map(mapCommand)
});
