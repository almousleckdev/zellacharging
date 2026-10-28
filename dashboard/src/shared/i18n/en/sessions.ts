import type { Dictionary } from "../types";

export const enSessions: Pick<Dictionary, "sessions"> = {
  sessions: {
    title: "Charging sessions",
    subtitle: "Every charge, newest first.",
    results: "{count} sessions",
    loadError: "We couldn't load the sessions.",
    emptyTitle: "No sessions",
    emptyText: "No session matches these filters.",
    searchLabel: "Search sessions",
    searchPlaceholder: "Station or customer name",
    stateLabel: "State",
    allStates: "All states",
    connector: "Connector",
    unitKwh: "kWh",
    states: {
      REQUESTED: "Requested",
      START_ACCEPTED: "Start accepted",
      ACTIVE: "Charging",
      STOPPING: "Stopping",
      COMPLETED: "Completed",
      FAILED: "Failed",
      ABANDONED: "Interrupted",
    },
    columns: {
      station: "Station",
      customer: "Customer",
      state: "State",
      energy: "Energy",
      batteryStart: "Battery at start",
      batteryEnd: "Battery at end",
      cost: "Amount",
      date: "Started",
    },
  },
};
