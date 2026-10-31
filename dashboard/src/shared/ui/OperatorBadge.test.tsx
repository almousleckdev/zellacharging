import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { OperatorBadge } from "./OperatorBadge";

describe("OperatorBadge", () => {
  it("shows the Airtel Money logo with its name", () => {
    render(<OperatorBadge operator="AIRTEL" />);

    expect(screen.getByAltText("Airtel Money").getAttribute("src")).toContain("airtel.png");
  });

  it("shows the Moov Money logo next to its name when asked", () => {
    render(<OperatorBadge operator="MOOV" showName />);

    expect(screen.getByText("Moov Money")).toBeInTheDocument();
    expect(document.querySelector("img")?.getAttribute("src")).toContain("moov.png");
  });

  it("falls back to the raw operator code when it is unknown", () => {
    render(<OperatorBadge operator="ORANGE" />);

    expect(screen.getByText("ORANGE")).toBeInTheDocument();
    expect(document.querySelector("img")).toBeNull();
  });
});
