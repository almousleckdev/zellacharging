import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

const SESSION_COOKIES = ["zella_at", "zella_rt"];

export function proxy(request: NextRequest) {
  const hasSession = SESSION_COOKIES.some((name) => request.cookies.has(name));
  if (hasSession) {
    return NextResponse.next();
  }
  return NextResponse.redirect(new URL("/login", request.url));
}

export const config = {
  matcher: ["/((?!api|_next/static|_next/image|icon.jpg|logo|operators|login|accept-invite|forgot-password|reset-password).*)"],
};
