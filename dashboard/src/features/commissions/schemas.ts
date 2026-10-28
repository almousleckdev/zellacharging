import { z } from "zod";

export const commissionSummarySchema = z.object({
  country: z.string(),
  currency: z.string(),
  decimals: z.number(),
  sessions: z.number(),
  electricMinor: z.number(),
  serviceFeeMinor: z.number(),
  vatMinor: z.number(),
  serviceFeePercent: z.number(),
  vatPercent: z.number(),
});

export const commissionItemSchema = z.object({
  sessionId: z.string(),
  country: z.string(),
  customerName: z.string(),
  stationId: z.string(),
  electricMinor: z.number(),
  serviceFeeMinor: z.number(),
  vatMinor: z.number(),
  totalMinor: z.number(),
  currency: z.string(),
  decimals: z.number(),
  at: z.string(),
});

export const commissionsSchema = z.object({
  summaries: z.array(commissionSummarySchema),
  items: z.array(commissionItemSchema),
});

export type CommissionSummary = z.infer<typeof commissionSummarySchema>;
export type CommissionItem = z.infer<typeof commissionItemSchema>;
