import { z } from "zod";

export const customerCountsSchema = z.object({
  total: z.number(),
  publicUsers: z.number(),
  ndzelaDrivers: z.number(),
  ndzelaStaff: z.number(),
  suspended: z.number(),
});

export type CustomerCounts = z.infer<typeof customerCountsSchema>;
