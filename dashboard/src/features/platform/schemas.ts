import { z } from "zod";

const settingValue = z.union([z.string(), z.number(), z.boolean(), z.null()]);

export const settingsSectionSchema = z.object({
  section: z.string(),
  values: z.record(z.string(), settingValue),
});

export const tariffsSchema = z.object({
  settings: z.array(settingsSectionSchema),
  stations: z.array(
    z.object({
      country: z.object({ code: z.string(), name: z.string(), currency: z.string(), currencyDecimals: z.number() }),
      prices: z.array(
        z.object({
          stationId: z.string(),
          pricePerKwhMinor: z.number(),
          sessions: z.number(),
          lastUsedAt: z.string().nullable(),
        }),
      ),
    }),
  ),
});

export const systemSchema = z.object({
  settings: z.array(settingsSectionSchema),
  probes: z.array(
    z.object({
      name: z.string(),
      result: z.object({ up: z.boolean(), millis: z.number(), detail: z.string().nullable() }),
    }),
  ),
  startedAtMillis: z.number(),
  javaVersion: z.string(),
});

export type SettingsSection = z.infer<typeof settingsSectionSchema>;
export type Tariffs = z.infer<typeof tariffsSchema>;
export type SystemStatus = z.infer<typeof systemSchema>;
