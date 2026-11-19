import { z } from "zod";

const totalsSchema = z.object({
  sessions: z.number(),
  energyKwh: z.number(),
  collectedMinor: z.number(),
  staffEnergyKwh: z.number(),
});

export const usageSchema = z.object({
  countries: z.array(
    z.object({
      code: z.string(),
      name: z.string(),
      currency: z.string(),
      decimals: z.number(),
      day: z.string(),
      today: totalsSchema,
      yesterday: totalsSchema,
      week: totalsSchema,
      month: totalsSchema,
    }),
  ),
});

export type UsageResponse = z.infer<typeof usageSchema>;
export type UsageCountry = UsageResponse["countries"][number];
export type UsageTotals = UsageCountry["today"];
