import { z } from "zod";

const customersSchema = z.object({
  total: z.number(),
  active: z.number(),
  newLast30Days: z.number(),
});

const sessionsSchema = z.object({
  live: z.number(),
  completed: z.number(),
  startedLast24Hours: z.number(),
  energyKwh: z.number(),
  revenueMinor: z.number(),
});

const walletsSchema = z.object({
  count: z.number(),
  balanceMinor: z.number(),
  reservedMinor: z.number(),
  topupsSucceededMinor: z.number(),
  pendingTopups: z.number(),
  withdrawalsPaidMinor: z.number(),
  pendingWithdrawals: z.number(),
  capturedMinor: z.number(),
});

export const countryFiguresSchema = z.object({
  code: z.string(),
  name: z.string(),
  currency: z.string(),
  currencyDecimals: z.number(),
  customers: customersSchema,
  sessions: sessionsSchema,
  wallets: walletsSchema,
});

export const overviewSchema = z.object({
  scope: z.string(),
  countries: z.array(countryFiguresSchema),
});

export type CountryFigures = z.infer<typeof countryFiguresSchema>;
export type Overview = z.infer<typeof overviewSchema>;
