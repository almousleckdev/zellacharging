import { z } from "zod";

export const staffUsageSchema = z.array(
  z.object({
    customerId: z.string(),
    country: z.string(),
    customerName: z.string(),
    sessions: z.number(),
    energyKwh: z.number(),
    valueMinor: z.number(),
    currency: z.string().nullable(),
    decimals: z.number().nullable(),
    lastChargeAt: z.string().nullable(),
  }),
);

export type StaffUsage = z.infer<typeof staffUsageSchema>[number];
