export type FieldKind = "money" | "percent" | "flag";

export type EditableField = { key: string; kind: FieldKind };

export const EDITABLE_FIELDS: Record<string, EditableField[]> = {
  charging: [
    { key: "pricePerKwhMinor", kind: "money" },
    { key: "serviceFeePercent", kind: "percent" },
    { key: "vatPercent", kind: "percent" },
  ],
  wallet: [
    { key: "minimumStartMinor", kind: "money" },
    { key: "maximumStartMinor", kind: "money" },
    { key: "minTopupMinor", kind: "money" },
    { key: "maxTopupMinor", kind: "money" },
    { key: "withdrawalsEnabled", kind: "flag" },
    { key: "withdrawalMinimumMinor", kind: "money" },
    { key: "withdrawalMaximumMinor", kind: "money" },
    { key: "withdrawalDailyMaximumMinor", kind: "money" },
  ],
};

export type Draft = Record<string, string | boolean>;

export function draftOf(fields: EditableField[], values: Record<string, string | number | boolean | null>, decimals: number): Draft {
  const scale = 10 ** decimals;
  return Object.fromEntries(
    fields.map(({ key, kind }) => {
      const value = values[key];
      if (kind === "flag") {
        return [key, value === true];
      }
      if (value === null || value === undefined) {
        return [key, ""];
      }
      return [key, String(kind === "money" ? Number(value) / scale : value)];
    }),
  );
}

export function changesOf(fields: EditableField[], initial: Draft, draft: Draft, decimals: number): Record<string, number | boolean> {
  const scale = 10 ** decimals;
  const changes: Record<string, number | boolean> = {};
  for (const { key, kind } of fields) {
    if (draft[key] === initial[key]) {
      continue;
    }
    if (kind === "flag") {
      changes[key] = draft[key] === true;
    } else if (typeof draft[key] === "string" && draft[key] !== "") {
      const number = Number(draft[key]);
      changes[key] = kind === "money" ? Math.round(number * scale) : number;
    }
  }
  return changes;
}
