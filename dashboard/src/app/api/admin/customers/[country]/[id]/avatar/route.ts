import { proxyAvatar } from "@/server/avatarProxy";
import { jsonError } from "@/server/requestGuards";

const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
const COUNTRY = /^[A-Z]{2}$/;

export async function GET(request: Request, context: { params: Promise<{ country: string; id: string }> }) {
  const { country, id } = await context.params;
  return COUNTRY.test(country) && UUID.test(id)
    ? proxyAvatar(request, `/v1/admin/customers/${country}/${id}/avatar`)
    : jsonError(404, "NOT_FOUND");
}
