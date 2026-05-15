import { SunmiPrintAlign, type SunmiPrintJob } from "./types";

export const buildSunmiSmokeTestJob = (): SunmiPrintJob => ({
  commands: [
    { type: "align", align: SunmiPrintAlign.Center },
    { type: "text", text: "SUNMI smoke test\n" },
    { type: "text", text: "P2 SE integration check\n\n" },
    { type: "align", align: SunmiPrintAlign.Left },
    { type: "text", text: "1. prepare()\n" },
    { type: "text", text: "2. getPrinterStatus()\n" },
    { type: "text", text: "3. print(job)\n" },
    { type: "text", text: "4. cutPaper()\n\n" },
    { type: "text", text: "If you can read this, printing works.\n\n" },
    { type: "feed", pixels: 48 },
    { type: "cut" }
  ]
});
