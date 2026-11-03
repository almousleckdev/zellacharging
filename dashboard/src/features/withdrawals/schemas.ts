import { z } from "zod";

export const withdrawalItemSchema = z.object({
  kind: z.string(),
  id: z.string(),
  country: z.string(),
  customerId: z.string(),
  customerName: z.string(),
  operator: z.string(),
  msisdn: z.string(),
  amountMinor: z.number(),
  currency: z.string(),
  decimals: z.number(),
  state: z.string(),
  failureCode: z.string().nullable(),
  reference: z.string(),
  createdAt: z.string(),
});

export const withdrawalListSchema = z.object({
  total: z.number(),
  page: z.number(),
  size: z.number(),
  items: z.array(withdrawalItemSchema),
});

export type WithdrawalItem = z.infer<typeof withdrawalItemSchema>;

export const withdrawalDetailSchema = z.object({
  payment: withdrawalItemSchema,
  providerRef: z.string().nullable(),
  updatedAt: z.string(),
});

export type WithdrawalDetail = z.infer<typeof withdrawalDetailSchema>;
