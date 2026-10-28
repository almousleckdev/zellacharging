import { z } from "zod";

export const paymentItemSchema = z.object({
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

export const paymentListSchema = z.object({
  total: z.number(),
  page: z.number(),
  size: z.number(),
  items: z.array(paymentItemSchema),
});

export type PaymentItem = z.infer<typeof paymentItemSchema>;
