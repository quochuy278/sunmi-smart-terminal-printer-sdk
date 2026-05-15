# @munchi_oy/sunmi-smart-terminal-printer-sdk

React Native bridge for the embedded printer on SUNMI smart terminals.

## Scope

- Supported hardware target: SUNMI terminals with an embedded printer.
- Primary target validated for setup: SUNMI P2 SE.
- Supported capability: embedded printer only.
- External printers are not supported.
- Non-printer terminal integrations are out of scope.

## Install

```bash
pnpm add @munchi_oy/sunmi-smart-terminal-printer-sdk
```

## Android

This package uses SUNMI's Android printer service dependency from Maven Central:

```gradle
implementation "com.sunmi:printerlibrary:1.0.24"
```

The package's `android/build.gradle` already includes that dependency, so consumers do not need to manually copy any local `.jar` or `.so` files.

The Android manifest in this package also declares package visibility for the SUNMI printer service:

```xml
<queries>
  <package android:name="woyou.aidlservice.jiuiv5" />
</queries>
```

That matters for Android 11-based SUNMI devices such as P2 SE.

## iOS

iOS is intentionally disabled. The module still links into a React Native app, but:

- `prepare()` rejects with `UnsupportedPlatform`
- `print()`, `printText()`, and `cutPaper()` reject with `UnsupportedPlatform`
- `getDeviceInfo()` returns a safe fallback object

## Usage

```ts
import {
  prepare,
  getDeviceInfo,
  getPrinterStatus,
  print,
  printText,
  cutPaper
} from "@munchi_oy/sunmi-smart-terminal-printer-sdk";

const device = await getDeviceInfo();
await prepare();
const status = await getPrinterStatus();
await print({
  commands: [
    { type: "align", align: "center" },
    { type: "text", text: "Hello Sunmi\n" }
  ]
});
await printText("Hello Sunmi");
await cutPaper();
```

Call `prepare()` before printing. `initialize()` is kept as an alias to `prepare()`.

## Smoke test screen

This package now exports a ready-made React Native screen for device validation:

```ts
import { SunmiPrinterSmokeTestScreen } from "@munchi_oy/sunmi-smart-terminal-printer-sdk";

export const PrinterTestRoute = () => <SunmiPrinterSmokeTestScreen />;
```

The screen includes buttons for:

- `getDeviceInfo()`
- `prepare()`
- `getPrinterStatus()`
- `printText()`
- `print(job)` with a sample receipt
- `cutPaper()`

Recommended real-device order on a `P2 SE`:

1. Open the screen on the SUNMI device.
2. Tap `Get device info` and confirm the model looks like `P2 SE`.
3. Tap `Prepare printer` and confirm it resolves without error.
4. Tap `Get printer status` and inspect the returned code.
5. Tap `Print text`.
6. Tap `Print sample receipt`.
7. Tap `Cut paper` and see whether the device supports it.

If `cutPaper()` fails but text printing works, that usually means the handheld model does not expose cutter support rather than the whole bridge being broken.

## Consumer compatibility helpers

For migration from stateful SUNMI printer libraries, this package now also exports:

- `setAlignment("left" | "center" | "right")`
- `setFontSize(number)`
- `lineWrap(number)`

`setAlignment()` and `setFontSize()` are stored as JS-side defaults and automatically merged into subsequent `printText()` calls. That means consumer code shaped like this is supported:

```ts
await prepare();
await setAlignment("center");
await setFontSize(24);
await printText("Hello\n");
await lineWrap(3);
```

## Current status

What is wired end to end today:

- `getDeviceInfo()` returns normalized device identity data from the current platform.
- `prepare()` and `initialize()` bind the SUNMI print service and initialize the printer session.
- `getPrinterStatus()` is wired to `updatePrinterState()`.
- `print(job)` is wired to `printerInit -> apply commands`.
- `printText(text, options)` is implemented as a convenience wrapper over `print(job)`.
- `cutPaper()` is wired to `cutPaper()` on the SUNMI service.

What to expect on each platform:

- Android on supported SUNMI hardware: this package is intended to run for real.
- iOS: the package should still build into the app, but printing is not supported.

What is not implemented as a feature:

- no external printer support
- no automatic retry or recovery
- no cash drawer support
- no guarantee of cross-app printer ownership beyond what the embedded SUNMI runtime already does

## Runtime behavior

- Call `prepare()` before `print()`, `printText()`, or `cutPaper()`.
- If `prepare()` has not succeeded yet, JS throws `NotPrepared` before going down to native.
- `print()` and `printText()` are serialized in a native FIFO queue on a worker thread.
- Printer status and callback failures are mapped into the package error model.

## Print commands

`print(job)` currently accepts these command types:

- `align`
- `text`
- `bitmap`
- `feed`
- `font`
- `fontScale`
- `gray`
- `spacing`
- `indent`
- `invert`
- `cut`

Some command types are translated approximately because SUNMI's public service API does not expose the same low-level font and spacing controls as lower-level vendor SDKs.

## Development

```bash
pnpm install
pnpm validate
```

This package currently verifies:

- TypeScript unit tests
- package build
- Android queue unit tests in source form

Real printer validation still requires a SUNMI device such as a P2 SE.
