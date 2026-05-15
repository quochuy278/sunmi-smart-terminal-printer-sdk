import {
  SunmiDevicePlatform,
  type SunmiDeviceInfo
} from "./types";

type RawSunmiDeviceInfo = {
  brand?: string | null;
  manufacturer?: string | null;
  model?: string | null;
  sdkInt?: number | null;
};

const normalizeString = (value: string | null | undefined): string =>
  typeof value === "string" ? value.trim() : "";

const normalizeModel = (value: string): string =>
  value.replace(/\s+/g, "").toUpperCase();

export const resolveSunmiDevicePlatform = (
  value: string
): SunmiDevicePlatform => {
  switch (value) {
    case "android":
      return SunmiDevicePlatform.Android;
    case "ios":
      return SunmiDevicePlatform.Ios;
    default:
      return SunmiDevicePlatform.Other;
  }
};

export const resolveSunmiDeviceInfo = (
  raw: RawSunmiDeviceInfo,
  platform: SunmiDevicePlatform
): SunmiDeviceInfo => {
  const manufacturer = normalizeString(raw.manufacturer);
  const brand = normalizeString(raw.brand);
  const model = normalizeString(raw.model);

  return {
    platform,
    manufacturer,
    brand,
    model,
    modelNormalized: normalizeModel(model),
    sdkInt: typeof raw.sdkInt === "number" ? raw.sdkInt : 0
  };
};
