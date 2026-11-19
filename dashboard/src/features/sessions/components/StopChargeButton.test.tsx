import { screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, expect, it, vi } from "vitest";
import { gabon, profileOf, renderConsole } from "@/test/renderConsole";
import type { SessionItem } from "../schemas";
import { StopChargeButton } from "./StopChargeButton";

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: vi.fn() }),
  usePathname: () => "/sessions",
  useSearchParams: () => new URLSearchParams(),
}));

function session(state: string, failureCode: string | null = null): SessionItem {
  return {
    id: "s1",
    country: "GA",
    customerId: "c1",
    customerName: "Aline Obame",
    stationId: "DY1",
    connectorId: 2,
    state,
    failureCode,
    energyKwh: 3,
    costMinor: null,
    currency: "XAF",
    decimals: 0,
    startBatteryPercent: null,
    endBatteryPercent: null,
    startedAt: new Date().toISOString(),
    stoppedAt: null,
    createdAt: new Date().toISOString(),
    freeCategory: null,
    ndzelaDriver: false,
  };
}

const operator = { ...profileOf([gabon], "OPERATIONS"), permissions: ["SESSIONS_READ", "SESSIONS_STOP"] };

describe("StopChargeButton", () => {
  it("asks for confirmation and then tells the charger to stop", async () => {
    const fetchMock = vi.fn(async () => new Response(null, { status: 204 }));
    vi.stubGlobal("fetch", fetchMock);
    const user = userEvent.setup();

    renderConsole(<StopChargeButton session={session("ACTIVE")} />, operator);
    await user.click(screen.getByRole("button", { name: "Arrêter" }));
    await user.click(screen.getByRole("button", { name: "Arrêter la charge" }));

    expect(fetchMock).toHaveBeenCalledWith("/api/admin/sessions/GA/s1/stop", expect.objectContaining({ method: "POST" }));
  });

  it("is offered for a charge closed by a lost charger", () => {
    renderConsole(<StopChargeButton session={session("ABANDONED", "CHARGER_OFFLINE")} />, operator);

    expect(screen.getByRole("button", { name: "Arrêter" })).toBeInTheDocument();
  });

  it("is hidden for a finished charge", () => {
    renderConsole(<StopChargeButton session={session("COMPLETED")} />, operator);

    expect(screen.queryByRole("button", { name: "Arrêter" })).not.toBeInTheDocument();
  });

  it("is hidden without the permission", () => {
    renderConsole(<StopChargeButton session={session("ACTIVE")} />, { ...operator, permissions: ["SESSIONS_READ"] });

    expect(screen.queryByRole("button", { name: "Arrêter" })).not.toBeInTheDocument();
  });
});
