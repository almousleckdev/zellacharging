import type { Dictionary } from "../types";

export const enStaff: Pick<Dictionary, "staff"> = {
  staff: {
    title: "NDZela staff",
    subtitle: "The energy used by NDZela staff. These charges are free: nothing is debited or collected.",
    period: "Period",
    periods: { today: "Today", week: "Last 7 days", month: "Last 30 days", all: "All time" },
    loadError: "Could not load staff consumption.",
    energy: "Energy used",
    energyHint: "Completed or interrupted charges",
    value: "Free value",
    valueHint: "At the normal rate, never collected",
    charges: "Charges",
    chargesHint: "Staff charges",
    empty: "No staff charges in this period.",
    columns: { member: "Member", charges: "Charges", energy: "Energy", value: "Free value", last: "Last charge" },
  },
};
