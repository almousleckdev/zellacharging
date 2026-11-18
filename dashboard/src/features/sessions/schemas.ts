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
  freeCategory: z.enum(["NDZELA_STAFF", "OTHER"]).nullable(),
});

export const sessionListSchema = z.object({
  total: z.number(),
  page: z.number(),
  size: z.number(),
  items: z.array(sessionItemSchema),
});

export type SessionItem = z.infer<typeof sessionItemSchema>;

export const sessionDigestSchema = z.object({
  total: z.number(),
  page: z.number(),
  size: z.number(),
  items: z.array(
    z.object({
      customerId: z.string(),
      country: z.string(),
      customerName: z.string(),
      sessions: z.number(),
      energyKwh: z.number(),
      spentMinor: z.number(),
      last: sessionItemSchema,
    }),
  ),
});

export type SessionDigest = z.infer<typeof sessionDigestSchema>["items"][number];

export const sessionDetailSchema = z.object({
  session: sessionItemSchema,
  idTag: z.string(),
  transactionId: z.number().nullable(),
  stopReason: z.string().nullable(),
  pricePerKwhMinor: z.number().nullable(),
  lastVoltage: z.number().nullable(),
  lastPowerKw: z.number().nullable(),
  lastEnergyKwh: z.number().nullable(),
  progressAt: z.string().nullable(),
  updatedAt: z.string(),
  reservedMinor: z.number().nullable(),
  capturedMinor: z.number().nullable(),
  holdState: z.string().nullable(),
  serviceFeeMinor: z.number().nullable(),
  vatMinor: z.number().nullable(),
});

export type SessionDetail = z.infer<typeof sessionDetailSchema>;
