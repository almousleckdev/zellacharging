import { z } from "zod";

export const ledgerItemSchema = z.object({
  id: z.string(),
  type: z.string(),
  amountMinor: z.number(),
  sessionId: z.string().nullable(),
  topupId: z.string().nullable(),
  createdAt: z.string(),
});

export const ledgerListSchema = z.object({
  total: z.number(),
  page: z.number(),
  size: z.number(),
  currency: z.string().nullable(),
  decimals: z.number(),
  items: z.array(ledgerItemSchema),
});

export const activityItemSchema = z.object({
  source: z.string(),
  actor: z.string().nullable(),
  action: z.string(),
  detail: z.string().nullable(),
  createdAt: z.string(),
});

export const activityListSchema = z.object({
  total: z.number(),
  page: z.number(),
  size: z.number(),
  items: z.array(activityItemSchema),
});

export type LedgerItem = z.infer<typeof ledgerItemSchema>;
export type ActivityItem = z.infer<typeof activityItemSchema>;
