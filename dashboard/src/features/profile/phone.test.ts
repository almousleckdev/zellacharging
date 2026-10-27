import { describe, expect, it } from "vitest";
import { joinPhone, splitPhone } from "./phone";

describe("phone helpers", () => {
  it("splits a stored number into its dial code and digits", () => {
    expect(splitPhone("+24177123456")).toEqual({ dial: "+241", digits: "77123456" });
    expect(splitPhone("+22370123456")).toEqual({ dial: "+223", digits: "70123456" });
  });

  it("defaults to Gabon when there is no number", () => {
    expect(splitPhone(null)).toEqual({ dial: "+241", digits: "" });
  });

  it("builds the international number or nothing", () => {
    expect(joinPhone("+241", "77123456")).toBe("+24177123456");
    expect(joinPhone("+241", "")).toBe("");
  });
});
