import { z } from "zod";

export const incidentItemSchema = z.object({
  key: z.string(),
  country: z.string(),
  kind: z.string(),
  severity: z.string(),
  subjectType: z.string(),
  subjectId: z.string(),
  customerId: z.string(),
  customerName: z.string(),
  code: z.string().nullable(),
  amountMinor: z.number().nullable(),
  currency: z.string().nullable(),
  decimals: z.number(),
  occurredAt: z.string(),
  status: z.string(),
  note: z.string().nullable(),
});

export const incidentListSchema = z.object({
  total: z.number(),
  open: z.number(),
  openHigh: z.number(),
  page: z.number(),
  size: z.number(),
  items: z.array(incidentItemSchema),
});

export type IncidentItem = z.infer<typeof incidentItemSchema>;
export type IncidentList = z.infer<typeof incidentListSchema>;
