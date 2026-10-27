import { z } from "zod";

export const countrySchema = z.object({
  code: z.string(),
  name: z.string(),
  currency: z.string(),
  currencyDecimals: z.number(),
});

export const adminProfileSchema = z.object({
  id: z.string(),
  email: z.string(),
  firstName: z.string().nullable(),
  lastName: z.string().nullable(),
  phone: z.string().nullable(),
  role: z.string(),
  permissions: z.array(z.string()),
  countries: z.array(countrySchema),
  profileComplete: z.boolean(),
  avatarVersion: z.number().nullable(),
});

export type Country = z.infer<typeof countrySchema>;
export type AdminProfile = z.infer<typeof adminProfileSchema>;
