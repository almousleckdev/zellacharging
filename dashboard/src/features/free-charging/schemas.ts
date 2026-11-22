import { z } from "zod";

export const agreementSchema = z.object({
  id: z.string(),
  driverId: z.string(),
  driverName: z.string(),
  startsOn: z.string(),
  endsOn: z.string().nullable(),
  status: z.string(),
  conditions: z.string().nullable(),
  eligibility: z.string(),
  unpaidDays: z.number().nullable(),
  lastFreeUse: z.string().nullable(),
});

export const agreementListSchema = z.array(agreementSchema);

export const usageSchema = z.object({
  sessionId: z.string(),
  driverId: z.string(),
  driverName: z.string(),
  startedAt: z.string(),
  energyKwh: z.number().nullable(),
  stopReason: z.string().nullable(),
});

export const usagePageSchema = z.object({
  items: z.array(usageSchema),
  page: z.number(),
  size: z.number(),
  total: z.number(),
});

export type Agreement = z.infer<typeof agreementSchema>;
export type FreeUsage = z.infer<typeof usageSchema>;

export type AgreementTerms = {
  startsOn: string;
  endsOn?: string;
  conditions?: string;
};

export const WEEKDAYS = ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"] as const;

export const freeWindowSchema = z.object({
  day: z.enum(WEEKDAYS),
  from: z.string(),
  until: z.string().nullable(),
  endOfDay: z.boolean(),
});

export type FreeWindow = z.infer<typeof freeWindowSchema>;
