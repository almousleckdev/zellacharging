import { proxyAvatar } from "@/server/avatarProxy";
import { jsonError } from "@/server/requestGuards";

const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

export async function GET(request: Request, context: { params: Promise<{ id: string }> }) {
  const { id } = await context.params;
  return UUID.test(id) ? proxyAvatar(request, `/v1/admin/users/${id}/avatar`) : jsonError(404, "NOT_FOUND");
}
