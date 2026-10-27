import "server-only";
import { z } from "zod";

const schema = z.object({
  apiUrl: z.url(),
  secureCookies: z.boolean(),
});

export type ServerEnv = z.infer<typeof schema>;

export function serverEnv(): ServerEnv {
  const production = process.env.NODE_ENV === "production";
  return schema.parse({
    apiUrl: process.env.ZELLA_API_URL ?? (production ? undefined : "http://localhost:8090"),
    secureCookies: production,
  });
}
