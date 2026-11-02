import { z } from "zod";
import { proxyAuthAction } from "@/server/proxyAuthAction";

const resetSchema = z.object({
  token: z.string().min(1).max(200),
  password: z.string().min(1).max(128),
});

export function POST(request: Request) {
  return proxyAuthAction(request, "/v1/admin/auth/password/reset", resetSchema);
}
