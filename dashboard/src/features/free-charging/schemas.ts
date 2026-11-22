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
  phone: z.string().nullable().optional(),
  email: z.string().nullable().optional(),
  fleetNumber: z.string().nullable().optional(),
  plate: z.string().nullable().optional(),
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

export const driverOptionSchema = z.object({
  driverId: z.string(),
  customerId: z.string().nullable(),
  fullName: z.string(),
  phone: z.string().nullable(),
  email: z.string().nullable(),
  fleetNumber: z.string().nullable(),
  plate: z.string().nullable(),
  status: z.string(),
  linked: z.boolean(),
  inProgram: z.boolean(),
  agreementId: z.string().nullable().optional(),
  agreementStatus: z.string().nullable().optional(),
});

export const driverPageSchema = z.object({
  items: z.array(driverOptionSchema),
  page: z.number(),
  size: z.number(),
  total: z.number(),
});

export type DriverOption = z.infer<typeof driverOptionSchema>;
