import { proxyAvatar } from "@/server/avatarProxy";

const handle = (request: Request) => proxyAvatar(request, "/v1/admin/me/avatar");

export const GET = handle;
export const PUT = handle;
export const DELETE = handle;
