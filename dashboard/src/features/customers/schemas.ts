import { z } from "zod";

export const customerItemSchema = z.object({
  id: z.string(),
  country: z.string(),
  firstName: z.string(),
  lastName: z.string(),
  email: z.string().nullable(),
  phone: z.string().nullable(),
  status: z.string(),
  createdAt: z.string(),
  balanceMinor: z.number().nullable().optional(),
  currency: z.string().nullable().optional(),
  decimals: z.number().nullable().optional(),
  completedSessions: z.number().optional(),
  spentMinor: z.number().optional(),
  energyKwh: z.number().optional(),
  lastSessionAt: z.string().nullable().optional(),
  avatarVersion: z.number().nullable().optional(),
});

export const customerListSchema = z.object({
  total: z.number(),
  page: z.number(),
  size: z.number(),
  items: z.array(customerItemSchema),
});

const walletSchema = z.object({
  currency: z.string(),
  decimals: z.number(),
  balanceMinor: z.number(),
  reservedMinor: z.number(),
  topupsSucceededMinor: z.number(),
  pendingTopups: z.number(),
  withdrawalsPaidMinor: z.number(),
});

const sessionsSchema = z.object({
  completed: z.number(),
  live: z.number(),
  energyKwh: z.number(),
  spentMinor: z.number(),
  lastSessionAt: z.string().nullable(),
});

const freeAccessSchema = z.object({
  reason: z.string(),
  grantedAt: z.string(),
  expiresAt: z.string().nullable(),
});

export const customerDetailSchema = customerItemSchema.extend({
  emailVerified: z.boolean(),
  phoneVerified: z.boolean(),
  wallet: walletSchema.nullable(),
  sessions: sessionsSchema,
  freeAccess: freeAccessSchema.nullable().optional(),
});

export type CustomerItem = z.infer<typeof customerItemSchema>;
export type CustomerList = z.infer<typeof customerListSchema>;
export type CustomerDetail = z.infer<typeof customerDetailSchema>;
