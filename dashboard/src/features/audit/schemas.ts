import { z } from "zod";

export const auditItemSchema = z.object({
  id: z.string(),
  adminId: z.string(),
  adminEmail: z.string(),
  country: z.string().nullable(),
  action: z.string(),
  subject: z.string().nullable(),
  detail: z.string().nullable(),
  correlationId: z.string().nullable(),
  createdAt: z.string(),
});

export const auditListSchema = z.object({
  total: z.number(),
  page: z.number(),
  size: z.number(),
  items: z.array(auditItemSchema),
  actions: z.array(z.string()),
});

export type AuditItem = z.infer<typeof auditItemSchema>;
