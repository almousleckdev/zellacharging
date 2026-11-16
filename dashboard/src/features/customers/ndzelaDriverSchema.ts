import { z } from "zod";

export const ndzelaDriverSchema = z.object({
  driver: z.object({
    firstName: z.string(),
    lastName: z.string(),
    phone: z.string().nullable().optional(),
    email: z.string().nullable().optional(),
    status: z.string(),
  }),
  contract: z
    .object({
      number: z.string(),
      status: z.string(),
      startDate: z.string(),
      endDate: z.string(),
      dailyRent: z.number(),
    })
    .nullable(),
  car: z
    .object({
      fleetNumber: z.string(),
      plate: z.string(),
      brand: z.string(),
      model: z.string(),
      year: z.number(),
      color: z.string().nullable().optional(),
    })
    .nullable(),
});

export type NdzelaDriver = z.infer<typeof ndzelaDriverSchema>;
