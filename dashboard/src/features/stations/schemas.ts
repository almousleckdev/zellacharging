import { z } from "zod";

export const connectorSchema = z.object({
  connectorId: z.number(),
  status: z.string().nullable(),
  connectorType: z.string().nullable(),
  maxPowerKw: z.number().nullable(),
});

export const stationViewSchema = z.object({
  station: z.object({
    id: z.string(),
    isoCode: z.string().nullable(),
    online: z.boolean(),
    vendor: z.string().nullable(),
    model: z.string().nullable(),
    lastSeenAt: z.string().nullable(),
    latitude: z.number().nullable(),
    longitude: z.number().nullable(),
    connectors: z.array(connectorSchema),
  }),
  activity: z.object({
    stationId: z.string(),
    sessions: z.number(),
    live: z.number(),
    energyKwh: z.number(),
    revenueMinor: z.number(),
    lastAt: z.string().nullable(),
  }),
});

export const stationListSchema = z.array(stationViewSchema);

export type StationView = z.infer<typeof stationViewSchema>;
export type Connector = z.infer<typeof connectorSchema>;
