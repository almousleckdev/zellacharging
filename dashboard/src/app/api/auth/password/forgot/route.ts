import { z } from "zod";
import { proxyAuthAction } from "@/server/proxyAuthAction";

const forgotSchema = z.object({ email: z.string().min(1).max(254) });

export function POST(request: Request) {
  return proxyAuthAction(request, "/v1/admin/auth/password/forgot", forgotSchema);
}
