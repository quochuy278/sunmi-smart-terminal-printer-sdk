import { describe, expect, it } from "vitest";
import { buildSunmiSmokeTestJob } from "../src/smokeTestJob";

describe("SUNMI smoke test job", () => {
  it("builds a printable receipt skeleton", () => {
    const job = buildSunmiSmokeTestJob();

    expect(job.commands.length).toBeGreaterThan(5);
    expect(job.commands[0]).toEqual({
      type: "align",
      align: "center"
    });
    expect(job.commands[job.commands.length - 1]).toEqual({
      type: "cut"
    });
  });
});
