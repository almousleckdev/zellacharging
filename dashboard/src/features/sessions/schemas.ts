import { z } from "zod";

export const sessionItemSchema = z.object({
  id: z.string(),
  country: z.string(),
  customerId: z.string(),
  customerName: z.string(),
  stationId: z.string(),
  connectorId: z.number(),
  state: z.string(),
  failureCode: z.string().nullable(),
  energyKwh: z.number().nullable(),
  costMinor: z.number().nullable(),
  currency: z.string().nullable(),
  decimals: z.number().nullable(),
  startBatteryPercent: z.number().nullable(),
  endBatteryPercent: z.number().nullable(),
  startedAt: z.string().nullable(),
  stoppedAt: z.string().nullable(),
  createdAt: z.string(),
});

export const sessionListSchema = z.object({
  total: z.number(),
  page: z.number(),
  size: z.number(),
  items: z.array(sessionItemSchema),
});

export type SessionItem = z.infer<typeof sessionItemSchema>;
