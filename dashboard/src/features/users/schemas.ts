import { z } from "zod";

export const adminUserSchema = z.object({
  id: z.string(),
  email: z.string(),
  firstName: z.string().nullable(),
  lastName: z.string().nullable(),
  phone: z.string().nullable(),
  role: z.string(),
  status: z.string(),
  permissions: z.array(z.string()),
  countries: z.array(z.string()),
  lastLoginAt: z.string().nullable(),
  avatarVersion: z.number().nullable(),
});

export const adminUserPageSchema = z.object({
  total: z.number(),
  page: z.number(),
  size: z.number(),
  items: z.array(adminUserSchema),
});

export const invitationResultSchema = z.object({ admin: adminUserSchema, emailSent: z.boolean() });
export const emailResultSchema = z.object({ emailSent: z.boolean() });

export type AdminUser = z.infer<typeof adminUserSchema>;

export const ASSIGNABLE_ROLES = ["ADMIN", "SUPPORT", "FINANCE", "OPERATIONS"] as const;
export type AssignableRole = (typeof ASSIGNABLE_ROLES)[number];

export const ASSIGNABLE_PERMISSIONS = [
  "OVERVIEW_READ",
  "STATIONS_READ",
  "SESSIONS_READ",
  "CUSTOMERS_READ",
  "CUSTOMERS_MANAGE",
  "PAYMENTS_READ",
  "AUDIT_READ",
] as const;

export const ROLE_PRESETS: Record<AssignableRole, readonly string[]> = {
  ADMIN: ["OVERVIEW_READ", "STATIONS_READ", "SESSIONS_READ", "CUSTOMERS_READ", "CUSTOMERS_MANAGE", "PAYMENTS_READ", "AUDIT_READ"],
  SUPPORT: ["OVERVIEW_READ", "CUSTOMERS_READ", "SESSIONS_READ"],
  FINANCE: ["OVERVIEW_READ", "CUSTOMERS_READ", "PAYMENTS_READ"],
  OPERATIONS: ["OVERVIEW_READ", "STATIONS_READ", "SESSIONS_READ"],
};

export type AccessDefinition = {
  role: AssignableRole;
  permissions: string[];
  countries: string[];
};
