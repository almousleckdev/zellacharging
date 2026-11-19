import { z } from "zod";

export const seriesPointSchema = z.object({
  day: z.string(),
  topupsMinor: z.number(),
  topupCount: z.number(),
  withdrawalsMinor: z.number(),
  capturedMinor: z.number(),
  sessions: z.number(),
  completed: z.number(),
  failed: z.number(),
  energyKwh: z.number(),
  revenueMinor: z.number(),
  serviceFeeMinor: z.number(),
  vatMinor: z.number(),
});

export const seriesSchema = z.object({
  countries: z.array(
    z.object({
      code: z.string(),
      name: z.string(),
      currency: z.string(),
      decimals: z.number(),
      today: z.string(),
      points: z.array(seriesPointSchema),
    }),
  ),
});

export type SeriesPoint = z.infer<typeof seriesPointSchema>;
export type SeriesResponse = z.infer<typeof seriesSchema>;
