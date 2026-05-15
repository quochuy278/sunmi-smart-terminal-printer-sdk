import { describe, expect, it } from "vitest";
import {
  resolveSunmiDeviceInfo,
  resolveSunmiDevicePlatform
} from "../src/deviceInfo";
import { SunmiDevicePlatform } from "../src/types";

describe("SUNMI device info platform helpers", () => {
  it("resolves known react native platforms", () => {
    expect(resolveSunmiDevicePlatform("android")).toBe(SunmiDevicePlatform.Android);
    expect(resolveSunmiDevicePlatform("ios")).toBe(SunmiDevicePlatform.Ios);
    expect(resolveSunmiDevicePlatform("web")).toBe(SunmiDevicePlatform.Other);
  });
});

describe("SUNMI device info result model", () => {
  it("returns a normalized identity result for Android", () => {
    const result = resolveSunmiDeviceInfo(
      {
        manufacturer: "SUNMI",
        brand: "SUNMI",
        model: "P2 SE",
        sdkInt: 30
      },
      SunmiDevicePlatform.Android
    );

    expect(result.platform).toBe(SunmiDevicePlatform.Android);
    expect(result.manufacturer).toBe("SUNMI");
    expect(result.brand).toBe("SUNMI");
    expect(result.model).toBe("P2 SE");
    expect(result.modelNormalized).toBe("P2SE");
    expect(result.sdkInt).toBe(30);
  });

  it("returns normalized identity result for iOS", () => {
    const result = resolveSunmiDeviceInfo(
      {
        manufacturer: "Apple",
        brand: "Apple",
        model: "iPhone"
      },
      SunmiDevicePlatform.Ios
    );

    expect(result.platform).toBe(SunmiDevicePlatform.Ios);
    expect(result.manufacturer).toBe("Apple");
    expect(result.brand).toBe("Apple");
    expect(result.model).toBe("iPhone");
    expect(result.modelNormalized).toBe("IPHONE");
    expect(result.sdkInt).toBe(0);
  });
});
