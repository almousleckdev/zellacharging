import type { Dictionary } from "../types";

export const enOverview: Pick<Dictionary, "overview"> = {
  overview: {
    title: "Overview",
    greeting: "Hello, {name}",
    pending: { title: "Withdrawals to approve", all: "See all", empty: "No withdrawal is waiting." },
    quick: { title: "Quick access" },
    activity: { title: "Recent activity", all: "See all", empty: "No operation yet." },
    subtitle: "Sales and activity · {scope}",
    loadError: "We couldn't load the indicators.",
    unitKwh: "kWh",
    period: { label: "Period", days: "d" },
    kpi: {
      sales: "Sales",
      commission: "Zella commission",
      energy: "Energy delivered",
      charges: "Charges",
      versus: "vs previous {days} days",
      new: "New",
      salesHint: "Total taken from customers",
      commissionHint: "Your share of the charges",
      energyHint: "{count} completed charges",
      chargesHint: "{rate}% successful · {failed} failed",
    },
    charts: {
      revenueTitle: "Sales trend",
      revenueText: "Sales and commission, day by day.",
      sales: "Sales",
      commission: "Commission",
    },
    attention: {
      title: "Needs attention",
      highIncidents: "High-priority incidents",
      openIncidents: "Open incidents",
      offlineStations: "Offline stations",
      liveSessions: "Charges in progress",
    },
    countries: { title: "Sales by country", text: "Comparison over the chosen period.", charges: "{count} charges", new: "New" },
    network: { title: "Station network", all: "See stations", online: "online", charges: "{count} charges", unavailable: "Stations are temporarily unavailable." },
    recent: { charges: "Latest charges", all: "See all", empty: "No charge yet." },
  },
};
